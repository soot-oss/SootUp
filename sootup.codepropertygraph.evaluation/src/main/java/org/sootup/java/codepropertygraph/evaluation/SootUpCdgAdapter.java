package org.sootup.java.codepropertygraph.evaluation;

import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCdgGenerator;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpCdgAdapter {
  private final SootUpCdgGenerator sootUpCdgGenerator;

  public SootUpCdgAdapter(SootUpCdgGenerator sootUpCdgGenerator) {
    this.sootUpCdgGenerator = sootUpCdgGenerator;
  }

  public PropertyGraph getCdg(PropertyGraph sootUpCdg) {
    PropertyGraph cdgGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpCdgGenerator.getGraphEdges(sootUpCdg)) {
      if (!edge.getSource().getName().contains(" := ") && !edge.getDestination().getName().contains(" := ")) {
        cdgGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
      }
    }

    return cdgGraph;
  }
}
