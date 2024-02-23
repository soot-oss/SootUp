package org.sootup.java.codepropertygraph.evaluation;

import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpDdgGenerator;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpDdgAdapter {
  private final SootUpDdgGenerator sootUpDdgGenerator;

  public SootUpDdgAdapter(SootUpDdgGenerator sootUpDdgGenerator) {
    this.sootUpDdgGenerator = sootUpDdgGenerator;
  }

  public PropertyGraph getDdg(PropertyGraph sootUpDdg) {
    PropertyGraph ddgGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpDdgGenerator.getGraphEdges(sootUpDdg)) {
      if (edge.getSource() instanceof StmtPropertyGraphNode
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
        ddgGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
      }
    }

    return ddgGraph;
  }
}
