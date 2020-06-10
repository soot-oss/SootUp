package de.upb.swt.soot.core.graph;

import com.google.common.graph.*;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Graph structure which is optimized for iterating forward (vs random access or working on
 * predecessors of stmts/nodes) over a control flow graph
 *
 * @author Markus Schmidt
 */
public class OnwardStmtGraph implements Graph<Stmt> {

  @Nonnull private final HashMap<Stmt, Integer> stmtPosition;
  @Nonnull private final List<Stmt> stmtList;
  @Nonnull private final Map<Stmt, List<Stmt>> branches = new HashMap<>();

  private int edgeCount = 0;

  public OnwardStmtGraph(int stmtCount) {
    stmtList = new ArrayList<>();
    stmtPosition = new HashMap<>();
  }

  void addNode(Stmt stmt) {
    int index = stmtList.size();
    stmtList.add(stmt);
    stmtPosition.put(stmt, index);
  }

  void addEdge(Stmt u, Stmt v) {
    if (!stmtList.contains(u)) {
      throw new IllegalArgumentException(
          "first parameter node " + u + " is not in the list of nodes.");
    }
    if (!stmtList.contains(v)) {
      throw new IllegalArgumentException(
          "second parameter node " + v + " is not in the list of nodes.");
    }
    if (hasEdgeConnecting(u, v)) {
      return;
    }
    final List<Stmt> branchSuccessors = branches.computeIfAbsent(u, key -> new ArrayList<>());
    branchSuccessors.add(v);
    edgeCount++;
  }

  @Override
  public Set<Stmt> nodes() {
    return new LinkedHashSet(stmtList);
  }

  @Override
  public Set<EndpointPair<Stmt>> edges() {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  @Override
  public boolean allowsSelfLoops() {
    return false;
  }

  @Override
  public ElementOrder<Stmt> nodeOrder() {
    return ElementOrder.insertion();
  }

  @Override
  public Set<Stmt> adjacentNodes(Stmt node) {
    final HashSet<Stmt> set = new HashSet<>();
    set.addAll(predecessors(node));
    set.addAll(successors(node));
    return set;
  }

  @Override
  public Set<Stmt> predecessors(Stmt node) {
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Stmt> successors(Stmt node) {
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<EndpointPair<Stmt>> incidentEdges(Stmt node) {
    final Set<Stmt> predecessors = predecessors(node);
    final Set<Stmt> successors = successors(node);
    final HashSet<EndpointPair<Stmt>> incidents =
        new HashSet<>(predecessors.size() + successors.size());
    predecessors.forEach(pred -> incidents.add(EndpointPair.ordered(pred, node)));
    successors.forEach(succ -> incidents.add(EndpointPair.ordered(node, succ)));
    return incidents;
  }

  @Override
  public int degree(Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  @Override
  public int inDegree(Stmt node) {
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public int outDegree(Stmt node) {
    // TODO: write validator that there is no fallsThrough() true statement at the end!
    int size = branches.get(node).size();
    return (node.fallsThrough() ? size + 1 : size);
  }

  @Override
  /** expensive method; */
  public boolean hasEdgeConnecting(Stmt nodeU, Stmt nodeV) {
    // TODO: [ms] build cheaper version to find predecessor of nodeV in stmts
    return (nodeU.fallsThrough() && stmtList.get(stmtList.indexOf(nodeU) + 1) == nodeV)
        || (nodeU instanceof BranchingStmt
            && branches.get(nodeU).stream().anyMatch(stmt -> stmt == nodeV));
  }
}
