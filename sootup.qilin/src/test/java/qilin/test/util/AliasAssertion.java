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

import java.util.Objects;
import qilin.core.PTA;
import qilin.core.pag.LocalVarNode;
import qilin.util.PagQueries;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

public class AliasAssertion implements IAssertion {
  private final PTA pta;
  private final SootMethod sm;
  private final Stmt stmt;
  private final Value va;
  private final Value vb;
  private final boolean groundTruth;

  public AliasAssertion(
      PTA pta, SootMethod sm, Stmt stmt, Value va, Value vb, boolean groundTruth) {
    this.pta = pta;
    this.sm = sm;
    this.stmt = stmt;
    this.va = va;
    this.vb = vb;
    this.groundTruth = groundTruth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AliasAssertion that = (AliasAssertion) o;
    return groundTruth == that.groundTruth
        && Objects.equals(sm, that.sm)
        && Objects.equals(stmt, that.stmt)
        && Objects.equals(va, that.va)
        && Objects.equals(vb, that.vb);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sm, stmt, va, vb, groundTruth);
  }

  @Override
  public boolean check() {
    if (DEBUG && va instanceof Local a && vb instanceof Local b) {
      printPts(a, "va");
      printPts(b, "vb");
    }
    return pta.isMayAlias(sm, va, vb) == groundTruth;
  }

  @Override
  public boolean isSoundnessCritical() {
    // "may-alias" (groundTruth=true) must hold under any sound analysis, however imprecise.
    // "not-alias" (groundTruth=false) is a precision claim a coarser/selective analysis can
    // legitimately miss.
    return groundTruth;
  }

  private static final boolean DEBUG = true;

  private void printPts(Local l, String label) {
    LocalVarNode lvn = pta.getPag().findLocalVarNode(sm, l, l.getType());
    System.out.println(label + " points to: " + PagQueries.getNodeLabel(lvn) + lvn);
    PagQueries.printPts(pta, pta.reachingObjects(sm, l).toCIPointsToSet());
  }
}
