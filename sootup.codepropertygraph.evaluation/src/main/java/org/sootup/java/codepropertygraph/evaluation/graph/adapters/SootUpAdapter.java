package org.sootup.java.codepropertygraph.evaluation.graph.adapters;

import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.java.codepropertygraph.propertygraph.*;

public class SootUpAdapter {
  public PropertyGraph adapt(PropertyGraph sootUpCfg) {
    PropertyGraph adaptedGraph = new PropertyGraph();

    for (PropertyGraphEdge edge : sootUpCfg.getEdges()) {
      StmtPropertyGraphNode src = (StmtPropertyGraphNode) edge.getSource();
      StmtPropertyGraphNode dst = (StmtPropertyGraphNode) edge.getDestination();
      Stmt srcStmt = src.getStmt();
      Stmt dstStmt = dst.getStmt();

      if (edge.getLabel().equals("DDG")
          && dstStmt instanceof JAssignStmt
          && ((JAssignStmt) dstStmt).getLeftOp() instanceof JArrayRef
          && srcStmt.getDefs().contains(dstStmt.getArrayRef().getUses().get(0))) continue;

      if ((srcStmt instanceof JIdentityStmt) || (dstStmt instanceof JIdentityStmt)) continue;
      if ((srcStmt instanceof JSwitchStmt) || (dstStmt instanceof JSwitchStmt)) continue;

      adaptedGraph.addEdge(edge.getSource(), edge.getDestination(), edge.getLabel());
    }

    return adaptedGraph;
  }
}
