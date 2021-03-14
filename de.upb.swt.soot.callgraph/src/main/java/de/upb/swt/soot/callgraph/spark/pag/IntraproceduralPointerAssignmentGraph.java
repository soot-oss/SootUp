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
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class IntraproceduralPointerAssignmentGraph {

  private final PointerAssignmentGraph pag;
  private final List<Pair<Node, Node>> sourceTargetPairs; // a (target) = b (source)
  private final SootMethod method;
  private final MethodNodeFactory nodeFactory;
  private final Queue<Pair<Node, Node>> internalEdges =
      new ArrayDeque<>(); // a (target) = b (source)
  private final Queue<Pair<Node, Node>> inEdges = new ArrayDeque<>();
  private final Queue<Pair<Node, Node>> outEdges = new ArrayDeque<>();
  private boolean hasBeenAdded = false;

  public IntraproceduralPointerAssignmentGraph(PointerAssignmentGraph pag, SootMethod method) {
    this.pag = pag;
    this.method = method;
    this.nodeFactory = new MethodNodeFactory(this);
    this.sourceTargetPairs = new LinkedList<>();
    build();
  }

  private void build() {
    if (method.isConcrete()) {
      Body body = method.getBody();
      for (Stmt stmt : body.getStmts()) {
        nodeFactory.processStmt(stmt);
      }
    } else {
      // TODO: build for native
    }
    // TODO: addMiscEdges
    addMiscEdges();
  }

  public void addToPAG() {
    // TODO: context
    if (hasBeenAdded) {
      return;
    }
    hasBeenAdded = true;

    addToPAG(internalEdges);
    addToPAG(inEdges);
    addToPAG(outEdges);
  }

  public SootMethod getMethod() {
    return method;
  }

  public PointerAssignmentGraph getPointerAssignmentGraph() {
    return pag;
  }

  public void addInternalEdge(@Nonnull Node source, @Nonnull Node target) {
    addEdge(internalEdges, source, target);
  }

  public void addInEdge(@Nonnull Node source, @Nonnull Node target) {
    addEdge(inEdges, source, target);
  }

  public void addOutEdge(@Nonnull Node source, @Nonnull Node target) {
    addEdge(outEdges, source, target);
  }

  private void addEdge(Queue<Pair<Node, Node>> edgeQueue, Node source, Node target) {
    if (source == null) {
      return;
    }
    edgeQueue.add(new ImmutablePair<>(source, target));
    if (hasBeenAdded) {
      pag.addEdge(source, target);
    }
  }

  private void addToPAG(Queue<Pair<Node, Node>> edgeQueue) {
    for (Pair<Node, Node> edge : edgeQueue) {
      Node source = edge.getKey();
      Node target = edge.getValue();
      pag.addEdge(source, target);
    }
  }

  private void addMiscEdges() {
    List<Pair<Node, Node>> edges =
        MiscEdgeHandler.getMiscEdge(method, pag.getNodeFactory(), nodeFactory);
    for (Pair<Node, Node> edge : edges) {
      addInEdge(edge.getKey(), edge.getValue());
    }
  }

  //TODO: parameterize context

  public MethodNodeFactory getNodeFactory() {
    return nodeFactory;
  }
}
