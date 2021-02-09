package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.callgraph.spark.builder.MethodNodeFactory;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class IntraproceduralPointerAssignmentGraph {

  private PointerAssignmentGraph pag;
  private List<Pair<Node, Node>> sourceTargetPairs; // a (target) = b (source)
  private SootMethod method;
  private MethodNodeFactory nodeFactory;

  public IntraproceduralPointerAssignmentGraph(PointerAssignmentGraph pag, SootMethod method) {
    this.pag = pag;
    this.method = method;
    this.nodeFactory = new MethodNodeFactory(this);
    this.sourceTargetPairs = new LinkedList<>();
    build();
  }

  public void build() {
    if (method.isConcrete()) {
      Body body = method.getBody();
      for (Stmt stmt : body.getStmts()) {
        nodeFactory.processStmt(stmt);
      }
    } else {
      // TODO: build for native
    }
    // TODO: addMiscEdges
  }

  public List<Pair<Node, Node>> getSourceTargetPairs() {
    return sourceTargetPairs;
  }

  public void setSourceTargetPairs(List<Pair<Node, Node>> sourceTargetPairs) {
    this.sourceTargetPairs = sourceTargetPairs;
  }

  public SootMethod getMethod() {
    return method;
  }

  public PointerAssignmentGraph getPointerAssignmentGraph() {
    return pag;
  }

  public void addEdge(Node source, Node target) {
    if (source == null) {
      return;
    }
    Pair<Node, Node> sourceTargetPair = new ImmutablePair<>(source, target);
    sourceTargetPairs.add(sourceTargetPair);
  }
}
