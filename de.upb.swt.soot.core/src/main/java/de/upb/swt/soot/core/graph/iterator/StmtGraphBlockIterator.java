package de.upb.swt.soot.core.graph.iterator;

import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Iterates over a given StmtGraph (which is connected, so all Stmt nodes are reached - except
 * traphandler) so the returned Jimple Stmts are returned as valid, linearized code blocks that are
 * intuitive to navigate.
 *
 * @author Markus Schmidt
 */
public class StmtGraphBlockIterator implements Iterator<Stmt> {

  @Nonnull private final StmtGraph graph;
  @Nonnull private final ArrayList<Trap> traps;
  @Nonnull private final HashMap<Stmt, Stmt> loopEdges = new HashMap<>();
  @Nonnull private final Set<Stmt> labeledStmt = new HashSet<>();
  @Nonnull private final List<Stmt> insertPoints = new ArrayList<>();
  @Nonnull private final Set<Stmt> insertedSources = new HashSet<>();
  @Nonnull private final ArrayDeque<Stmt> stack = new ArrayDeque<>();

  @Nonnull
  private final HashMap<Stmt, ArrayList<Stmt>> matchedInsertPointsSources = new HashMap<>();

  @Nonnull private HashMap<Stmt, List<Stmt>> predsAdjacentList = new HashMap<>();

  public StmtGraphBlockIterator(@Nonnull StmtGraph graph, List<Trap> traps) {
    this(graph, graph.getStartingStmt(), traps);
  }

  private StmtGraphBlockIterator(
      @Nonnull StmtGraph graph, @Nonnull Stmt startingStmt, @Nonnull List<Trap> traps) {
    this.graph = graph;
    this.traps = new ArrayList<>(traps);
    detectLoopEdges(); // checked
    this.predsAdjacentList = buildPredAdjacentListWithoutLoop();
    stack.addFirst(startingStmt);
    if (!this.traps.isEmpty()) {
      findAllLabelStmts(); // checked
      findAllInsertPoint(); // checked
      matchInsertPositionSources(); // checked
    }
  }

  @Override
  @Nonnull
  public Stmt next() {
    if (!this.stack.isEmpty()) {
      // System.out.println(this.stack);
      Stmt nextStmt = this.stack.pop();
      List<Stmt> succs = graph.successors(nextStmt);
      for (int i = succs.size() - 1; i >= 0; i--) {
        Stmt succ = succs.get(i);
        List<Stmt> preds = this.predsAdjacentList.get(succs.get(i));
        if (preds.contains(nextStmt)) {
          preds.remove(nextStmt);
          if (preds.isEmpty()) {
            this.stack.push(succ);
          }
        }
        if (this.insertPoints.contains(nextStmt)) {
          List<Stmt> sources = this.matchedInsertPointsSources.get(nextStmt);
          for (int j = sources.size() - 1; j >= 0; j--) {
            Stmt source = sources.get(j);
            if (!this.insertedSources.contains(source)) {
              this.stack.push(source);
              this.insertedSources.add(source);
            }
          }
        }
      }
      return nextStmt;
    } else {
      // Todo: [ZW] maybe need other exception handling
      throw new RuntimeException("There's no such stmt!");
    }
  }

  @Override
  public boolean hasNext() {
    // Todo: [ZW] maybe need exception handling
    boolean hasNext = false;
    if (!this.stack.isEmpty()) {
      hasNext = true;
    }
    return hasNext;
  }

  /** Use DFS to detect loop edges, and store them in a HashMap key: fromStmt, value: toStmt */
  private void detectLoopEdges() {

    // find all sources in the graph
    List<Stmt> sources = new ArrayList<>();
    sources.add(graph.getStartingStmt());
    for (Trap trap : traps) {
      Stmt handlerStmt = trap.getHandlerStmt();
      if (!sources.contains(handlerStmt)) {
        sources.add(handlerStmt);
      }
    }

    // build adjacent-list for graph
    HashMap<Stmt, List<Stmt>> adjacentList = new HashMap<>();
    for (Stmt stmt : graph.nodes()) {
      adjacentList.put(stmt, graph.successors(stmt));
    }

    // initialize stack
    Deque<Stmt> stack = new ArrayDeque<>(sources);
    Set<Stmt> greys = new HashSet<>(sources);
    Set<Stmt> blacks = new HashSet<>();

    // perform the DFS
    while (!stack.isEmpty()) {
      Stmt fromStmt = stack.getFirst();
      if (adjacentList.get(fromStmt).isEmpty()) {
        greys.remove(fromStmt);
        blacks.add(fromStmt);
        stack.removeFirst();
      } else {
        List<Stmt> stmtList = adjacentList.get(fromStmt);
        Stmt toStmt = stmtList.get(0);
        List<Stmt> reducedList = stmtList.subList(1, stmtList.size());
        adjacentList.replace(fromStmt, reducedList);
        if (!greys.contains(toStmt) && !blacks.contains(toStmt)) {
          greys.add(toStmt);
          stack.addFirst(toStmt);
        } else if (greys.contains(toStmt)) {
          loopEdges.put(fromStmt, toStmt);
        }
      }
    }
  }

