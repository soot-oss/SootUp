package sootup.callgraph;

import static sootup.callgraph.GraphBasedCallGraph.*;

import java.util.Comparator;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.LinePosition;
import sootup.core.model.Position;

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

  private static int getLineFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getFirstLine() : Integer.MAX_VALUE;
  }

  private static int getColStartFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getFirstCol() : Integer.MAX_VALUE;
  }

  private static int getColEndFrom(Call c) {
    Position pos = stmtPositionOf(c);
    return pos instanceof LinePosition lp ? lp.getLastCol() : Integer.MAX_VALUE;
  }

  private static Position stmtPositionOf(Call c) {
    InvokableStmt site = c.invokableStmt();
    return site.getPositionInfo().getStmtPosition();
  }
}
