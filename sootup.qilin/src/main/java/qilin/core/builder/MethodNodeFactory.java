/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.builder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.core.PointsToAnalysis;
import qilin.core.pag.*;
import qilin.core.pag.Field;
import qilin.util.PTAUtils;
import qilin.util.queue.UniqueQueue;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

/**
 * @author Ondrej Lhotak
 */
public class MethodNodeFactory {
  protected PAG pag;
  protected MethodPAG mpag;
  protected SootMethod method;
  private final PTAScene scene;

  public MethodNodeFactory(PAG pag, MethodPAG mpag) {
    this.pag = pag;
    this.mpag = mpag;
    method = mpag.getMethod();
    this.scene = pag.getPta().getScene();
  }

  public Node getNode(Value v) {
    if (v instanceof Local) {
      Local l = (Local) v;
      return caseLocal(l);
    } else if (v instanceof JCastExpr) {
      JCastExpr castExpr = (JCastExpr) v;
      return caseCastExpr(castExpr);
    } else if (v instanceof JNewExpr) {
      JNewExpr ne = (JNewExpr) v;
      return caseNewExpr(ne);
    } else if (v instanceof JStaticFieldRef) {
      JStaticFieldRef sfr = (JStaticFieldRef) v;
      return caseStaticFieldRef(sfr);
    } else if (v instanceof JNewArrayExpr) {
      JNewArrayExpr nae = (JNewArrayExpr) v;
      return caseNewArrayExpr(nae);
    } else if (v instanceof JArrayRef) {
      JArrayRef ar = (JArrayRef) v;
      return caseArrayRef(ar);
    } else if (v instanceof ClassConstant) {
      ClassConstant cc = (ClassConstant) v;
      return caseClassConstant(cc);
    } else if (v instanceof StringConstant) {
      StringConstant sc = (StringConstant) v;
      return caseStringConstant(sc);
    } else if (v instanceof JCaughtExceptionRef) {
      JCaughtExceptionRef cef = (JCaughtExceptionRef) v;
      return caseCaughtExceptionRef(cef);
    } else if (v instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) v;
      return caseParameterRef(pr);
    } else if (v instanceof NullConstant) {
      NullConstant nc = (NullConstant) v;
      return caseNullConstant(nc);
    } else if (v instanceof JInstanceFieldRef) {
      JInstanceFieldRef ifr = (JInstanceFieldRef) v;
      return caseInstanceFieldRef(ifr);
    } else if (v instanceof JThisRef) {
      return caseThis();
    } else if (v instanceof JNewMultiArrayExpr) {
      JNewMultiArrayExpr nmae = (JNewMultiArrayExpr) v;
      return caseNewMultiArrayExpr(nmae);
    }
    System.out.println(v + ";;" + v.getClass());
    return null;
  }

  /** Adds the edges required for this statement to the graph. */
  public final void handleStmt(Stmt s) {
    if (s.isInvokableStmt() && s.asInvokableStmt().containsInvokeExpr()) {
      mpag.addCallStmt(s.asInvokableStmt());
      handleInvokeStmt(s.asInvokableStmt());
    } else {
      handleIntraStmt(s);
    }
  }

  /**
   * Adds the edges required for this statement to the graph. Add throw stmt if the invoke method
   * throws an Exception.
   */
  protected void handleInvokeStmt(InvokableStmt s) {
    AbstractInvokeExpr ie = s.getInvokeExpr().get();
    int numArgs = ie.getArgCount();
    for (int i = 0; i < numArgs; i++) {
      Value arg = ie.getArg(i);
      if (!(arg.getType() instanceof ReferenceType) || arg instanceof NullConstant) {
        continue;
      }
      getNode(arg);
    }
    if (s instanceof JAssignStmt) {
      JAssignStmt assignStmt = (JAssignStmt) s;
      Value l = assignStmt.getLeftOp();
      if ((l.getType() instanceof ReferenceType)) {
        getNode(l);
      }
    }
    if (ie instanceof AbstractInstanceInvokeExpr) {
      AbstractInstanceInvokeExpr aie = (AbstractInstanceInvokeExpr) ie;
      getNode(aie.getBase());
    }
  }

  private void resolveClinit(JStaticFieldRef staticFieldRef) {
    FieldSignature fieldSig = staticFieldRef.getFieldSignature();
    ClassType classType = fieldSig.getDeclClassType();
    if (PTAUtils.isFakeMainClass(classType)) { // skip FakeMain
      return;
    }
    SootClass sootClass = scene.getView().getClass(classType).get();
    clinitsOf(sootClass).forEach(mpag::addTriggeredClinit);
  }

  /** Adds the edges required for this statement to the graph. */
  private void handleIntraStmt(Stmt s) {
    s.accept(
        new AbstractStmtVisitor() {
          protected Object result = null;

          protected void setResult(Object result) {
            this.result = result;
          }

          public Object getResult() {
            return result;
          }

          @Override
          public void caseAssignStmt(@NonNull JAssignStmt stmt) {
            Value l = stmt.getLeftOp();
            Value r = stmt.getRightOp();
            if (l instanceof JStaticFieldRef) {
              resolveClinit((JStaticFieldRef) l);
            } else if (r instanceof JStaticFieldRef) {
              resolveClinit((JStaticFieldRef) r);
            }

            if (!(l.getType() instanceof ReferenceType)) return;
            // check for improper casts, with mal-formed code we might get
            // l = (refliketype)int_type, if so just return
            if (r instanceof JCastExpr
                && (!(((JCastExpr) r).getOp().getType() instanceof ReferenceType))) {
              return;
            }

            if (!(r.getType() instanceof ReferenceType)) return;
            Node dest = getNode(l);
            Node src = getNode(r);
            mpag.addInternalEdge(src, dest);
          }

          @Override
          public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
            if (!(stmt.getLeftOp().getType() instanceof ReferenceType)) {
              return;
            }
            Node dest = getNode(stmt.getLeftOp());
            Node src = getNode(stmt.getRightOp());
            mpag.addInternalEdge(src, dest);
          }

          @Override
          public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
            defaultCaseStmt(stmt);
          }

          @Override
          public void caseReturnStmt(@NonNull JReturnStmt stmt) {
            if (!(stmt.getOp().getType() instanceof ReferenceType)) return;
            Node retNode = getNode(stmt.getOp());
            mpag.addInternalEdge(retNode, caseRet());
          }

          @Override
          public void caseThrowStmt(@NonNull JThrowStmt stmt) {
            if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
              mpag.addInternalEdge(getNode(stmt.getOp()), getNode(scene.getFieldGlobalThrow()));
            }
          }
        });
  }

  private VarNode caseLocal(Local l) {
    return pag.makeLocalVarNode(l, l.getType(), method);
  }

  private AllocNode caseNewArrayExpr(JNewArrayExpr nae) {
    return pag.makeAllocNode(nae, nae.getType(), method);
  }

  private AllocNode caseNewExpr(JNewExpr ne) {
    SootClass cl = scene.getSootClass(ne.getType().toString());
    clinitsOf(cl).forEach(mpag::addTriggeredClinit);
    return pag.makeAllocNode(ne, ne.getType(), method);
  }

  private FieldRefNode caseInstanceFieldRef(JInstanceFieldRef ifr) {
    FieldSignature fieldSig = ifr.getFieldSignature();
    Optional<? extends SootField> osf = scene.getView().getField(fieldSig);
    SootField sf;
    if (!osf.isPresent()) {
      sf =
          new SootField(
              fieldSig,
              Collections.singleton(FieldModifier.PUBLIC),
              NoPositionInformation.getInstance());
      // System.out.println("Warnning:" + ifr + " is resolved to be a null field in Scene.");
    } else {
      sf = osf.get();
    }
    Local base = ifr.getBase();
    return pag.makeFieldRefNode(pag.makeLocalVarNode(base, base.getType(), method), new Field(sf));
  }

  private VarNode caseNewMultiArrayExpr(JNewMultiArrayExpr nmae) {
    ArrayType type = (ArrayType) nmae.getType();
    int pos = 0;
    AllocNode prevAn =
        pag.makeAllocNode(
            JavaJimple.getInstance().newNewArrayExpr(type, nmae.getSize(pos)), type, method);
    VarNode prevVn = pag.makeLocalVarNode(prevAn.getNewExpr(), prevAn.getType(), method);
    mpag.addInternalEdge(prevAn, prevVn); // new
    VarNode ret = prevVn;
    while (true) {
      Type t = type.getElementType();
      if (!(t instanceof ArrayType)) {
        break;
      }
      type = (ArrayType) t;
      ++pos;
      Immediate sizeVal;
      if (pos < nmae.getSizeCount()) {
        sizeVal = nmae.getSize(pos);
      } else {
        sizeVal = IntConstant.getInstance(1);
      }
      AllocNode an =
          pag.makeAllocNode(JavaJimple.getInstance().newNewArrayExpr(type, sizeVal), type, method);
      VarNode vn = pag.makeLocalVarNode(an.getNewExpr(), an.getType(), method);
      mpag.addInternalEdge(an, vn); // new
      mpag.addInternalEdge(vn, pag.makeFieldRefNode(prevVn, ArrayElement.v())); // store
      prevVn = vn;
    }
    return ret;
  }

  private VarNode caseCastExpr(JCastExpr ce) {
    Node opNode = getNode(ce.getOp());
    VarNode castNode = pag.makeLocalVarNode(ce, ce.getType(), method);
    mpag.addInternalEdge(opNode, castNode);
    return castNode;
  }

  public VarNode caseThis() {
    Type type =
        method.isStatic()
            ? PTAUtils.getClassType("java.lang.Object")
            : method.getDeclaringClassType();
    VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.THIS_NODE), type, method);
    ret.setInterProcTarget();
    return ret;
  }

  public VarNode caseParm(int index) {
    VarNode ret =
        pag.makeLocalVarNode(new Parm(method, index), method.getParameterType(index), method);
    ret.setInterProcTarget();
    return ret;
  }

  public VarNode caseRet() {
    VarNode ret =
        pag.makeLocalVarNode(
            new Parm(method, PointsToAnalysis.RETURN_NODE), method.getReturnType(), method);
    ret.setInterProcSource();
    return ret;
  }

  public VarNode caseMethodThrow() {
    VarNode ret =
        pag.makeLocalVarNode(
            new Parm(method, PointsToAnalysis.THROW_NODE),
            PTAUtils.getClassType("java.lang.Throwable"),
            method);
    ret.setInterProcSource();
    return ret;
  }

  public final FieldRefNode caseArray(VarNode base) {
    return pag.makeFieldRefNode(base, ArrayElement.v());
  }

  private Node caseCaughtExceptionRef(JCaughtExceptionRef cer) {
    if (CoreConfig.v().getPtaConfig().preciseExceptions) {
      // we model caughtException expression as an local assignment.
      return pag.makeLocalVarNode(cer, cer.getType(), method);
    } else {
      return getNode(scene.getFieldGlobalThrow());
    }
  }

  private FieldRefNode caseArrayRef(JArrayRef ar) {
    return caseArray(caseLocal(ar.getBase()));
  }

  private VarNode caseParameterRef(JParameterRef pr) {
    return caseParm(pr.getIndex());
  }

  private VarNode caseStaticFieldRef(JStaticFieldRef sfr) {
    return pag.makeGlobalVarNode(sfr.getFieldSignature(), sfr.getType());
  }

  private Node caseNullConstant(NullConstant nr) {
    return null;
  }

  private VarNode caseStringConstant(StringConstant sc) {
    AllocNode stringConstantNode = pag.makeStringConstantNode(sc);
    VarNode stringConstantVar =
        pag.makeGlobalVarNode(sc, PTAUtils.getClassType("java.lang.String"));
    mpag.addInternalEdge(stringConstantNode, stringConstantVar);
    VarNode vn = pag.makeLocalVarNode(sc, PTAUtils.getClassType("java.lang.String"), method);
    mpag.addInternalEdge(stringConstantVar, vn);
    return vn;
  }

  public LocalVarNode makeInvokeStmtThrowVarNode(Stmt invoke, SootMethod method) {
    return pag.makeLocalVarNode(invoke, PTAUtils.getClassType("java.lang.Throwable"), method);
  }

  public final VarNode caseClassConstant(ClassConstant cc) {
    AllocNode classConstant = pag.makeClassConstantNode(cc);
    VarNode classConstantVar = pag.makeGlobalVarNode(cc, PTAUtils.getClassType("java.lang.Class"));
    mpag.addInternalEdge(classConstant, classConstantVar);
    VarNode vn = pag.makeLocalVarNode(cc, PTAUtils.getClassType("java.lang.Class"), method);
    mpag.addInternalEdge(classConstantVar, vn);
    return vn;
  }

  /*
   * We use this method to replace EntryPoints.v().clinitsOf() because it is infested with bugs.
   * */
  public Set<SootMethod> clinitsOf(SootClass cl) {
    Set<SootMethod> ret = new HashSet<>();
    Set<SootClass> visit = new HashSet<>();
    Queue<SootClass> worklist = new UniqueQueue<>();
    Optional<? extends ClassType> curr = Optional.of(cl.getType());
    while (curr.isPresent()) {
      ClassType ct = curr.get();
      SootClass sc = scene.getView().getClass(ct).get();
      worklist.add(sc);
      curr = sc.getSuperclass();
    }
    while (!worklist.isEmpty()) {
      SootClass sc = worklist.poll();
      if (visit.add(sc)) {
        Set<? extends ClassType> itfs = sc.getInterfaces();
        for (ClassType itf : itfs) {
          Optional<? extends SootClass> xsc = scene.getView().getClass(itf);
          xsc.ifPresent(worklist::add);
        }
      }
    }
    for (SootClass sc : visit) {
      MethodSubSignature subclinit =
          scene.getView().getIdentifierFactory().parseMethodSubSignature("void <clinit>()");
      final Optional<? extends SootMethod> initStart = sc.getMethod(subclinit);
      initStart.ifPresent(ret::add);
    }
    return ret;
  }
}
