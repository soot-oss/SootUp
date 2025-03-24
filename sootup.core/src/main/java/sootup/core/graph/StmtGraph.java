package sootup.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import com.google.common.collect.Iterators;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.ClassType;
import sootup.core.util.DotExporter;
import sootup.core.util.EscapedWriter;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.core.util.printer.JimplePrinter;

/**
 * Interface for control flow graphs on Jimple Stmts. A StmtGraph is directed and connected (except
 * for traphandlers - those are not connected to the unexceptional flow via StmtGraph). Its directed
 * edges represent flows between Stmts. If the edge starts in a branching Stmt there is an edge for
 * each flow to the target Stmt. This can include duplicate flows to the same target e.g. for
 * JSwitchStmt, so that every label has its own flow to a target.
 *
 * <p>THe StmtGraph structure keeps the edge insertion order of each node to store information about
 * successor stmts in its edges for Branching. Ordered edges are necessary because we want to
 * associate the i-th item with the i-th branch case of a {@link BranchingStmt}. In a valid
 * StmtGraph it is not allowed to have unconnected Nodes.
 *
 * <pre>
 *  Stmt stmt1, stmt2;
 *  ...
 *  MutableStmtGraph graph = new MutableBlockStmtGraph();
 *  graph.setEntryPoint(stmt1);
 *  graph.addNode(stmt1);
 *  graph.addNode(stmt2);
 *  graph.putEdge(stmt1, stmt2);
 * </pre>
 *
 * @author Markus Schmidt
 */
public abstract class StmtGraph<V extends BasicBlock<V>> implements Iterable<Stmt> {

  public abstract Stmt getStartingStmt();

  public abstract BasicBlock<?> getStartingStmtBlock();

  public abstract List<BasicBlock<?>> getTailStmtBlocks();

  /**
   * returns the nodes in this graph in a non-deterministic order (-&gt;Set) to get the nodes in
   * linearized, ordered manner use iterator() or getStmts.
   */
  @NonNull
  public abstract Collection<Stmt> getNodes();

  public List<Stmt> getStmts() {
    final ArrayList<Stmt> res = new ArrayList<>();
    Iterators.addAll(res, iterator());
    return res;
  }

  @NonNull
  public abstract Collection<? extends BasicBlock<?>> getBlocks();

  @NonNull
  public abstract List<? extends BasicBlock<?>> getBlocksSorted();

  public Iterator<BasicBlock<?>> getBlockIterator() {
    return new BlockGraphIterator(this);
  }

  public abstract BasicBlock<?> getBlockOf(@NonNull Stmt stmt);

  public abstract boolean containsNode(@NonNull Stmt node);

  /**
   * returns the ingoing flows to node as an List with no reliable/specific order and possibly
   * duplicate entries i.e. if a JSwitchStmt has multiple cases that brnach to `node`
   */
  @NonNull
  public abstract List<Stmt> predecessors(@NonNull Stmt node);

  /** it is possible to reach traphandlers through inline code i.e. without any exceptional flow */
  @NonNull
  public abstract List<Stmt> exceptionalPredecessors(@NonNull Stmt node);

  /** returns the outgoing flows of node as ordered List. The List can have duplicate entries! */
  @NonNull
  public abstract List<Stmt> successors(@NonNull Stmt node);

  @NonNull
  public abstract Map<ClassType, Stmt> exceptionalSuccessors(@NonNull Stmt node);

  /**
   * Collects all successors i.e. unexceptional and exceptional successors of a given stmt into a
   * list.
   *
   * @param stmt in the given graph
   * @return a list containing the unexceptional+exceptional successors of the given stmt
   */
  @NonNull
  public List<Stmt> getAllSuccessors(@NonNull Stmt stmt) {
    final List<Stmt> successors = successors(stmt);
    final Map<ClassType, Stmt> exSuccessors = exceptionalSuccessors(stmt);
    List<Stmt> allSuccessors = new ArrayList<>(successors.size() + exSuccessors.size());
    allSuccessors.addAll(successors);
    allSuccessors.addAll(exSuccessors.values());
    return allSuccessors;
  }

  /** returns the amount of ingoing flows into node */
  public abstract int inDegree(@NonNull Stmt node);

  /** returns the amount of flows that start from node */
  public abstract int outDegree(@NonNull Stmt node);

  /** returns the amount of flows with node as source or target. */
  public int degree(@NonNull Stmt node) {
    return inDegree(node) + outDegree(node);
  }

  /**
   * returns true if there is a flow between source and target throws an Exception if at least one
   * of the parameters is not contained in the graph.
   */
  public abstract boolean hasEdgeConnecting(@NonNull Stmt source, @NonNull Stmt target);

