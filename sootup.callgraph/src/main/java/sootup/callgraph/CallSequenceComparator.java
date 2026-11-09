package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Ashik Mogasavara Ravikumar
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

import static sootup.callgraph.GraphBasedCallGraph.*;

import java.util.Comparator;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.LinePosition;
import sootup.core.model.Position;

/**
 * Comparator for ordering {@link Call} objects by their source location.
 *
 * <p>Ordering is performed using three keys (in priority order):
 *
 * <ol>
 *   <li>line number
 *   <li>start column
 *   <li>end column
 * </ol>
 *
 * <p>Calls that lack precise {@link LinePosition} information are treated as having the largest
 * possible line/column values (Integer.MAX_VALUE) so that they are consistently placed after calls
 * that do have source locations.
 */
public class CallSequenceComparator implements Comparator<Call> {

  public CallSequenceComparator() {}

  @Override
  public int compare(Call call1, Call call2) {
    int line1 = getLineFrom(call1), line2 = getLineFrom(call2);
    if (line1 != line2) return Integer.compare(line1, line2);

    int colStart1 = getColStartFrom(call1), colStart2 = getColStartFrom(call2);
    if (colStart1 != colStart2) return Integer.compare(colStart1, colStart2);

    int colEnd1 = getColEndFrom(call1), colEnd2 = getColEndFrom(call2);
    return Integer.compare(colEnd1, colEnd2);
  }

  /**
   * Extract the first line for the call's statement. Returns Integer.MAX_VALUE when the statement
   * position is not a LinePosition so that unknown positions are ordered after known ones.
   */
  private static int getLineFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getFirstLine() : Integer.MAX_VALUE;
  }

  /**
   * Extract the start column for the call's statement. Unknown positions yield Integer.MAX_VALUE.
   */
  private static int getColStartFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getFirstCol() : Integer.MAX_VALUE;
  }

  /** Extract the end column for the call's statement. Unknown positions yield Integer.MAX_VALUE. */
  private static int getColEndFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getLastCol() : Integer.MAX_VALUE;
  }

  /** Helper that returns the Position associated with the call's invoking statement. */
  private static Position stmtPositionOf(Call c) {
    InvokableStmt site = c.invokableStmt();
    return site.getPositionInfo().getStmtPosition();
  }
}
