package sootup.codepropertygraph.cdg;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
* %%
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation, either version 2.1 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Lesser Public License for more details.
*
* You should have received a copy of the GNU General Lesser Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/lgpl-2.1.html>.
* #L%
*/

import java.util.*;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.CdgEdge;
import sootup.codepropertygraph.propertygraph.nodes.ControlFlowGraphNode;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.PostDominanceFinder;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

/**
 * This class is responsible for creating the Control Dependence Graph (CDG) property graph for a
 * given Soot method.
 */
public class CdgCreator {

  /**
   * Creates the CDG property graph for the given Soot method.
   *
   * @param method the Soot method
   * @return the CDG property graph
   */
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new StmtMethodPropertyGraph.Builder();
    graphBuilder.setName("cdg_" + method.getName());

    if (method.isAbstract() || method.isNative()) {
      return graphBuilder.build();
    }

    ControlFlowGraph<?> controlFlowGraph = method.getBody().getControlFlowGraph();
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(controlFlowGraph);

    List<? extends BasicBlock<?>> blocks = controlFlowGraph.getBlocksSorted();
    for (BasicBlock<?> currBlock : blocks) {
      for (BasicBlock<?> frontierBlock : postDominanceFinder.getDominanceFrontiers(currBlock)) {
        ControlFlowGraphNode sourceNode = new ControlFlowGraphNode(frontierBlock.getTail());
        for (Stmt srcStmt : currBlock.getStmts()) {
          ControlFlowGraphNode destinationNode = new ControlFlowGraphNode(srcStmt);
          graphBuilder.addEdge(new CdgEdge(sourceNode, destinationNode));
        }
      }
    }

    return graphBuilder.build();
  }
}
