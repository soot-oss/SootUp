package org.sootup.java.codepropertygraph.evaluation.graph.comparison;

import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sootup.java.codepropertygraph.evaluation.graph.converter.joern2sootup.JoernToSootUpConverter;
import org.sootup.java.codepropertygraph.evaluation.graph.adapters.JoernAdapter;
import org.sootup.java.codepropertygraph.evaluation.graph.adapters.SootUpAdapter;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;

public class GraphSimilarityEvaluator {
  private final JoernToSootUpConverter joernToSootUpConverter;
  private final SootUpAdapter sootUpAdapter;
  private final JoernAdapter joernAdapter;
  private int totalSameEdges;
  private int totalDiffEdges;
  private int totalMethods;

  public GraphSimilarityEvaluator() {
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

      String a = e.getSource().getName();
      String b = e.getDestination().getName();
      if (a.startsWith("goto ")) a = "goto";
      if (b.startsWith("goto ")) b = "goto";

      String s1 = String.format("%s -> %s", a, b);
      s1 = s1.replace("\\\"", "").replace("'", "");

      for (PropertyGraphEdge otherE : joernPropertyGraph.getEdges()) {
        if (Arrays.asList(
                e.getSource(), e.getDestination(), otherE.getSource(), otherE.getDestination())
            .contains(null)) continue;

        String c = otherE.getSource().getName();
        String d = otherE.getDestination().getName();
        if (c.startsWith("goto ")) c = "goto";
        if (d.startsWith("goto ")) d = "goto";

        String s2 = String.format("%s -> %s", c, d);

        s2 =
            s2.replace("\\\"", "")
                .replace("'", ""); // Todo: Remove the \" occurences in the through the adapter

        s1 = s1.replace("specialinvoke", "virtualinvoke");
        s1 = s1.replace("interfaceinvoke", "virtualinvoke");
        s1 = s1.replace("dynamicinvoke", "virtualinvoke");

        s2 = s2.replace("specialinvoke", "virtualinvoke");
        s2 = s2.replace("interfaceinvoke", "virtualinvoke");
        s2 = s2.replace("dynamicinvoke", "virtualinvoke");

        if (s1.equals(s2)) {
          sameEdgesCount++;
          foundEquivEdge = true;
          simEdges.add(s1);
          break;
        }
      }
      if (!foundEquivEdge) {
        diffEdgesCount++;
        diffEdges.add(s1);
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
