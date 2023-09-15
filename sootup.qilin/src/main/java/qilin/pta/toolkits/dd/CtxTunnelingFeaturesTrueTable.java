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

package qilin.pta.toolkits.dd;

import qilin.util.PTAUtils;
import soot.*;
import soot.jimple.*;

/*
 * features and formulas used in "Precise and Scalable Points-to Analysis via Data-Driven
 * Context Tunneling" (OOPSLA 2018).
 * */
public class CtxTunnelingFeaturesTrueTable {
    private final boolean[] f = new boolean[24];

    public CtxTunnelingFeaturesTrueTable(SootMethod sm) {
        String sig = sm.getSignature();
        // the 10 atomic signature features.
        this.f[1] = sig.contains("java");
        this.f[2] = sig.contains("lang");
        this.f[3] = sig.contains("sun");
        this.f[4] = sig.contains("()");
        this.f[5] = sig.contains("void");
        this.f[6] = sig.contains("security");
        this.f[7] = sig.contains("int");
        this.f[8] = sig.contains("util");
        this.f[9] = sig.contains("String");
        this.f[10] = sig.contains("init");
        // 13 additional features (a bit complex).
        this.f[11] = containedInNestedClass(sm);
        this.f[12] = sm.getParameterCount() > 1;

        Body body = PTAUtils.getMethodBody(sm);
        this.f[15] = body.getLocalCount() > 0;
        int heapAllocCnt = 0;
        for (Unit unit : body.getUnits()) {
            if (unit instanceof AssignStmt assignStmt) {
                Value left = assignStmt.getLeftOp();
                if (left instanceof Local) {
                    Value right = assignStmt.getRightOp();
                    if (right instanceof Local) {
                        this.f[14] = true;
                    } else if (right instanceof NewExpr || right instanceof NewArrayExpr || right instanceof NewMultiArrayExpr) {
                        heapAllocCnt++;
                    } else if (right instanceof InvokeExpr) {
                        if (right instanceof StaticInvokeExpr) {
                            this.f[17] = true;
                        } else if (right instanceof VirtualInvokeExpr || right instanceof InterfaceInvokeExpr) {
                            this.f[18] = true;
                        }
                    } else if (right instanceof ArrayRef) {
                        this.f[13] = true;
                    }
                } else if (left instanceof InstanceFieldRef) {
                    this.f[16] = true;
                }
            } else if (unit instanceof InvokeStmt invokeStmt) {
                InvokeExpr expr = invokeStmt.getInvokeExpr();
                if (expr instanceof StaticInvokeExpr) {
                    this.f[17] = true;
                } else if (expr instanceof VirtualInvokeExpr || expr instanceof InterfaceInvokeExpr) {
                    this.f[18] = true;
                }
            }

        }
        this.f[19] = sm.isStatic();
        this.f[20] = heapAllocCnt == 1;
        this.f[21] = sig.contains("Object");
        this.f[22] = heapAllocCnt >= 1; // note, the original implementation is >=1 not > 1 which is conflict with the paper.
        this.f[23] = sm.getDeclaringClass().getMethods().size() > 20; // their artifact uses 20 as the threshold.
    }

    public boolean containedInNestedClass(SootMethod sm) {
        SootClass sc = sm.getDeclaringClass();
        return sc.toString().contains("$");
    }

    // corresponding to the Tunneling predicate in its original implementation.
    public boolean cfaFormula2() {
        boolean subF1 = !f[3] && !f[6] && !f[9] && f[14] && f[15] && !f[18] && !f[19] && !f[23];
        boolean subF2 = f[1] && !f[3] && !f[4] && f[7] && !f[9] && f[12] && f[14] && f[15] && !f[16] && !f[19] && !f[21];
        boolean subF3 = f[1] && !f[2] && !f[3] && !f[6] && !f[9] && f[11] && !f[13] && f[14] && f[15] && !f[16]
                && !f[17] && !f[19] && !f[20] && !f[21] && !f[22] && !f[23];
        return subF1 || subF2 || subF3;
    }

