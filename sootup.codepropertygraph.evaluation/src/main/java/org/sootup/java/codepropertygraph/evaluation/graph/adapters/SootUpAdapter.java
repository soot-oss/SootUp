package org.sootup.java.codepropertygraph.evaluation.graph.adapters;

import java.util.Objects;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpAdapter {
  public PropertyGraph adapt(PropertyGraph sootUpCfg) {
    PropertyGraph cfgGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpCfg.getEdges()) {
      if (Objects.equals(edge.getLabel(), "DDG")
          && edge.getSource() instanceof StmtPropertyGraphNode
          && edge.getSource() instanceof StmtPropertyGraphNode) {
        StmtPropertyGraphNode src = (StmtPropertyGraphNode) edge.getSource();
        StmtPropertyGraphNode dst = (StmtPropertyGraphNode) edge.getDestination();
        Stmt srcStmt = src.getStmt();
        Stmt dstStmt = dst.getStmt();
        if (dstStmt instanceof JAssignStmt
            && ((JAssignStmt) dstStmt).getLeftOp() instanceof JArrayRef
            && srcStmt.getDefs().contains(dstStmt.getArrayRef().getUses().get(0))) {
          continue;
        }
      }

      if (!edge.getSource().getName().contains(" := ")
          && !edge.getDestination().getName().contains(" := ")) {
        cfgGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
      }
    }

    return cfgGraph;
  }
}
