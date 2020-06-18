package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
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
    StmtGraph mutableGraph = StmtGraph.copyOf(originalBody.getStmtGraph());
    Set<Stmt> stmtSet = mutableGraph.nodes();

    for (Stmt stmt : stmtSet) {
      if (stmt instanceof JNopStmt) {
        boolean keepNop = false;
        final Set<Stmt> successors = mutableGraph.successors(stmt);
        final int successorSize = successors.size();
        if (successorSize == 0) {
          for (Trap trap : originalBody.getTraps()) {
            if (trap.getEndStmt() == stmt) {
              keepNop = true;
            }
          }
        }
        if (!keepNop) {
          if (successorSize > 0) {
            final Stmt successorOfNop = successors.iterator().next();
            mutableGraph
                .predecessors(stmt)
                .forEach(pred -> mutableGraph.putEdge(pred, successorOfNop));
          }
          mutableGraph.removeNode(stmt);
        }
      }
    }

    return originalBody.withStmts(mutableGraph);
  }
}
