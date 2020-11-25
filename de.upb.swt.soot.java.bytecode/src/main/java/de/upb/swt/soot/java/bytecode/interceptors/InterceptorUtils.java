package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import java.util.*;
import javax.annotation.Nonnull;

public class InterceptorUtils {

  /**
   * Get all definition-stmts which define the given local used by the given stmt.
   *
   * @param builder an instance of BodyBuilder whose body contains the given stmt.
   * @param use a local that is used by the given stmt.
   * @param stmt a stmt which uses the given local.
   * @return
   */
  public static List<Stmt> getDefsForLocalUse(Body.BodyBuilder builder, Local use, Stmt stmt) {
    if (!stmt.getUses().contains(use)) {
      throw new RuntimeException(stmt + " doesn't use the local " + use.toString());
    }
    List<Stmt> defStmts = new ArrayList<>();
    Set<Stmt> visited = new HashSet<>();
    StmtGraph graph = builder.getStmtGraph();
    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(stmt);
    while (!queue.isEmpty()) {
      Stmt s = queue.removeFirst();
      visited.add(s);
      for (Stmt pred : graph.predecessors(s)) {
        if (!visited.contains(pred)) {
          visited.add(pred);
          if (s instanceof AbstractDefinitionStmt && s.getDefs().get(0).equivTo(use)) {
            defStmts.add(pred);
          } else {
            queue.add(pred);
          }
        }
      }
    }
    return defStmts;
  }

  /**
   * Use newUse to replace the oldUse in oldStmt.
   *
   * @param oldStmt a Stmt that has oldUse.
   * @param oldUse a Value in the useList of oldStmt.
   * @param newUse a Value is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Nonnull
  public static Stmt withNewUse(
          @Nonnull Stmt oldStmt, @Nonnull Value oldUse, @Nonnull Value newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return Objects.requireNonNull(visitor.getNewStmt());
  }
}