  /**
   * Removes the specified exceptional flow from all blocks.
   *
   * @param exceptionType The class type of the exceptional flow.
   * @param exceptionHandlerStmt The handler statement of the exceptional flow.
   */
  public abstract void removeExceptionalFlowFromAllBlocks(
      ClassType exceptionType, Stmt exceptionHandlerStmt);

  /**
   * returns a Collection of Stmts that leave the body (i.e. JReturnVoidStmt, JReturnStmt and
   * JThrowStmt)
   */
  @NonNull
  public List<Stmt> getTails() {
    return getNodes().stream()
        .filter(stmt -> stmt.getExpectedSuccessorCount() == 0)
        .collect(Collectors.toList());
  }

  /**
   * returns a Collection of all stmt in the graph that are either the starting stmt or only have an
   * exceptional ingoing flow
   */
  @NonNull
  public Collection<Stmt> getEntrypoints() {
    final ArrayList<Stmt> entrypoints = new ArrayList<>();
    entrypoints.add(getStartingStmt());

    Collection<? extends BasicBlock<?>> blocks = getBlocks();
    blocks.forEach(
        block -> {
          Stmt stmt = block.getHead();
          if (!(stmt instanceof JIdentityStmt)) return;

          JIdentityStmt jidStmt = (JIdentityStmt) stmt;
          IdentityRef rightOp = jidStmt.getRightOp();
          if (!(rightOp instanceof JCaughtExceptionRef)) return;
          // at this point we have an exception handler

          entrypoints.add(stmt);
        });

    return entrypoints;
  }

  /** validates whether the each Stmt has the correct amount of outgoing flows. */
  public void validateStmtConnectionsInGraph() {
    try {
      List<Stmt> handlerStmts = new ArrayList<>();
      for (Stmt stmt : getNodes()) {
        if (stmt instanceof JIdentityStmt) {
          // JThrowStmt?
          IdentityRef rightOp = ((JIdentityStmt) stmt).getRightOp();
          if (rightOp instanceof JCaughtExceptionRef) {
            handlerStmts.add(stmt);
          }
        }
      }

      for (Stmt stmt : getNodes()) {
        final List<Stmt> successors = successors(stmt);
        final int successorCount = successors.size();

        if (predecessors(stmt).isEmpty()) {

          if (!(stmt == getStartingStmt()
              || handlerStmts.stream().anyMatch(handler -> handler == stmt))) {
            throw new IllegalStateException(
                "Stmt '"
                    + stmt
                    + "' which is neither the StartingStmt nor a TrapHandler is missing a predecessor!");
          }
        }

        if (stmt instanceof BranchingStmt) {
          if (stmt instanceof JSwitchStmt) {
            if (successorCount != ((JSwitchStmt) stmt).getValueCount()) {
              throw new IllegalStateException(
                  stmt
                      + ": size of outgoing flows (i.e. "
                      + successorCount
                      + ") does not match the amount of JSwitchStmts case labels (i.e. "
                      + ((JSwitchStmt) stmt).getValueCount()
                      + ").");
            }
          } else if (stmt instanceof JIfStmt) {
            if (successorCount != 2) {
              throw new IllegalStateException(
                  stmt + ": JIfStmt must have '2' outgoing flow but has '" + successorCount + "'.");
            }
          } else if (stmt instanceof JGotoStmt) {
            if (successorCount != 1) {
              throw new IllegalStateException(
                  stmt + ": JGoto must have '1' outgoing flow but has '" + successorCount + "'.");
            }
          }

        } else if (stmt instanceof JReturnStmt
            || stmt instanceof JReturnVoidStmt
            || stmt instanceof JThrowStmt) {
          if (successorCount != 0) {
            throw new IllegalStateException(
                stmt + ": must have '0' outgoing flow but has '" + successorCount + "'.");
          }
        } else {
          if (successorCount != 1) {
            throw new IllegalStateException(
                stmt + ": must have '1' outgoing flow but has '" + successorCount + "'.");
          }
        }
      }

    } catch (Exception e) {
      final String urlToWebeditor = DotExporter.createUrlToWebeditor(this);
      throw new IllegalStateException("visualize invalid StmtGraph: " + urlToWebeditor, e);
    }
  }

