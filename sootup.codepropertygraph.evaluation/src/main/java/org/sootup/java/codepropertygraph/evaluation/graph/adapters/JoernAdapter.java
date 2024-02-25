package org.sootup.java.codepropertygraph.evaluation.graph.adapters;

import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.codepropertygraph.propertygraph.NodeType;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphEdge;
import sootup.java.codepropertygraph.propertygraph.StmtPropertyGraphNode;

import java.util.HashSet;
import java.util.Set;

public class JoernAdapter {
  public PropertyGraph adapt(MutableBlockStmtGraph joernGraph) {
    PropertyGraph adaptedGraph = new PropertyGraph();

    Body.BodyBuilder builder = Body.builder(new MutableBlockStmtGraph(joernGraph));

    Set<Local> locals = new HashSet<>();
    for (Stmt stmt : builder.getStmtGraph().getNodes()) {
      for (Value value : stmt.getUsesAndDefs()) {
        if (value instanceof Local) {
          Local local = (Local) value;
          locals.add(local);
        }
      }
    }
    builder.setLocals(locals);

    for (Stmt stmt : builder.getStmts()) {
      for (Stmt successor : builder.getStmtGraph().getAllSuccessors(stmt)) {
        String srcName, dstName;

        srcName = stmt.toString();
        dstName = successor.toString();

        PropertyGraphEdge edge =
                new PropertyGraphEdge(
                        new StmtPropertyGraphNode(srcName, NodeType.STMT, stmt.getPositionInfo(), stmt),
                        new StmtPropertyGraphNode(
                                dstName, NodeType.STMT, successor.getPositionInfo(), successor),
                        "UNKOWN");
        adaptedGraph.addEdge(edge);
      }
    }

    return adaptedGraph;
  }
}
