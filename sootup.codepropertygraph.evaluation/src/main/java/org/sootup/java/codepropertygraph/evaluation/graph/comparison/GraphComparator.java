package org.sootup.java.codepropertygraph.evaluation.graph.comparison;

import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sootup.java.codepropertygraph.evaluation.graph.adapters.JoernAdapter;
import org.sootup.java.codepropertygraph.evaluation.graph.adapters.SootUpAdapter;
import org.sootup.java.codepropertygraph.evaluation.graph.converter.joern2sootup.JoernToSootUpConverter;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;

public class GraphComparator {
  private final JoernToSootUpConverter joernToSootUpConverter;
  private final SootUpAdapter sootUpAdapter;
  private final JoernAdapter joernAdapter;
  private int totalSameEdges;
  private int totalDiffEdges;
  private int totalMethods;

  public GraphComparator() {
    this.joernToSootUpConverter = new JoernToSootUpConverter();
    this.sootUpAdapter = new SootUpAdapter();
    this.joernAdapter = new JoernAdapter();
  }

  public void compare(Graph joernGraph, PropertyGraph sootUpGraph, String methodFullName) {

    MutableBlockStmtGraph convertedJoernGraph = joernToSootUpConverter.adapt(joernGraph);

    PropertyGraph joernPropertyGraph = joernAdapter.adapt(convertedJoernGraph);
    PropertyGraph sootUpPropertyGraph = sootUpAdapter.adapt(sootUpGraph);

    totalMethods++;

    int sameEdgesCount, diffEdgesCount;
    boolean foundEquivEdge;
    sameEdgesCount = diffEdgesCount = 0;
    List<String> simEdges = new ArrayList<>();
    List<String> diffEdges = new ArrayList<>();
    for (PropertyGraphEdge e : sootUpPropertyGraph.getEdges()) {
      foundEquivEdge = false;

      String sootUpEdgeSrc = applyNormalizations(e.getSource().getName());
      String sootUpEdgeDst = applyNormalizations(e.getDestination().getName());
      String sootUpEdgeStr = sootUpEdgeSrc + " -> " + sootUpEdgeDst;

      for (PropertyGraphEdge otherE : joernPropertyGraph.getEdges()) {
        if (Arrays.asList(
                e.getSource(), e.getDestination(), otherE.getSource(), otherE.getDestination())
            .contains(null)) continue;

        String joernEdgeSrc = applyNormalizations(otherE.getSource().getName());
        String joernEdgeDst = applyNormalizations(otherE.getDestination().getName());

        if (sootUpEdgeSrc.equals(joernEdgeSrc) && sootUpEdgeDst.equals(joernEdgeDst)) {
          sameEdgesCount++;
          foundEquivEdge = true;
          simEdges.add(sootUpEdgeStr);
          break;
        }
      }
      if (!foundEquivEdge) {
        diffEdgesCount++;
        diffEdges.add(sootUpEdgeStr);
      }
    }

    System.out.println("********************************************************");
    sootUpPropertyGraph.getNodes().forEach(n -> System.out.println(n.getName()));
    System.out.println("********************************************************");

    System.out.println("Method name               : " + methodFullName);
    System.out.println("Number of edges (SootUp)  : " + sootUpPropertyGraph.getEdges().size());
    System.out.println("Number of edges (Joern)   : " + joernPropertyGraph.getEdges().size());
    System.out.println("Different edges           : " + diffEdgesCount);
    System.out.println("Same      edges           : " + sameEdgesCount);

    totalSameEdges += sameEdgesCount;
    totalDiffEdges += diffEdgesCount;

    simEdges.forEach(
        sedge -> System.out.println("                                          " + sedge));
    if (simEdges.size() > 0 && diffEdges.size() > 0) {
      System.out.println(
          "                                          -----------------------------------------------------------------");
    }
    diffEdges.forEach(
        dedge -> System.out.println("                                          " + dedge));
    System.out.println("********************************************************");
    System.out.println("********************************************************");
    System.out.println("********************************************************");
  }

  private String applyNormalizations(String name) {
    String normalizedStmt = name;

    normalizedStmt = normalizeGotoStatements(normalizedStmt);
    normalizedStmt = sanitizeEscapeSequences(normalizedStmt);
    normalizedStmt = normalizeInvokeStmts(normalizedStmt);
    normalizedStmt = normalizeBootstrapCalls(normalizedStmt);

    return normalizedStmt;
  }

  /** Ignore goto stmt target */
  private static String normalizeGotoStatements(String stmtStr) {
    //
    if (stmtStr.startsWith("goto ")) stmtStr = "goto";
    return stmtStr;
  }

  private static String sanitizeEscapeSequences(String normalizedStmt) {
    // normalizedStmt = normalizedStmt.replace("'", "").replace("\\\"", "").replace("\\", "");
    normalizedStmt =
        normalizedStmt
            .replace("\\\"", "")
            .replace("'", "")
            .replace("\\", "")
            .replace("\\\"", "")
            .replace("\\'", "")
            .replace("\\\\", "\\");
    return normalizedStmt;
  }

  private static String normalizeInvokeStmts(String normalizedStmt) {
    return normalizedStmt
        .replace("specialinvoke", "virtualinvoke")
        .replace("interfaceinvoke", "virtualinvoke")
        .replace("dynamicinvoke", "virtualinvoke");
  }

  /** Ignore bootstrap method identifiers */
  private static String normalizeBootstrapCalls(String normalizedStmt) {
    if (normalizedStmt.contains("bootstrap$")) {
      normalizedStmt = normalizedStmt.replaceAll("__\\d+", "");
    }
    return normalizedStmt;
  }

  public int getTotalSameEdges() {
    return totalSameEdges;
  }

  public int getTotalDiffEdges() {
    return totalDiffEdges;
  }

  public int getTotalMethods() {
    return totalMethods;
  }
}