  /**
   * Look for a path in graph, from def to use. This path has to lie inside an extended basic block
   * (and this property implies uniqueness.). The path returned includes from and to. FIXME: ms:
   * explain better
   *
   * @param from start point for the path.
   * @param to end point for the path.
   * @return null if there is no such path.
   */
  @Nullable
  public List<Stmt> getExtendedBasicBlockPathBetween(@NonNull Stmt from, @NonNull Stmt to) {

    // if this holds, we're doomed to failure!!!
    if (inDegree(to) > 1) {
      return null;
    }

    // pathStack := list of succs lists
    // pathStackIndex := last visited index in pathStack
    List<Stmt> pathStack = new ArrayList<>();
    List<Integer> pathStackIndex = new ArrayList<>();

    pathStack.add(from);
    pathStackIndex.add(0);

    int psiMax = outDegree(pathStack.get(0));
    int level = 0;
    while (pathStackIndex.get(0) != psiMax) {
      int p = pathStackIndex.get(level);

      List<Stmt> succs = successors((pathStack.get(level)));
      if (p >= succs.size()) {
        // no more succs - backtrack to previous level.

        pathStack.remove(level);
        pathStackIndex.remove(level);

        level--;
        int q = pathStackIndex.get(level);
        pathStackIndex.set(level, q + 1);
        continue;
      }

      Stmt betweenStmt = succs.get(p);

      // we win!
      if (betweenStmt == to) {
        pathStack.add(to);
        return pathStack;
      }

      // check preds of betweenStmt to see if we should visit its kids.
      if (inDegree(betweenStmt) > 1) {
        pathStackIndex.set(level, p + 1);
        continue;
      }

      // visit kids of betweenStmt.
      level++;
      pathStackIndex.add(0);
      pathStack.add(betweenStmt);
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof StmtGraph)) {
      return false;
    }
    StmtGraph<?> otherGraph = (StmtGraph<?>) o;

    if (getStartingStmt() != otherGraph.getStartingStmt()) {
      return false;
    }

    Collection<Stmt> nodes = getNodes();
    final Collection<Stmt> otherNodes = otherGraph.getNodes();
    if (nodes.size() != otherNodes.size()) {
      return false;
    }

    List<Trap> currTraps = new BriefStmtPrinter(this).getTraps();
    List<Trap> otherGraphTraps = new BriefStmtPrinter(otherGraph).getTraps();
    if (!currTraps.equals(otherGraphTraps)) {
      return false;
    }

    for (Stmt node : nodes) {
      if (!otherNodes.contains(node)) {
        return false;
      }
      final List<Stmt> successors = successors(node);
      final List<Stmt> otherSuccessors = otherGraph.successors(node);
      if (!successors.equals(otherSuccessors)) {
        return false;
      }
    }

    return true;
  }

  @Override
  @NonNull
  public Iterator<Stmt> iterator() {
    return new BlockStmtGraphIterator();
  }

  public List<Stmt> getBranchTargetsOf(BranchingStmt fromStmt) {
    final List<Stmt> successors = successors(fromStmt);
    if (fromStmt instanceof JIfStmt) {
      // remove the first successor as if its a fallsthrough stmt and not a branch target
      return Collections.singletonList(successors.get(1));
    }
    return successors;
  }

  public boolean isStmtBranchTarget(@NonNull Stmt targetStmt) {
    final List<Stmt> predecessors = predecessors(targetStmt);
    if (predecessors.size() > 1) {
      // join node i.e. at least one is a branch
      return true;
    }

    final Iterator<Stmt> iterator = predecessors.iterator();
    if (iterator.hasNext()) {
      Stmt pred = iterator.next();
      if (pred.branches()) {
        if (pred instanceof JIfStmt) {
          // [ms] bounds are validated in Body
          return getBranchTargetsOf((JIfStmt) pred).get(0) == targetStmt;
        }
        return true;
      }
    }

    return false;
  }

  /** Iterates the Stmts according to the jimple output order. */
  private class BlockStmtGraphIterator implements Iterator<Stmt> {

    private final BlockGraphIterator blockIt;
    @NonNull private Iterator<Stmt> currentBlockIt = Collections.emptyIterator();

    public BlockStmtGraphIterator() {
      this(new BlockGraphIterator(StmtGraph.this));
    }

    public BlockStmtGraphIterator(@NonNull BlockGraphIterator blockIterator) {
      blockIt = blockIterator;
    }

    @Override
    public boolean hasNext() {
      // hint: a BasicBlock has at least 1 Stmt or should not be in a StmtGraph!
      return currentBlockIt.hasNext() || blockIt.hasNext();
    }

    @Override
    public Stmt next() {
      if (!currentBlockIt.hasNext()) {
        if (!blockIt.hasNext()) {
          throw new NoSuchElementException("Iterator has no more Stmts.");
        }
        BasicBlock<?> currentBlock = blockIt.next();
        currentBlockIt = currentBlock.getStmts().iterator();
      }
      return currentBlockIt.next();
    }
  }

  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new JimplePrinter().printTo(this, writerOut);
    }
    return writer.toString();
  }
}