    // corresponding to the TunnelingM in its original implementation.
    public boolean cfaFormula1() {
        boolean subF1 = f[1] && !f[2] && !f[3] && f[4] && f[5] && !f[6] && !f[7] && f[8] && f[10] && !f[11]
                && !f[12] && !f[13] && f[14] && f[15] && !f[16] && !f[17] && !f[18] && !f[19] && !f[20] && !f[21] && !f[22] && f[23];

        boolean subF2 = !f[6] && f[8] && !f[10] && !f[11] && f[14] && f[15] && !f[16] && !f[17] && !f[18] && !f[19] && !f[20] && !f[22];

        boolean subF3 = f[1] && f[2] && !f[3] && !f[4] && !f[6] && f[8] && !f[9] && !f[10] && !f[11] && f[12] && f[14]
                && f[15] && !f[16] && f[17] && f[18] && f[19] && !f[20] && f[21] && !f[22] && f[23];
        return subF1 || subF2 || subF3;
    }

    public boolean objFormula2() {
        boolean subF1 = f[1] && !f[3] && !f[6] && !f[9] && f[11] && f[14] && f[15] && !f[19];
        boolean subF2 = f[1] && f[2] && f[3] && !f[4] && f[5] && f[6] && !f[7] && !f[8] && f[9] && f[10] && !f[11]
                && f[12] && !f[13] && f[14] && f[15] && f[16] && f[17] && f[18] && !f[19] && !f[20] && !f[21] && f[22] && f[23];
        return subF1 || subF2;
    }

    public boolean objFormula1() {
        boolean subF1 = !f[2] && !f[3] && !f[4] && f[5] && !f[6] && !f[7] && !f[8] && !f[9] && f[10] && !f[11] && !f[12]
                && !f[13] && f[14] && f[15] && f[16] && !f[17] && !f[18] && !f[19] && f[20] && !f[21] && f[22] && !f[23];

        boolean subF2 = f[1] && !f[2] && !f[3] && f[4] && !f[5] && !f[6] && !f[7] && !f[9] && !f[10] && !f[11] && !f[12]
                && !f[13] && f[14] && f[15] && !f[16] && !f[17] && !f[18] && !f[19] && f[20] && !f[21] && !f[23];

        boolean subF3 = f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && f[7] && f[8] && !f[9] && f[10] && !f[11]
                && f[12] && !f[13] && f[14] && f[15] && f[16] && !f[17] && !f[18] && !f[19] && f[20] && !f[21] && f[22] && !f[23];

        boolean subF4 = f[1] && f[2] && !f[3] && !f[4] && !f[5] && !f[6] && !f[7] && f[8] && !f[10] && !f[11] && !f[12]
                && !f[13] && f[14] && f[15] && !f[16] && !f[17] && !f[18] && f[21] && f[23];
        return subF1 || subF2 || subF3 || subF4;
    }

    public boolean typeFormula2() {
        boolean subF1 = f[1] && !f[3] && !f[6] && !f[9] && f[11] && f[14] && f[15] && !f[19] && !f[23];
        boolean subF2 = f[1] && f[2] && !f[5] && !f[7] && !f[8] && f[9] && !f[10] && !f[11] && f[14]
                && f[15] && !f[16] && f[17] && !f[19] && !f[20] && f[22] && !f[23];
        boolean subF3 = f[3] && f[5] && !f[6] && !f[7] && !f[8] && !f[9] && !f[11] && !f[13] && f[14] && f[15]
                && f[18] && !f[19] && !f[20] && !f[22] && f[23];

        boolean subF4 = f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && !f[7] && !f[8] && !f[9] && f[10]
                && !f[11] && f[14] && f[15] && f[16] && !f[17] && !f[19] && !f[20] && !f[21];

        boolean subF5 = !f[1] && !f[2] && !f[3] && !f[4] && !f[6] && f[7] && !f[9] && !f[11] && f[12]
                && !f[13] && f[14] && f[15] && !f[17] && !f[19] && !f[20] && !f[21] && !f[23];
        return subF1 || subF2 || subF3 || subF4 || subF5;
    }

