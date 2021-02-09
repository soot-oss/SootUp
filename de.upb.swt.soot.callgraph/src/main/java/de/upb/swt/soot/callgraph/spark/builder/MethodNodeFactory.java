package de.upb.swt.soot.callgraph.spark.builder;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.callgraph.spark.pag.IntraproceduralPointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.callgraph.spark.pointsto.PointsToAnalysis;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.visitor.AbstractJimpleValueVisitor;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MethodNodeFactory extends AbstractJimpleValueVisitor implements NodeFactory {
  private SootMethod method;
  private IntraproceduralPointerAssignmentGraph intraPag;
  private PointerAssignmentGraph pag;
  private View<? extends SootClass> view;

  protected final ReferenceType rtClass;
  protected final ReferenceType rtObject;
  protected final ReferenceType rtStringType;
  protected final ReferenceType rtHashSet;
  protected final ReferenceType rtHashMap;
  protected final ReferenceType rtLinkedList;
  protected final ReferenceType rtHashtableEmptyIterator;
  protected final ReferenceType rtHashtableEmptyEnumerator;

  public MethodNodeFactory(IntraproceduralPointerAssignmentGraph intraPag) {
    this.intraPag = intraPag;
    this.pag = intraPag.getPointerAssignmentGraph();
    this.view = pag.getView();
    setMethod(intraPag.getMethod());

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    rtClass = new JavaClassType(CLASS, identifierFactory.getPackageName(JAVA_LANG));
    rtObject = new JavaClassType(OBJECT, identifierFactory.getPackageName(JAVA_LANG));
    rtStringType = new JavaClassType(STRING, identifierFactory.getPackageName(JAVA_LANG));
    rtHashSet = new JavaClassType(HASH_SET, identifierFactory.getPackageName(JAVA_UTIL));
    rtHashMap = new JavaClassType(HASH_MAP, identifierFactory.getPackageName(JAVA_UTIL));
    rtLinkedList = new JavaClassType(LINKED_LIST, identifierFactory.getPackageName(JAVA_UTIL));
    rtHashtableEmptyIterator =
        new JavaClassType(HASH_TABLE_EMPTY_ITERATOR, identifierFactory.getPackageName(JAVA_UTIL));
    rtHashtableEmptyEnumerator =
        new JavaClassType(
            HASH_TABLE_EMPTY_NUMERATOR, identifierFactory.getPackageName(JAVA_UTIL));
  }

  /** Sets the method for which a graph is currently being built. */
  private void setMethod(SootMethod m) {
    // TODO: cases
    method = m;
    if (!m.isStatic()) {
      SootClass c = view.getClassOrThrow(m.getDeclaringClassType());
      // caseThis();
    }
    for (int i = 0; i < m.getParameterCount(); i++) {
      if (m.getParameterType(i) instanceof ReferenceType) {
        // caseParm(i);
      }
    }
    Type retType = m.getReturnTypeSignature();
    if (retType instanceof ReferenceType) {
      // caseRet();
    }
  }

  public Node getNode(Value v) {
    v.accept(this);
    return getNode();
  }

  public final Node getNode() {
    return (Node) getResult();
  }

  public void processStmt(Stmt stmt) {
    if (!canProcess(stmt)) {
      return;
    }
    stmt.accept(
        new AbstractStmtVisitor() {
          @Override
          public final void caseAssignStmt(JAssignStmt stmt) {
            Value leftOp = stmt.getLeftOp();
            Value rightOp = stmt.getRightOp();
            if (!(leftOp.getType() instanceof ReferenceType)) {
              return;
            }
            if (!(rightOp.getType() instanceof ReferenceType)) {
              throw new AssertionError(
                  "Type mismatch in assignment " + stmt + " in method " + method.getSignature());
            }
            leftOp.accept(MethodNodeFactory.this);
            Node target = getNode();
            rightOp.accept(MethodNodeFactory.this);
            Node source = getNode();
            if (leftOp instanceof JInstanceFieldRef) {
              ((JInstanceFieldRef) leftOp).getBase().accept(MethodNodeFactory.this);
              pag.addDereference((VariableNode) getNode());
            }
            if (rightOp instanceof JInstanceFieldRef) {
              ((JInstanceFieldRef) rightOp).getBase().accept(MethodNodeFactory.this);
              pag.addDereference((VariableNode) getNode());
            } else if (rightOp instanceof JStaticFieldRef) {
              JStaticFieldRef staticFieldRef = (JStaticFieldRef) rightOp;
              staticFieldRef.getFieldSignature();
              // TODO: SPARK_OPT empties-as-allocs
            }
            intraPag.addEdge(source, target);
          }

          @Override
          public void caseReturnStmt(JReturnStmt returnStmt) {
            if (!(returnStmt.getOp().getType() instanceof ReferenceType)) {
              return;
            }
            returnStmt.getOp().accept(MethodNodeFactory.this);
            Node returnNode = getNode();
            intraPag.addEdge(returnNode, caseReturn());
            throw new NotImplementedException();
          }

          @Override
          public void caseIdentityStmt(JIdentityStmt identityStmt) {
            if (!(identityStmt.getLeftOp().getType() instanceof ReferenceType)) {
              return;
            }
            Value leftOp = identityStmt.getLeftOp();
            Value rightOp = identityStmt.getRightOp();
            leftOp.accept(MethodNodeFactory.this);
            Node target = getNode();
            rightOp.accept(MethodNodeFactory.this);
            Node source = getNode();
            intraPag.addEdge(source, target);

            // TODO: SPARK_OPT library_disabled

          }

          @Override
          public void caseThrowStmt(JThrowStmt throwStmt) {
            throwStmt.getOp().accept(MethodNodeFactory.this);
            // TODO: mpag.addOutEdge(getNode(), pag.nodeFactory().caseThrow());
            throw new NotImplementedException();
          }
        });
  }

  private boolean canProcess(Stmt stmt) {
    // TODO: types-for-invoke
    if (stmt.containsInvokeExpr()) {
      AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
      if (!isReflectionNewInstance(invokeExpr)) {
        return false;
      } else if (!(invokeExpr instanceof JStaticInvokeExpr)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given invocation is for Class.newInstance()
   *
   * @param invokeExpr
   * @return
   */
  private boolean isReflectionNewInstance(AbstractInvokeExpr invokeExpr) {
    // TODO: put this in a utility class?
    if (invokeExpr instanceof JVirtualInvokeExpr) {
      JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) invokeExpr;
      if (virtualInvokeExpr.getBase().getType() instanceof ReferenceType) {
        ReferenceType rt = (ReferenceType) virtualInvokeExpr.getBase().getType();
        if (rt.getClass().getName().equals("java.lang.Class")) {
          MethodSignature signature = virtualInvokeExpr.getMethodSignature();
          if (signature.getName().equals("newInstance")
              && signature.getParameterTypes().size() == 0) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public Node caseThis() {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, String>(method, PointsToAnalysis.THIS_NODE),
            method.getDeclaringClassType(),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public Node caseParameter(int index) {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, Integer>(method, new Integer(index)),
            method.getParameterType(index),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public void casePhiExpr() {
    throw new NotImplementedException();
  }

  public Node caseReturn() {
    VariableNode node =
        pag.getOrCreateLocalVariableNode(
            new ImmutablePair<SootMethod, String>(method, PointsToAnalysis.RETURN_NODE),
            method.getReturnTypeSignature(),
            method);
    // TODO: setInterProcTarget
    return node;
  }

  public Node caseArray(VariableNode base) {
    return pag.getOrCreateFieldReferenceNode(base, new ArrayElement());
  }

  public void caseArrayRef(JArrayRef ref) {
    caseLocal((Local) ref.getBase());
    setResult(caseArray((VariableNode) getNode()));
  }

  public void caseCastExpr(JCastExpr castExpr) {
    Pair<JCastExpr, String> castPair = new ImmutablePair<>(castExpr, PointsToAnalysis.CAST_NODE);
    castExpr.getOp().accept(this);
    Node opNode = getNode();
    Node castNode = pag.getOrCreateLocalVariableNode(castPair, castExpr.getType(), method);
    intraPag.addEdge(opNode, castNode);
    setResult(castNode);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef ref) {
    setResult(pag.getNodeFactory().caseThrow());
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef ref) {
    // TODO: SPARK_OPT field-based vta
    setResult(
        pag.getOrCreateLocalFieldReferenceNode(
            ref.getBase(), ref.getBase().getType(), ref.getField(view).get(), method));
  }

  public void caseLocal(Local local) {
    setResult(pag.getOrCreateLocalVariableNode(local, local.getType(), method));
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr expr) {
    setResult(pag.getOrCreateAllocationNode(expr, expr.getType(), method));
  }

  private boolean isStringBuffer(Type type) {
    if (!(type instanceof ReferenceType)) {
      return false;
    }
    ReferenceType refType = (ReferenceType) type;
    String s = refType.toString();
    if (s.equals("java.lang.StringBuffer")) {
      return true;
    }
    if (s.equals("java.lang.StringBuilder")) {
      return true;
    }
    return false;
  }

  @Override
  public void caseNewExpr(JNewExpr expr) {
    // TODO: SPARK_OPT merge-stringbuffer
    setResult(pag.getOrCreateAllocationNode(expr, expr.getType(), method));
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
    ArrayType type = (ArrayType) expr.getType();
    AllocationNode prevAllocationNode =
        pag.getOrCreateAllocationNode(
            new ImmutablePair<Expr, Integer>(expr, new Integer(type.getDimension())), type, method);
    VariableNode prevVariableNode =
        pag.getOrCreateLocalVariableNode(prevAllocationNode, prevAllocationNode.getType(), method);
    intraPag.addEdge(prevAllocationNode, prevVariableNode);
    setResult(prevAllocationNode);
    // TODO: do we need to handle elementType?
  }

  public void caseParameterRef(JParameterRef ref) {
    setResult(caseParameter(ref.getIndex()));
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef ref) {
    setResult(
        pag.getOrCreateGlobalVariableNode(ref.getField(view), ref.getField(view).get().getType()));
  }

  @Override
  public void caseStringConstant(StringConstant sc) {
    // TODO: SPARK_OPT string-constant
    AllocationNode strConstant =
        pag.getOrCreateAllocationNode(PointsToAnalysis.STRING_NODE, rtStringType, null);
    VariableNode strConstantLocal = pag.getOrCreateGlobalVariableNode(strConstant, rtStringType);
    pag.addEdge(strConstant, strConstantLocal);
    setResult(strConstantLocal);
  }

  public void caseThisRef(JThisRef ref) {
    setResult(caseThis());
  }

  public void caseNullConstant(NullConstant nullConstant) {
    setResult(null);
  }

  @Override
  public void caseClassConstant(ClassConstant cc) {
    AllocationNode classConstant = pag.getOrCreateClassConstantNode(cc);
    VariableNode classConstantLocal = pag.getOrCreateGlobalVariableNode(classConstant, rtClass);
    pag.addEdge(classConstant, classConstantLocal);
    setResult(classConstantLocal);
  }

  @Override
  public void defaultCase(Object obj) {
    throw new RuntimeException("failed to handle " + obj);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    if (expr.getArgCount() == 1
        && expr.getArg(0) instanceof StringConstant
        && methodSignature.getName().equals("forName")
        && methodSignature.getDeclClassType().getFullyQualifiedName().equals("java.lang.Class")
        && methodSignature.getParameterTypes().size() == 1) {
      // This is a call to Class.forName
      StringConstant classNameConst = (StringConstant) expr.getArg(0);
      String constStr = "L" + classNameConst.getValue().replaceAll("\\.", "/") + ";";
      caseClassConstant(new ClassConstant(constStr, null)); // [KK] type not needed in old soot
    }
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {
    if (isReflectionNewInstance(expr)) {
      NewInstanceNode newInstanceNode = pag.getOrCreateNewInstanceNode(expr, rtObject, method);
      expr.getBase().accept(this);
      Node srcNode = getNode();
      intraPag.addEdge(srcNode, newInstanceNode);
      setResult(newInstanceNode);
    } else {
      throw new RuntimeException("Unhandled case of JVirtualInvokeExpr");
    }
  }
}
