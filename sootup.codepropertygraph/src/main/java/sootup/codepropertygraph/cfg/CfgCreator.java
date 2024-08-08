package sootup.codepropertygraph.cfg;

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

import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.StmtMethodPropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.SootMethod;

/**
 * This class is responsible for creating the Control Flow Graph (CFG) property graph for a given
 * Soot method.
 */
public class CfgCreator {

  /**
   * Creates the CFG property graph for the given Soot method.
   *
   * @param method the Soot method
   * @return the CFG property graph
   */
  public PropertyGraph createGraph(SootMethod method) {
    PropertyGraph.Builder graphBuilder = new StmtMethodPropertyGraph.Builder();
    graphBuilder.setName("cfg_" + method.getName());

    if (method.isAbstract() || method.isNative()) {
      return graphBuilder.build();
    }

    StmtGraph<?> stmtGraph = method.getBody().getStmtGraph();
    stmtGraph.forEach(
        currStmt -> {
          int expectedCount = currStmt.getExpectedSuccessorCount();
          int successorIndex = 0;

          for (Stmt successor : stmtGraph.getAllSuccessors(currStmt)) {
            StmtGraphNode sourceNode = new StmtGraphNode(currStmt);
            StmtGraphNode destinationNode = new StmtGraphNode(successor);
            AbstCfgEdge edge = createEdge(currStmt, successorIndex, sourceNode, destinationNode);

            if (successorIndex >= expectedCount) {
              edge = new ExceptionalCfgEdge(sourceNode, destinationNode);
            }

            graphBuilder.addEdge(edge);
            successorIndex++;
          }
        });

    return graphBuilder.build();
  }

  /**
   * Creates an edge between the source and destination nodes based on the type of statement.
   *
   * @param currStmt the current statement
   * @param successorIndex the index of the successor
   * @param sourceNode the source node
   * @param destinationNode the destination node
   * @return the created edge
   */
  private AbstCfgEdge createEdge(
      Stmt currStmt, int successorIndex, StmtGraphNode sourceNode, StmtGraphNode destinationNode) {
    if (currStmt instanceof JIfStmt) {
      return successorIndex == JIfStmt.TRUE_BRANCH_IDX
          ? new IfTrueCfgEdge(sourceNode, destinationNode)
          : new IfFalseCfgEdge(sourceNode, destinationNode);
    } else if (currStmt instanceof JSwitchStmt) {
      return new SwitchCfgEdge(sourceNode, destinationNode);
    } else if (currStmt instanceof JGotoStmt) {
      return new GotoCfgEdge(sourceNode, destinationNode);
    } else {
      return new NormalCfgEdge(sourceNode, destinationNode);
    }
  }
}