    public boolean typeFormula1() {
        boolean subF1 = f[1] && !f[2] && !f[3] && !f[6] && !f[7] && f[8] && !f[9] && !f[10] && !f[11]
                && !f[12] && f[14] && f[15] && !f[16] && !f[17] && !f[19] && f[20] && f[22];

        boolean subF2 = f[1] && !f[2] && !f[3] && f[4] && f[5] && !f[6] && !f[7] && f[8] && !f[9] && f[10] && !f[11]
                && !f[12] && !f[13] && !f[14] && f[15] && !f[16] && !f[17] && !f[18] && !f[19] && !f[20] && !f[21] && f[22] && f[23];

        boolean subF3 = f[1] && f[2] && !f[3] && !f[4] && f[5] && !f[6] && !f[7] && !f[8] && !f[9] && f[10] && !f[11]
                && f[12] && !f[13] && f[14] && f[15] && f[16] && !f[17] && !f[18] && !f[19] && !f[20] && !f[21] && f[22] && f[23];

        boolean subF4 = f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && f[7] && f[8] && !f[9] && f[10] && !f[11]
                && !f[12] && !f[13] && f[14] && f[15] && f[16] && !f[17] && !f[21] && !f[18] && f[20] && f[22] && !f[23];

        boolean subF5 = !f[1] && !f[2] && !f[3] && !f[4] && !f[5] && !f[6] && f[7] && !f[8] && !f[9] && !f[10] && !f[11]
                && f[12] && !f[13] && f[14] && f[15] && !f[16] && !f[17] && f[18] && !f[19] && !f[20] && !f[21] && f[22] && f[23];

        boolean subF6 = f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && f[7] && !f[9]
                && f[8] && f[10] && !f[11] && f[12] && !f[13] && f[14] && f[15] && f[16]
                && !f[17] && !f[18] && !f[19] && f[20] && !f[21] && f[22] && !f[23];

        boolean subF7 = !f[1] && !f[2] && f[3] && f[4] && f[5] && !f[6] && !f[7] && !f[8]
                && !f[9] && f[10] && !f[11] && !f[12] && !f[13] && !f[14] && f[15] && !f[16]
                && !f[17] && !f[18] && !f[19] && !f[20] && !f[21] && f[22] && !f[23];

        boolean subF8 = !f[4] && !f[9] && !f[10] && !f[11] && f[13] && f[14] && f[15] && !f[16] && f[18] && f[22];

        boolean subF9 = f[1] && !f[2] && !f[3] && f[4] && !f[5] && !f[6] && !f[7] && f[8] && !f[9] && !f[10] && !f[11]
                && !f[12] && !f[13] && f[14] && f[15] && !f[16] && !f[17] && f[18] && !f[19] && !f[20] && !f[21] && f[22] && f[23];

        boolean subF10 = !f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && !f[7] && !f[9]
                && !f[11] && !f[12] && !f[13] && f[14] && f[15] && !f[16] && !f[17] && !f[19] && !f[21] && f[22];
        return subF1 || subF2 || subF3 || subF4 || subF5 || subF6 || subF7 || subF8 || subF9 || subF10;
    }

    public boolean hybridFormula2() {
        boolean subF1 = f[1] && !f[3] && !f[6] && !f[9] && f[11] && f[15] && !f[23];
        boolean subF2 = f[1] && !f[3] && !f[4] && !f[6] && f[8] && !f[9] && !f[10]
                && !f[11] && f[14] && f[15] && !f[16] && f[19];
        boolean subF3 = f[2] && !f[3] && f[4] && !f[5] && !f[6] && f[8] && !f[9] && !f[11] && !f[13]
                && f[14] && f[15] && !f[16] && f[17] && !f[18] && !f[19] && !f[20] && f[23];
        return subF1 || subF2 || subF3;
    }

    public boolean hybridFormula1() {
        boolean subF1 = f[1] && !f[2] && !f[3] && f[4] && !f[6] && !f[8] && !f[9] && !f[11] && !f[13]
                && f[14] && f[15] && !f[17] && !f[19] && f[20] && !f[21] && f[22];
        boolean subF2 = f[1] && !f[2] && !f[3] && !f[4] && f[5] && !f[6] && f[7] && f[8] && !f[9] && f[10] && !f[11] &&
                !f[13] && !f[14] && f[15] && f[16] && !f[17] && !f[18] && !f[19] && f[20] && !f[21] && f[22] && !f[23];
        return subF1 || subF2;
    }
}
