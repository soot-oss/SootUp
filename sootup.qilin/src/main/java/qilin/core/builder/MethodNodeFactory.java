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

import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.core.PointsToAnalysis;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import qilin.util.Pair;
import sootup.core.jimple.basic.Local;
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
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Modifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

/**
 * @author Ondrej Lhotak
 */
public class MethodNodeFactory {
    protected PAG pag;
    protected MethodPAG mpag;
    protected SootMethod method;

    public MethodNodeFactory(PAG pag, MethodPAG mpag) {
        this.pag = pag;
        this.mpag = mpag;
        method = mpag.getMethod();
    }

    public Node getNode(Value v) {
        if (v instanceof Local l) {
            return caseLocal(l);
        } else if (v instanceof JCastExpr castExpr) {
            return caseCastExpr(castExpr);
        } else if (v instanceof JNewExpr ne) {
            return caseNewExpr(ne);
        } else if (v instanceof JStaticFieldRef sfr) {
            return caseStaticFieldRef(sfr);
        } else if (v instanceof JNewArrayExpr nae) {
            return caseNewArrayExpr(nae);
        } else if (v instanceof JArrayRef ar) {
            return caseArrayRef(ar);
        } else if (v instanceof ClassConstant cc) {
            return caseClassConstant(cc);
        } else if (v instanceof StringConstant sc) {
            return caseStringConstant(sc);
        } else if (v instanceof JCaughtExceptionRef cef) {
            return caseCaughtExceptionRef(cef);
        } else if (v instanceof JParameterRef pr) {
            return caseParameterRef(pr);
        } else if (v instanceof NullConstant nc) {
            return caseNullConstant(nc);
        } else if (v instanceof JInstanceFieldRef ifr) {
            return caseInstanceFieldRef(ifr);
        } else if (v instanceof JThisRef) {
            return caseThis();
        } else if (v instanceof JNewMultiArrayExpr nmae) {
            return caseNewMultiArrayExpr(nmae);
        }
        System.out.println(v + ";;" + v.getClass());
        return null;
    }

    /**
     * Adds the edges required for this statement to the graph.
     */
    final public void handleStmt(Stmt s) {
        if (s.containsInvokeExpr()) {
            mpag.addCallStmt(s);
            handleInvokeStmt(s);
        } else {
            handleIntraStmt(s);
        }
    }

    /**
     * Adds the edges required for this statement to the graph. Add throw stmt if
     * the invoke method throws an Exception.
     */
    protected void handleInvokeStmt(Stmt s) {
        AbstractInvokeExpr ie = s.getInvokeExpr();
        int numArgs = ie.getArgCount();
        for (int i = 0; i < numArgs; i++) {
            Value arg = ie.getArg(i);
            if (!(arg.getType() instanceof ReferenceType) || arg instanceof NullConstant) {
                continue;
            }
            getNode(arg);
        }
        if (s instanceof JAssignStmt assignStmt) {
            Value l = assignStmt.getLeftOp();
            if ((l.getType() instanceof ReferenceType)) {
                getNode(l);
            }
        }
        if (ie instanceof AbstractInstanceInvokeExpr iie) {
            getNode(iie.getBase());
        }
    }

    private void resolveClinit(JStaticFieldRef staticFieldRef) {
        PTAUtils.clinitsOf(staticFieldRef.getField().getDeclaringClass()).forEach(mpag::addTriggeredClinit);
    }

