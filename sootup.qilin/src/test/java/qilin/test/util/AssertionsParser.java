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

package qilin.test.util;

import qilin.core.PTA;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;

import java.util.HashSet;
import java.util.Set;

public class AssertionsParser {
    protected static String mayAliasSig = "<qilin.microben.utils.Assert: void mayAlias(java.lang.Object,java.lang.Object)>";
    protected static String notAliasSig = "<qilin.microben.utils.Assert: void notAlias(java.lang.Object,java.lang.Object)>";

    public static Set<IAssertion> retrieveQueryInfo(PTA pta) {
        Set<IAssertion> aliasAssertionSet = new HashSet<>();
        for (SootMethod sm : pta.getNakedReachableMethods()) {
            if (sm.isNative() || sm.isPhantom()) {
                continue;
            }

//            if (sm.getSignature().equals("<qilin.pta.reflog.DoopRefBug: void main(java.lang.String[])>")) {
//                System.out.println(sm);
//                System.out.println(PTAUtils.getMethodBody(sm));
//                PTAUtils.dumpMPAG(pta, sm);
//                System.out.println("=================================================================");
//            }
            for (Unit unit : PTAUtils.getMethodBody(sm).getUnits()) {
                final Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr ie = stmt.getInvokeExpr();
                    if (ie instanceof StaticInvokeExpr) {
                        final String calleeSig = ie.getMethodRef().getSignature();
                        if (calleeSig.equals(mayAliasSig)) {
                            aliasAssertionSet.add(new AliasAssertion(pta, sm, stmt, ie.getArg(0), ie.getArg(1), true));
                        } else if (calleeSig.equals(notAliasSig)) {
                            aliasAssertionSet.add(new AliasAssertion(pta, sm, stmt, ie.getArg(0), ie.getArg(1), false));
                        }
                    }
                }
            }
        }
        System.out.println("#alias queries: " + aliasAssertionSet.size());
        return aliasAssertionSet;
    }

}
