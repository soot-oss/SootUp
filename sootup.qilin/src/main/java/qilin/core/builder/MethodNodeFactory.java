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
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JNewArrayExpr;

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
        } else if (v instanceof CastExpr castExpr) {
            return caseCastExpr(castExpr);
        } else if (v instanceof NewExpr ne) {
            return caseNewExpr(ne);
        } else if (v instanceof StaticFieldRef sfr) {
            return caseStaticFieldRef(sfr);
        } else if (v instanceof NewArrayExpr nae) {
            return caseNewArrayExpr(nae);
        } else if (v instanceof ArrayRef ar) {
            return caseArrayRef(ar);
        } else if (v instanceof ClassConstant cc) {
            return caseClassConstant(cc);
        } else if (v instanceof StringConstant sc) {
            return caseStringConstant(sc);
        } else if (v instanceof CaughtExceptionRef cef) {
            return caseCaughtExceptionRef(cef);
        } else if (v instanceof ParameterRef pr) {
            return caseParameterRef(pr);
        } else if (v instanceof NullConstant nc) {
            return caseNullConstant(nc);
        } else if (v instanceof InstanceFieldRef ifr) {
            return caseInstanceFieldRef(ifr);
        } else if (v instanceof ThisRef) {
            return caseThis();
        } else if (v instanceof NewMultiArrayExpr nmae) {
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
        InvokeExpr ie = s.getInvokeExpr();
        int numArgs = ie.getArgCount();
        for (int i = 0; i < numArgs; i++) {
            Value arg = ie.getArg(i);
            if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant) {
                continue;
            }
            getNode(arg);
        }
        if (s instanceof AssignStmt) {
            Value l = ((AssignStmt) s).getLeftOp();
            if ((l.getType() instanceof RefLikeType)) {
                getNode(l);
            }
        }
        if (ie instanceof InstanceInvokeExpr) {
            getNode(((InstanceInvokeExpr) ie).getBase());
        }
    }

    private void resolveClinit(StaticFieldRef staticFieldRef) {
        PTAUtils.clinitsOf(staticFieldRef.getField().getDeclaringClass()).forEach(mpag::addTriggeredClinit);
    }

    /**
     * Adds the edges required for this statement to the graph.
     */
    private void handleIntraStmt(Stmt s) {
        s.apply(new AbstractStmtSwitch<>() {
            public void caseAssignStmt(AssignStmt as) {
                Value l = as.getLeftOp();
                Value r = as.getRightOp();
                if (l instanceof StaticFieldRef) {
                    resolveClinit((StaticFieldRef) l);
                } else if (r instanceof StaticFieldRef) {
                    resolveClinit((StaticFieldRef) r);
                }

                if (!(l.getType() instanceof RefLikeType))
                    return;
                // check for improper casts, with mal-formed code we might get
                // l = (refliketype)int_type, if so just return
                if (r instanceof CastExpr && (!(((CastExpr) r).getOp().getType() instanceof RefLikeType))) {
                    return;
                }

                if (!(r.getType() instanceof RefLikeType))
                    throw new RuntimeException("Type mismatch in assignment (rhs not a RefLikeType) " + as
                            + " in method " + method.getSignature());
                Node dest = getNode(l);
                Node src = getNode(r);
                mpag.addInternalEdge(src, dest);
            }

            public void caseReturnStmt(ReturnStmt rs) {
                if (!(rs.getOp().getType() instanceof RefLikeType))
                    return;
                Node retNode = getNode(rs.getOp());
                mpag.addInternalEdge(retNode, caseRet());
            }

            public void caseIdentityStmt(IdentityStmt is) {
                if (!(is.getLeftOp().getType() instanceof RefLikeType)) {
                    return;
                }
                Node dest = getNode(is.getLeftOp());
                Node src = getNode(is.getRightOp());
                mpag.addInternalEdge(src, dest);
            }

            public void caseThrowStmt(ThrowStmt ts) {
                if (!CoreConfig.v().getPtaConfig().preciseExceptions) {
                    mpag.addInternalEdge(getNode(ts.getOp()), getNode(PTAScene.v().getFieldGlobalThrow()));
                }
            }
        });
    }

    private VarNode caseLocal(Local l) {
        return pag.makeLocalVarNode(l, l.getType(), method);
    }

    private AllocNode caseNewArrayExpr(NewArrayExpr nae) {
        return pag.makeAllocNode(nae, nae.getType(), method);
    }

    private AllocNode caseNewExpr(NewExpr ne) {
        SootClass cl = PTAScene.v().loadClassAndSupport(ne.getType().toString());
        PTAUtils.clinitsOf(cl).forEach(mpag::addTriggeredClinit);
        return pag.makeAllocNode(ne, ne.getType(), method);
    }

    private FieldRefNode caseInstanceFieldRef(InstanceFieldRef ifr) {
        SootField sf = ifr.getField();
        if (sf == null) {
            sf = new SootField(ifr.getFieldRef().name(), ifr.getType(), Modifier.PUBLIC);
            sf.setNumber(Scene.v().getFieldNumberer().size());
            Scene.v().getFieldNumberer().add(sf);
            System.out.println("Warnning:" + ifr + " is resolved to be a null field in Scene.");
        }
        return pag.makeFieldRefNode(pag.makeLocalVarNode(ifr.getBase(), ifr.getBase().getType(), method), new Field(sf));
    }

    private VarNode caseNewMultiArrayExpr(NewMultiArrayExpr nmae) {
        ArrayType type = (ArrayType) nmae.getType();
        int pos = 0;
        AllocNode prevAn = pag.makeAllocNode(new JNewArrayExpr(type, nmae.getSize(pos)), type, method);
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
            Value sizeVal;
            if (pos < nmae.getSizeCount()) {
                sizeVal = nmae.getSize(pos);
            } else {
                sizeVal = IntConstant.v(1);
            }
            AllocNode an = pag.makeAllocNode(new JNewArrayExpr(type, sizeVal), type, method);
            VarNode vn = pag.makeLocalVarNode(an.getNewExpr(), an.getType(), method);
            mpag.addInternalEdge(an, vn); // new
            mpag.addInternalEdge(vn, pag.makeFieldRefNode(prevVn, ArrayElement.v())); // store
            prevVn = vn;
        }
        return ret;
    }

    private VarNode caseCastExpr(CastExpr ce) {
        Node opNode = getNode(ce.getOp());
        VarNode castNode = pag.makeLocalVarNode(ce, ce.getCastType(), method);
        mpag.addInternalEdge(opNode, castNode);
        return castNode;
    }

    public VarNode caseThis() {
        Type type = method.isStatic() ? RefType.v("java.lang.Object") : method.getDeclaringClass().getType();
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

    private Node caseCaughtExceptionRef(CaughtExceptionRef cer) {
        if (CoreConfig.v().getPtaConfig().preciseExceptions) {
            // we model caughtException expression as an local assignment.
            return pag.makeLocalVarNode(cer, cer.getType(), method);
        } else {
            return getNode(PTAScene.v().getFieldGlobalThrow());
        }
    }

    private FieldRefNode caseArrayRef(ArrayRef ar) {
        return caseArray(caseLocal((Local) ar.getBase()));
    }

    private VarNode caseParameterRef(ParameterRef pr) {
        return caseParm(pr.getIndex());
    }

    private VarNode caseStaticFieldRef(StaticFieldRef sfr) {
        return pag.makeGlobalVarNode(sfr.getField(), sfr.getField().getType());
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
