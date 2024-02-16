package org.sootup.java.codepropertygraph.evaluation;

import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpDdgGenerator;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpDdgAdapter {
  private final SootUpDdgGenerator sootUpDdgGenerator;

  public SootUpDdgAdapter(SootUpDdgGenerator sootUpDdgGenerator) {
    this.sootUpDdgGenerator = sootUpDdgGenerator;
  }

  public PropertyGraph getDdg(PropertyGraph sootUpDdg) {
    PropertyGraph ddgGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpDdgGenerator.getGraphEdges(sootUpDdg)) {
      if (!edge.getSource().getName().contains(" := ") && !edge.getDestination().getName().contains(" := ")) {
        ddgGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
      }
    }

    return ddgGraph;
  }
}
