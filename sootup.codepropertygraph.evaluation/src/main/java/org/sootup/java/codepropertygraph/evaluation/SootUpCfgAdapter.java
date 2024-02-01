package org.sootup.java.codepropertygraph.evaluation;

import io.shiftleft.codepropertygraph.generated.nodes.*;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpCfgGenerator;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpCfgAdapter {
  private final SootUpCfgGenerator sootUpCfgGenerator;

  public SootUpCfgAdapter(SootUpCfgGenerator sootUpCfgGenerator) {
    this.sootUpCfgGenerator = sootUpCfgGenerator;
  }

  public PropertyGraph getCfg(PropertyGraph sootUpCfg) {
    PropertyGraph cfgGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpCfgGenerator.getGraphEdges(sootUpCfg)) {
      if (!edge.getSource().getName().contains(" := ") && !edge.getDestination().getName().contains(" := ")) {
        cfgGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
      }
    }

    return cfgGraph;
  }
}