  /**
   * build pred-adjacent-list for graph without loopEdges, store it in a HashMap: key is Stmt, value
   * a list of predecessors of the corresponding stmt
   */
  private HashMap<Stmt, List<Stmt>> buildPredAdjacentListWithoutLoop() {
    HashMap<Stmt, List<Stmt>> adList = new HashMap<>();

    for (Stmt stmt : graph.nodes()) {
      List<Stmt> preds = graph.predecessors(stmt);
      List<Stmt> reducedPreds = new ArrayList<>();
      for (Stmt pred : preds) {
        if (!this.loopEdges.containsKey(pred) || stmt != loopEdges.get(pred)) {
          reducedPreds.add(pred);
        }
      }
      adList.put(stmt, reducedPreds);
    }
    return adList;
  }

  /** Find all stmts which with a label */
  private void findAllLabelStmts() {
    for (Trap trap : traps) {
      this.labeledStmt.add(trap.getBeginStmt());
      this.labeledStmt.add(trap.getEndStmt());
      this.labeledStmt.add(trap.getHandlerStmt());
    }
    for (Stmt stmt : graph.nodes()) {
      if (stmt instanceof JGotoStmt) {
        labeledStmt.add(graph.successors(stmt).get(0));
      } else if (stmt instanceof JIfStmt) {
        labeledStmt.add(graph.successors(stmt).get(1));
      } else if (stmt instanceof JSwitchStmt) {
        for (Stmt succ : graph.successors(stmt)) {
          labeledStmt.add(succ);
        }
      }
    }
  }

  /**
   * Each Trap's handlerStmt is a source node in StmtGraph. It handles, when the handlerBlock should
   * be inserted in body. The function is to calculate the stmt(InsertPoint). The handlerBlock
   * should be inserted after this stmt directly.
   *
   * @param trap
   * @return Stmt: The handlerBlock should be inserted before this stmt directly.
   */
  private Stmt findInsertPoint(Trap trap) {
    HashMap<Stmt, List<Stmt>> predsAdList = buildPredAdjacentListWithoutLoop();
    Stmt endStmt = trap.getEndStmt();
    Deque<Stmt> queue = new ArrayDeque<Stmt>();
    queue.addFirst(endStmt);
    Set<Stmt> candidates = new HashSet<>();
    Boolean isSplitt = false;
    Stmt insertPoint = null;
    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      List<Stmt> succs = graph.successors(stmt);
      if (succs.size() == 1) {
        Stmt succ = succs.get(0);
        if (this.labeledStmt.contains(succ)) {
          insertPoint = stmt;
          break;
        } else {
          queue.addLast(succ);
        }
      } else if (succs.size() > 1) {
        isSplitt = true;
        queue.addFirst(stmt);
        break;
      }
    }
    if (isSplitt) {
      do {
        insertPoint = queue.removeFirst();
        List<Stmt> succs = graph.successors(insertPoint);
        for (int i = succs.size() - 1; i >= 0; i--) {
          Stmt succ = succs.get(i);
          List<Stmt> preds = predsAdList.get(succ);
          if (preds.contains(insertPoint)) {
            preds.remove(insertPoint);
            if (preds.isEmpty()) {
              if (candidates.contains(succ)) {
                candidates.remove(succ);
              }
              queue.addFirst(succ);
            } else {
              candidates.add(succ);
            }
          }
        }
      } while ((candidates.size() + queue.size()) > 1);
    }
    return insertPoint;
  }

  /** find all insert-point */
  private void findAllInsertPoint() {
    for (Trap trap : this.traps) {
      this.insertPoints.add(findInsertPoint(trap));
    }
  }

  private void matchInsertPositionSources() {
    List<Stmt> sources = new ArrayList<>();
    for (Trap trap : traps) {
      Stmt handlerStmt = trap.getHandlerStmt();
      sources.add(handlerStmt);
    }
    for (int i = 0; i < this.insertPoints.size(); i++) {
      Stmt insertStmt = insertPoints.get(i);
      if (matchedInsertPointsSources.containsKey(insertStmt)) {
        matchedInsertPointsSources.get(insertStmt).add(sources.get(i));
      } else {
        ArrayList<Stmt> matchedSources = new ArrayList<>();
        matchedSources.add(sources.get(i));
        this.matchedInsertPointsSources.put(insertStmt, matchedSources);
      }
    }
  }
}
