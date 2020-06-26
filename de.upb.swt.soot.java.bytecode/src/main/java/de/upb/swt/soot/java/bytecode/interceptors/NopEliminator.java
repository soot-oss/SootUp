package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/** @author Marcus Nachtigall, Markus Schmidt */
public class NopEliminator implements BodyInterceptor {

  /**
   * Removes {@link JNopStmt}s from the given {@link Body}. Complexity is linear with respect to the
   * statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    Body.BodyBuilder builder = Body.builder(originalBody);
    StmtGraph originalGraph = originalBody.getStmtGraph();
    Set<Stmt> stmtSet = originalGraph.nodes();

    for (Stmt stmt : stmtSet) {
      if (stmt instanceof JNopStmt) {
        final List<Stmt> successors = originalGraph.successors(stmt);
        // relink predecessors to successor of nop
        // [ms] in a valid Body there is always a successor of nop -> last stmt (no successor) is a
        // return|throw stmt
        final Stmt successorOfNop = successors.iterator().next();
        builder.removeFlow(stmt, successorOfNop);
        for (Stmt pred : originalGraph.predecessors(stmt)) {
          builder.removeFlow(pred, stmt);
          builder.addFlow(pred, successorOfNop);
        }
      }
    }

    return builder.build();
  }
}
