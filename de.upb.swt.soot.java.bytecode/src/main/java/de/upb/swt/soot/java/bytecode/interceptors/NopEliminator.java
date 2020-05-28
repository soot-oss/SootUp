package de.upb.swt.soot.java.bytecode.interceptors;

import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
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
   * Removes {@link JNopStmt}s from the given {@link Body}. Complexity is linear with respect to the statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    MutableGraph mutableGraph= Graphs.copyOf(originalBody.getStmtGraph());
    Set<Stmt> stmtSet = mutableGraph.nodes();

    for(Stmt stmt: stmtSet){
      if(stmt instanceof JNopStmt){
        boolean keepNop = false;
        if(mutableGraph.successors(stmt).size() == 0){
          for(Trap trap : originalBody.getTraps()){
            if(trap.getEndStmt() == stmt){
              keepNop = true;
            }
          }
        }
        if(!keepNop){
          for (Object predecessor : mutableGraph.predecessors(stmt)){
            for(Object successor : mutableGraph.successors(stmt)){
              mutableGraph.putEdge(predecessor, successor);
              mutableGraph.removeEdge(predecessor, stmt);
              mutableGraph.removeEdge(stmt, successor);
            }
          }
          mutableGraph.removeNode(stmt);
        }
      }
    }

    return originalBody.withStmts(mutableGraph);
  }
}