    /**
     * Adds the edges required for this statement to the graph.
     */
    private void handleIntraStmt(Stmt s) {
        s.apply(new AbstractStmtSwitch<>() {
            public void caseAssignStmt(JAssignStmt as) {
                Value l = as.getLeftOp();
                Value r = as.getRightOp();
                if (l instanceof JStaticFieldRef sfr) {
                    resolveClinit(sfr);
                } else if (r instanceof JStaticFieldRef sfr) {
                    resolveClinit(sfr);
                }

                if (!(l.getType() instanceof ReferenceType))
                    return;
                // check for improper casts, with mal-formed code we might get
                // l = (refliketype)int_type, if so just return
                if (r instanceof JCastExpr && (!(((JCastExpr) r).getOp().getType() instanceof ReferenceType))) {
                    return;
                }

                if (!(r.getType() instanceof ReferenceType))
                    throw new RuntimeException("Type mismatch in assignment (rhs not a RefLikeType) " + as
                            + " in method " + method.getSignature());
                Node dest = getNode(l);
                Node src = getNode(r);
                mpag.addInternalEdge(src, dest);
            }

            public void caseReturnStmt(JReturnStmt rs) {
                if (!(rs.getOp().getType() instanceof ReferenceType))
                    return;
                Node retNode = getNode(rs.getOp());
                mpag.addInternalEdge(retNode, caseRet());
            }

            public void caseIdentityStmt(JIdentityStmt is) {
                if (!(is.getLeftOp().getType() instanceof ReferenceType)) {
                    return;
                }
                Node dest = getNode(is.getLeftOp());
                Node src = getNode(is.getRightOp());
                mpag.addInternalEdge(src, dest);
            }

            public void caseThrowStmt(JThrowStmt ts) {
                if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
                    mpag.addInternalEdge(getNode(ts.getOp()), getNode(PTAScene.v().getFieldGlobalThrow()));
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
        SootClass cl = PTAScene.v().loadClassAndSupport(ne.getType().toString());
        PTAUtils.clinitsOf(cl).forEach(mpag::addTriggeredClinit);
        return pag.makeAllocNode(ne, ne.getType(), method);
    }

    private FieldRefNode caseInstanceFieldRef(JInstanceFieldRef ifr) {
        SootField sf = ifr.getField();
//        if (sf == null) {
//            sf = new SootField(ifr.getFieldSignature(), ifr.getType(), Modifier.PUBLIC);
//            sf.setNumber(Scene.v().getFieldNumberer().size());
//            Scene.v().getFieldNumberer().add(sf);
//            System.out.println("Warnning:" + ifr + " is resolved to be a null field in Scene.");
//        }
        return pag.makeFieldRefNode(pag.makeLocalVarNode(ifr.getBase(), ifr.getBase().getType(), method), new Field(sf));
    }

    private VarNode caseNewMultiArrayExpr(JNewMultiArrayExpr nmae) {
        ArrayType type = (ArrayType) nmae.getType();
        int pos = 0;
        AllocNode prevAn = pag.makeAllocNode(new JNewArrayExpr(type, nmae.getSize(pos)), type, method);
        VarNode prevVn = pag.makeLocalVarNode(prevAn.getNewExpr(), prevAn.getType(), method);
        mpag.addInternalEdge(prevAn, prevVn); // new
        VarNode ret = prevVn;
        while (true) {
            Type t = type.getArrayElementType();
            if (!(t instanceof ArrayType)) {
                break;
            }
            type = (ArrayType) t;
            ++pos;
            Value sizeVal;
            if (pos < nmae.getSizeCount()) {
                sizeVal = nmae.getSize(pos);
            } else {
                sizeVal = IntConstant.getInstance(1);
            }
            AllocNode an = pag.makeAllocNode(new JNewArrayExpr(type, sizeVal), type, method);
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
        Type type = method.isStatic() ? RefType.v("java.lang.Object") : method.getDeclaringClassType();
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.THIS_NODE), type, method);
        ret.setInterProcTarget();
        return ret;
    }

    public VarNode caseParm(int index) {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, index), method.getParameterType(index), method);
        ret.setInterProcTarget();
        return ret;
    }

    public VarNode caseRet() {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.RETURN_NODE), method.getReturnType(), method);
        ret.setInterProcSource();
        return ret;
    }

    public VarNode caseMethodThrow() {
        VarNode ret = pag.makeLocalVarNode(new Parm(method, PointsToAnalysis.THROW_NODE), RefType.v("java.lang.Throwable"), method);
        ret.setInterProcSource();
        return ret;
    }

    final public FieldRefNode caseArray(VarNode base) {
        return pag.makeFieldRefNode(base, ArrayElement.v());
    }

    private Node caseCaughtExceptionRef(JCaughtExceptionRef cer) {
        if (CoreConfig.v().getPtaConfig().preciseExceptions) {
            // we model caughtException expression as an local assignment.
            return pag.makeLocalVarNode(cer, cer.getType(), method);
        } else {
            return getNode(PTAScene.v().getFieldGlobalThrow());
        }
    }

    private FieldRefNode caseArrayRef(JArrayRef ar) {
        return caseArray(caseLocal((Local) ar.getBase()));
    }

    private VarNode caseParameterRef(JParameterRef pr) {
        return caseParm(pr.getIndex());
    }

    private VarNode caseStaticFieldRef(JStaticFieldRef sfr) {
        return pag.makeGlobalVarNode(sfr.getField(), sfr.getType());
    }

    private Node caseNullConstant(NullConstant nr) {
        return null;
    }

    private VarNode caseStringConstant(StringConstant sc) {
        AllocNode stringConstantNode = pag.makeStringConstantNode(sc);
        VarNode stringConstantVar = pag.makeGlobalVarNode(sc, RefType.v("java.lang.String"));
        mpag.addInternalEdge(stringConstantNode, stringConstantVar);
        VarNode vn = pag.makeLocalVarNode(new Pair<>(method, sc), RefType.v("java.lang.String"), method);
        mpag.addInternalEdge(stringConstantVar, vn);
        return vn;
    }

    public LocalVarNode makeInvokeStmtThrowVarNode(Stmt invoke, SootMethod method) {
        return pag.makeLocalVarNode(invoke, RefType.v("java.lang.Throwable"), method);
    }

    final public VarNode caseClassConstant(ClassConstant cc) {
        AllocNode classConstant = pag.makeClassConstantNode(cc);
        VarNode classConstantVar = pag.makeGlobalVarNode(cc, RefType.v("java.lang.Class"));
        mpag.addInternalEdge(classConstant, classConstantVar);
        VarNode vn = pag.makeLocalVarNode(new Pair<>(method, cc), RefType.v("java.lang.Class"), method);
        mpag.addInternalEdge(classConstantVar, vn);
        return vn;
    }


}
