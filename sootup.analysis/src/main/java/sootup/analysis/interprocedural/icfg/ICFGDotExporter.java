package sootup.analysis.interprocedural.icfg;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.DotExporter;

public class ICFGDotExporter {

  public static String buildICFGGraph(Map<MethodSignature, StmtGraph> signatureToStmtGraph) {
    final StringBuilder sb = new StringBuilder();
    DotExporter.buildDiGraphObject(sb);
    Map<Integer, MethodSignature> calls;
    calls = computeCalls(signatureToStmtGraph.values());
    for (Map.Entry<MethodSignature, StmtGraph> entry : signatureToStmtGraph.entrySet()) {
      String graph = DotExporter.buildGraph(entry.getValue(), true, calls, entry.getKey());
      sb.append(graph + "\n");
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * This method finds out all the calls made in the given StmtGraphs, so it can be edged to other
   * methods.
   */
  private static Map<Integer, MethodSignature> computeCalls(Collection<StmtGraph> stmtGraphSet) {
    Map<Integer, MethodSignature> calls = new HashMap<>();
    for (StmtGraph stmtGraph : stmtGraphSet) {
      Collection<? extends BasicBlock<?>> blocks;
      try {
        blocks = stmtGraph.getBlocksSorted();
      } catch (Exception e) {
        blocks = stmtGraph.getBlocks();
      }
      for (BasicBlock<?> block : blocks) {
        List<Stmt> stmts = block.getStmts();
        for (Stmt stmt : stmts) {
          if (stmt.containsInvokeExpr()) {
            MethodSignature methodSignature = stmt.getInvokeExpr().getMethodSignature();
            int hashCode = stmt.hashCode();
            calls.put(hashCode, methodSignature);
          }
        }
      }
    }
    return calls;
  }
}
