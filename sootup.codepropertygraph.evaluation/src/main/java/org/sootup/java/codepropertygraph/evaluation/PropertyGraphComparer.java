package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernCfgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;

public class PropertyGraphComparer {
  private final JoernCfgGenerator joernCfgGenerator;
  private final SootUpCfgGenerator sootUpCfgGenerator;
  private final JoernCfgAdapter joernCfgAdapter;
  private final SootUpCfgAdapter sootUpCfgAdapter;
  private int totalSameEdges;
  private int totalDiffEdges;

  public PropertyGraphComparer(
      JoernCfgGenerator joernCfgGenerator, SootUpCfgGenerator sootUpCfgGenerator) {
    this.joernCfgGenerator = joernCfgGenerator;
    this.sootUpCfgGenerator = sootUpCfgGenerator;
    this.joernCfgAdapter = new JoernCfgAdapter(joernCfgGenerator);
    this.sootUpCfgAdapter = new SootUpCfgAdapter(sootUpCfgGenerator);
  }

  public boolean compareCfg(Graph joernCfg, Graph joernAst, PropertyGraph sootUpCfg) {

    PropertyGraph joernPropertyGraph = joernCfgAdapter.getCfg(joernCfg, joernAst);
    PropertyGraph sootUpPropertyGraph = sootUpCfgAdapter.getCfg(sootUpCfg);

    int sameEdgesCount, diffEdgesCount;
    boolean foundEquivEdge;
    sameEdgesCount = diffEdgesCount = 0;
    List<String> simEdges = new ArrayList<>();
    List<String> diffEdges = new ArrayList<>();
    for (PropertyGraphEdge e : joernPropertyGraph.getEdges()) {
      /*if (sootUpPropertyGraph.getEdges().contains(e))
      throw new RuntimeException("ACHIEVED !!!!!!!!!!!!!!!!!!!!!!!!!!!!");*/
      foundEquivEdge = false;

      for (PropertyGraphEdge otherE : sootUpPropertyGraph.getEdges()) {
        if (Arrays.asList(
                e.getSource(), e.getDestination(), otherE.getSource(), otherE.getDestination())
            .contains(null)) continue;

        String a = e.getSource().getName();
        String b = e.getDestination().getName();
        String c = otherE.getSource().getName();
        String d = otherE.getDestination().getName();

        String s1 = String.format("%s -> %s", a, b);

        String s2 = String.format("%s -> %s", c, d);

        if (s1.equals(s2)) {
          // throw new RuntimeException("Should be equal !!!!!!!!!!!!!");
          sameEdgesCount++;
          foundEquivEdge = true;
          simEdges.add(s1);
          break;
        }
        if (e.getSource().getType().equals(otherE.getSource().getType())
            && e.getDestination().getType().equals(otherE.getDestination().getType())
            && e.getSource()
                .getName()
                .split(" ")[0]
                .equals(otherE.getSource().getName().split(" ")[0])
            && e.getDestination()
                .getName()
                .split(" ")[0]
                .equals(otherE.getDestination().getName().split(" ")[0])) {
          // diffEdges.add(s1 + "    &    " + s2);
          diffEdges.add("[src1] " + a);
          diffEdges.add("[src2] " + c);
          diffEdges.add("[dst1] " + b);
          diffEdges.add("[dst2] " + d);
          diffEdges.add("--");
        }
      }
      if (!foundEquivEdge) diffEdgesCount++;
    }

    System.out.println("********************************************************");
    sootUpPropertyGraph.getNodes().forEach(n -> System.out.println(n.getName()));
    System.out.println("********************************************************");

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

    return true;
  }

  public int getTotalSameEdges() {
    return totalSameEdges;
  }

  public int getTotalDiffEdges() {
    return totalDiffEdges;
  }
}
