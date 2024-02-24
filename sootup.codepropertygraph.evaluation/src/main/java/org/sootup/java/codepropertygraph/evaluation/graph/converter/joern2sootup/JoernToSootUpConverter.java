package org.sootup.java.codepropertygraph.evaluation.graph.converter.joern2sootup;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Edge;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.*;
import org.sootup.java.codepropertygraph.evaluation.graph.util.NodeTypeResolver;
import scala.jdk.CollectionConverters;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.common.stmt.*;

public class JoernToSootUpConverter {

  private final NodeConverter nodeConverter = new NodeConverter();
  private final NodeTypeResolver nodeTypeResolver = new NodeTypeResolver();

  public MutableBlockStmtGraph adapt(Graph joernGraph) {
    if (joernGraph.vertices().size() == 0) {
      return new MutableBlockStmtGraph();
    }

    List<Stmt> stmts = new ArrayList<>();
    List<Stmt[]> edges = new ArrayList<>();
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    for (Edge edge : CollectionConverters.SeqHasAsJava(joernGraph.edges().toSeq()).asJava()) {
      StoredNode joernEdgeSrc = edge.src();
      StoredNode joernEdgeDst = edge.dst();

      // Todo: Add the start and end edges
      if (joernEdgeSrc instanceof Method || joernEdgeDst instanceof MethodReturn) continue;

      // Only consider statements
      if (!(joernEdgeSrc._astIn().hasNext() && joernEdgeSrc._astIn().next() instanceof Block)) {
        // System.out.println("Should be skipped because of src.");
        continue;
      }
      if (!(joernEdgeDst._astIn().hasNext() && joernEdgeDst._astIn().next() instanceof Block)) {
        // System.out.println("Should be skipped because of dst.");
        while (joernEdgeDst._cfgOut().hasNext()
            && !(joernEdgeDst._astIn().next() instanceof Block)) {
          joernEdgeDst = joernEdgeDst._cfgOut().next();
        }
      }

      Stmt src = nodeConverter.convert(joernEdgeSrc);
      Stmt dst = nodeConverter.convert(joernEdgeDst);

      if (!stmts.contains(src)) stmts.add(src);
      if (!stmts.contains(dst)) stmts.add(dst);
      edges.add(new Stmt[] {src, dst});

      if (nodeTypeResolver.isGotoStatement(joernEdgeSrc)
          && ((Expression) joernEdgeDst).lineNumber().nonEmpty()
          && nodeConverter
                  .getGotoStmtTarget((Unknown) joernEdgeSrc)
                  .getStmtPosition()
                  .getFirstLine()
              != ((Expression) joernEdgeDst).lineNumber().get()) continue;

      logEdgeConversionDetails(joernEdgeSrc, joernEdgeDst, src, dst);
    }

    Map<Stmt, Integer> counts = new HashMap<>();
    for (Stmt stmt : stmts) {
      counts.put(stmt, 0);
    }

    for (Stmt[] edge : edges) {
      Stmt source = edge[0];
      if (source instanceof JGotoStmt) {
        BranchingStmt branchingStmt = (BranchingStmt) source;
        graph.putEdge(branchingStmt, 0, edge[1]);
      } else if (source instanceof JIfStmt) {
        int count = counts.get(source);
        graph.putEdge((BranchingStmt) source, count, edge[1]);
        counts.put(source, count + 1);
      } else if (source instanceof JThrowStmt) {

      } else {
        graph.putEdge((FallsThroughStmt) source, edge[1]);
      }
    }

    return graph;
  }

  private static void logEdgeConversionDetails(
      StoredNode joernEdgeSrc, StoredNode joernEdgeDst, Stmt src, Stmt dst) {
    System.out.println(
        "\t\t"
            + String.format("%-80s", "[" + joernEdgeSrc.label() + "]")
            + "["
            + joernEdgeDst.label()
            + "]");
    System.out.println(
        "\t"
            + String.format("%-60s", joernEdgeSrc.toMap().get("CODE").get())
            + "   ---->   "
            + joernEdgeDst.toMap().get("CODE").get());
    System.out.println(
        "\t"
            + String.format("%-60s", (src != null ? src.toString() : null))
            + "   ====>   "
            + (dst != null ? dst.toString() : null));
    System.out.println("\t" + String.join("", Collections.nCopies(100, "-")));
  }
}
