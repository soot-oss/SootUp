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

import java.util.HashSet;
import java.util.Set;
import qilin.core.PTA;
import qilin.util.PTAUtils;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;

public class AssertionsParser {
  protected static String mayAliasSig =
      "<qilin.microben.utils.Assert: void mayAlias(java.lang.Object,java.lang.Object)>";
  protected static String notAliasSig =
      "<qilin.microben.utils.Assert: void notAlias(java.lang.Object,java.lang.Object)>";

  public static Set<IAssertion> retrieveQueryInfo(PTA pta) {
    Set<IAssertion> aliasAssertionSet = new HashSet<>();
    for (SootMethod sm : pta.getNakedReachableMethods()) {
      if (sm.isNative()) {
        continue;
      }

      //            if (sm.getSignature().equals("<qilin.pta.reflog.DoopRefBug: void
      // main(java.lang.String[])>")) {
      //                System.out.println(sm);
      //                System.out.println(PTAUtils.getMethodBody(sm));
      //                PTAUtils.dumpMPAG(pta, sm);
      //
      // System.out.println("=================================================================");
      //            }
      for (final Stmt stmt : PTAUtils.getMethodBody(sm).getStmts()) {
        if (stmt.containsInvokeExpr()) {
          AbstractInvokeExpr ie = stmt.getInvokeExpr();
          if (ie instanceof JStaticInvokeExpr) {
            final MethodSignature calleeSig = ie.getMethodSignature();
            if (calleeSig.equals(mayAliasSig)) {
              aliasAssertionSet.add(
                  new AliasAssertion(pta, sm, stmt, ie.getArg(0), ie.getArg(1), true));
            } else if (calleeSig.equals(notAliasSig)) {
              aliasAssertionSet.add(
                  new AliasAssertion(pta, sm, stmt, ie.getArg(0), ie.getArg(1), false));
            }
          }
        }
      }
    }
    System.out.println("#alias queries: " + aliasAssertionSet.size());
    return aliasAssertionSet;
  }
}
