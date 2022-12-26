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
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

/*
 * features and formulas used in "Data-driven context-sensitivity for Points-to Analysis (OOPSLA 2017)"
 * */
public class FeaturesTrueTable {
    private final boolean[] features = new boolean[26];

    public FeaturesTrueTable(SootMethod sm) {
        String sig = sm.getSignature();
        this.features[1] = sig.contains("java");
        this.features[2] = sig.contains("lang");
        this.features[3] = sig.contains("sun");
        this.features[4] = sig.contains("()");
        this.features[5] = sig.contains("void");
        this.features[6] = sig.contains("security");
        this.features[7] = sig.contains("int");
        this.features[8] = sig.contains("util");
        this.features[9] = sig.contains("String");
        this.features[10] = sig.contains("init");
        Body body = PTAUtils.getMethodBody(sm);
        for (Unit unit : body.getUnits()) {
            if (unit instanceof AssignStmt) {
                this.features[11] = true;
            } else if (unit instanceof IdentityStmt) {
                this.features[12] = true;
            } else if (unit instanceof InvokeStmt) {
                this.features[13] = true;
            } else if (unit instanceof ReturnStmt) {
                this.features[14] = true;
            } else if (unit instanceof ThrowStmt) {
                this.features[15] = true;
            } else if (unit instanceof BreakpointStmt) {
                this.features[16] = true;
            } else if (unit instanceof EnterMonitorStmt) {
                this.features[17] = true;
            } else if (unit instanceof ExitMonitorStmt) {
                this.features[18] = true;
            } else if (unit instanceof GotoStmt) {
                this.features[19] = true;
            } else if (unit instanceof IfStmt) {
                this.features[20] = true;
            } else if (unit instanceof LookupSwitchStmt) {
                this.features[21] = true;
            } else if (unit instanceof NopStmt) {
                this.features[22] = true;
            } else if (unit instanceof RetStmt) {
                this.features[23] = true;
            } else if (unit instanceof ReturnVoidStmt) {
                this.features[24] = true;
            } else if (unit instanceof TableSwitchStmt) {
                this.features[25] = true;
            }
        }
    }

    // the following two are for hybrid 2-object-sensitive pointer analysis.
    public boolean hybrid2objFormula2() {
        return features[1] && !features[3] && !features[6] && features[8] && !features[9] && !features[16]
                && !features[17] && !features[18] && !features[19] && !features[20] && !features[21]
                && !features[22] && !features[23] && !features[24] && !features[25];
    }

    public boolean hybrid2objFormula1() {
        boolean subF1 = features[1] && !features[3] && !features[4] && features[6] && !features[7] && !features[8] && !features[9] &&
                !features[15] && !features[16] && !features[17] && !features[18] && !features[19] && !features[20] && !features[21]
                && !features[22] && !features[23] && !features[24] && !features[25];
        boolean subF2 = !features[3] && !features[4] && !features[7] && !features[8] && !features[9] && features[10] && features[11] &&
                features[12] && features[13] && !features[16] && !features[17] && !features[18] && !features[19] && !features[20] &&
                !features[21] && !features[22] && !features[23] && !features[24] && !features[25];
        boolean subF3 = !features[3] && !features[9] && features[13] && features[14] && features[15] && !features[16] && !features[17]
                && !features[18] && !features[19] && !features[20] && !features[21] && !features[22] && !features[23] && !features[24] && !features[25];
        boolean subF4 = features[1] && features[2] && !features[3] && features[4] && !features[5] && !features[6] && !features[7] && !features[8]
                && !features[9] && !features[10] && !features[13] && !features[15] && !features[16] && !features[17] && !features[18]
                && !features[19] && !features[20] && !features[21] && !features[22] && !features[23] && !features[24] && !features[25];
        return subF1 || subF2 || subF3 || subF4;
    }

    public boolean twoObjFormula2() {
        return hybrid2objFormula2();
    }

    public boolean twoObjFormula1() {
        boolean subF1 = features[1] && features[2] && !features[3] && !features[6] && !features[7] && !features[8] && !features[9] &&
                !features[16] && !features[17] && !features[18] && !features[19] && !features[20] && !features[21] && !features[22] &&
                !features[23] && !features[24] && !features[25];
        boolean subF2 = !features[1] && !features[2] && features[5] && features[8] && !features[9] && features[11] && features[12] && !features[14]
                && !features[15] && !features[16] && !features[17] && !features[18] && !features[19] && !features[20] && !features[21] && !features[22] &&
                !features[23] && !features[24] && !features[25];
        boolean subF3 = !features[3] && !features[4] && !features[7] && !features[8] && !features[9] && features[10] && features[11] && features[12] &&
                !features[16] && !features[17] && !features[18] && !features[19] && !features[20] && !features[21] && !features[22] &&
                !features[23] && !features[24] && !features[25];
        return subF1 || subF2 || subF3;
    }

    public boolean twoTypeFormula2() {
        return hybrid2objFormula2();
    }

    public boolean twoTypeFormula1() {
        return features[1] && features[2] && !features[3] && !features[6] && !features[7] && !features[8] && !features[9] && !features[15] &&
                !features[16] && !features[17] && !features[18] && !features[19] && !features[20] && !features[21] && !features[22] &&
                !features[23] && !features[24] && !features[25];
    }

    public boolean twoCFAFormula2() {
        return features[1] && !features[6] && !features[7] && features[11] && features[12] && features[13] && !features[16] && !features[17] && !features[18]
                && !features[19] && !features[20] && !features[21] && !features[22] && !features[23] && !features[24] && !features[25];
    }

    public boolean twoCFAFormula1() {
        return features[1] && features[2] && !features[7] && !features[16] && !features[17] && !features[18]
                && !features[19] && !features[20] && !features[21] && !features[22] && !features[23] && !features[24] && !features[25];
    }
}
