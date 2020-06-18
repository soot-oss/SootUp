package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Trap;
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
        boolean keepNop = false;
        final List<Stmt> successors = originalGraph.successors(stmt);
        final int successorSize = successors.size();
        if (successorSize == 0) {
          for (Trap trap : originalBody.getTraps()) {
            if (trap.getEndStmt() == stmt) {
              keepNop = true;
            }
          }
        }
        if (!keepNop) {
          // relink predecessors to successor of nop
          if (successorSize > 0) {
            final Stmt successorOfNop = successors.iterator().next();
            originalGraph.predecessors(stmt).forEach(pred -> builder.addFlow(pred, successorOfNop));
          }
          // remove node,edges
          builder.removeStmt(stmt);
        }
      }
    }

    return builder.build();
  }
}
