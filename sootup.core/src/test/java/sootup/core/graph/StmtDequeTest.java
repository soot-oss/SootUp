package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;

/** random ops on {@link MutableBasicBlockImpl.StmtDeque} against an ArrayList */
public class StmtDequeTest {

  @Test
  void matchesArrayList() {
    for (long seed = 0; seed < 50; seed++) {
      Random rnd = new Random(seed);
      MutableBasicBlockImpl.StmtDeque deque = new MutableBasicBlockImpl.StmtDeque();
      List<Stmt> model = new ArrayList<>();
      for (int step = 0; step < 2000; step++) {
        int n = model.size();
        int op = rnd.nextInt(6);
        String ctx = "seed " + seed + " step " + step + " op " + op;
        if (op == 0 || n == 0) {
          // insert a run at a position biased to the ends
          int i = biased(rnd, n + 1);
          List<Stmt> in = new ArrayList<>();
          for (int k = rnd.nextInt(4) + 1; k > 0; k--) {
            in.add(new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo()));
          }
          deque.addAll(i, in);
          model.addAll(i, in);
        } else if (op == 1) {
          Stmt s = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
          int i = biased(rnd, n + 1);
          deque.add(i, s);
          model.add(i, s);
        } else if (op == 2) {
          int i = biased(rnd, n);
          assertEquals(model.remove(i), deque.remove(i), ctx);
        } else if (op == 3) {
          int from = rnd.nextInt(n);
          int to = from + rnd.nextInt(Math.min(4, n - from) + 1);
          deque.subList(from, to).clear();
          model.subList(from, to).clear();
        } else if (op == 4) {
          int i = rnd.nextInt(n);
          Stmt s = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
          deque.set(i, s);
          model.set(i, s);
        } else {
          // keep the list from growing without bound
          while (model.size() > 64) {
            model.remove(0);
            deque.remove(0);
          }
        }
        assertEquals(model, deque, ctx);
        for (int j = 0; j < model.size(); j++) {
          assertEquals(j, deque.identityIndexOf(model.get(j)), ctx + " index of " + j);
        }
        assertEquals(
            -1, deque.identityIndexOf(new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo())), ctx);
      }
    }
  }

  private static int biased(Random rnd, int bound) {
    switch (rnd.nextInt(3)) {
      case 0:
        return 0;
      case 1:
        return bound - 1;
      default:
        return rnd.nextInt(bound);
    }
  }
}
