package de.upb.swt.soot.core.graph;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Zun Wang
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

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * @author Zun Wang ExceptionalStmtGraph is used to look up exceptional predecessors and successors
 *     for each stmt. Exceptional Successor of a stmt: If a stmt is in a trap, namely, stmt is
 *     between the trap's beginStmt(inclusive) and this trap's endStmt(exclusive), then the trap's
 *     handlerStmt is the exceptional successor of this stmt. Exceptional Predecessors of a
 *     stmt(handlerStmt): If this handlerStmt is another stmt's successor, then the another stmt is
 *     predecessor of this hanlderStmt. Exceptional DestinationTrap of a stmt: if the stmt is in a
 *     trap, then this trap is the destination trap of the stmt.
 */
public class MutableExceptionalStmtGraph extends MutableStmtGraphImpl {

  @Nonnull private final ArrayList<List<Stmt>> exceptionalPreds = new ArrayList<>();
  @Nonnull private final ArrayList<List<Stmt>> exceptionalSuccs = new ArrayList<>();
  @Nonnull private final ArrayList<List<Trap>> exceptionalTargetTraps = new ArrayList<>();

  /** creates an empty instance of ExceptionalStmtGraph */
  public MutableExceptionalStmtGraph() {
    super();
  }

  /** creates a mutable copy(!) of originalStmtGraph with exceptional info */
  public MutableExceptionalStmtGraph(@Nonnull StmtGraph oriStmtGraph) {
    super(oriStmtGraph);

    // initialize exceptionalPreds and exceptionalSuccs
    int size = oriStmtGraph.nodes().size();

    for (int i = 0; i < size; i++) {
      exceptionalPreds.add(Collections.emptyList());
      exceptionalSuccs.add(Collections.emptyList());
      exceptionalTargetTraps.add(Collections.emptyList());
    }

    // if there're traps, then infer every stmt's exceptional succs
    if (!oriStmtGraph.getTraps().isEmpty()) {

      List<Trap> traps = oriStmtGraph.getTraps();

      // Map: key: a stmt  | value: position num of corresponding stmt
      Map<Stmt, Integer> stmtToPosInBody = getStmtToPosInBody(oriStmtGraph);

      // This map is using for collecting predecessors for each handlerStmts
      HashMap<Stmt, List<Stmt>> handlerStmtToPreds = new HashMap<>();
      oriStmtGraph
          .getTraps()
          .forEach(trap -> handlerStmtToPreds.put(trap.getHandlerStmt(), new ArrayList<>()));

      // set exceptional successors for each stmt
      for (Stmt stmt : oriStmtGraph.nodes()) {
        List<Stmt> inferedSuccs = inferExceptionalSuccs(stmt, stmtToPosInBody, traps);
        Integer idx = stmtToIdx.get(stmt);
        exceptionalSuccs.set(idx, inferedSuccs);
        inferedSuccs.forEach(handlerStmt -> handlerStmtToPreds.get(handlerStmt).add(stmt));
      }

      // set exceptional predecessors for the stmt which is a handlerStmt
      for (Stmt handlerStmt : handlerStmtToPreds.keySet()) {
        Integer index = stmtToIdx.get(handlerStmt);
        exceptionalPreds.set(index, handlerStmtToPreds.get(handlerStmt));
      }

      // set exceptional destination-traps for each stmt
      for (Stmt stmt : oriStmtGraph.nodes()) {
        List<Trap> inferedDests = inferExceptionalDestinations(stmt, stmtToPosInBody, traps);
        Integer idx = stmtToIdx.get(stmt);
        exceptionalTargetTraps.set(idx, inferedDests);
      }
    }
  }

  @Nonnull
  public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
    int idx = getNodeIdx(stmt);
    List<Stmt> stmts = exceptionalPreds.get(idx);
    return Collections.unmodifiableList(stmts);
  }

  @Nonnull
  public List<Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
    int idx = getNodeIdx(stmt);
    List<Stmt> stmts = exceptionalSuccs.get(idx);
    return Collections.unmodifiableList(stmts);
  }

  @Nonnull
  public List<Trap> getDestTraps(@Nonnull Stmt stmt) {
    int idx = getNodeIdx(stmt);
    return exceptionalTargetTraps.get(idx);
  }

  @Nonnull
  @Override
  public ExceptionalStmtGraph unmodifiableStmtGraph() {
    return new ExceptionalStmtGraph(this);
  }

  /**
   * Set the targetTrap of the given stmt as empty list
   *
   * @param stmt a given stmt
   */
  public void removeTargetTraps(@Nonnull Stmt stmt) {
    int idx = getNodeIdx(stmt);
    List<Trap> dests = exceptionalTargetTraps.get(idx);
    exceptionalTargetTraps.set(idx, Collections.emptyList());
    exceptionalSuccs.set(idx, Collections.emptyList());
    for (Trap trap : dests) {
      int i = getNodeIdx(trap.getHandlerStmt());
      exceptionalPreds.get(i).remove(stmt);
    }
  }

  /**
   * Replaced stmt is never a handlerStmt of a Trap.
   *
   * @param oldStmt a stmt which is already in the StmtGraph
   * @param newStmt a new stmt which will replace the old stmt
   */
  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {

    if (oldStmt.getSuccessorCount() != newStmt.getSuccessorCount()) {
      throw new RuntimeException(
          "You can only use replaceNode if newStmt has the same amount of branches/outgoing flows.");
    }

    super.replaceNode(oldStmt, newStmt);

    int idx = stmtToIdx.get(newStmt);

    if (!exceptionalSuccs.isEmpty()) {
      for (Stmt exceptSucc : exceptionalSuccs.get(idx)) {
        Integer exceptSuccIdx = stmtToIdx.get(exceptSucc);
        exceptionalPreds.get(exceptSuccIdx).remove(oldStmt);
        exceptionalPreds.get(exceptSuccIdx).add(newStmt);
      }

      for (Trap trap : getTraps()) {
        if (trap.getBeginStmt() == newStmt || trap.getEndStmt() == newStmt) {
          int hIdx = stmtToIdx.get(trap.getHandlerStmt());
          for (Stmt exceptPred : exceptionalPreds.get(hIdx)) {
            int exceptPredIdx = stmtToIdx.get(exceptPred);
            List<Trap> dests = exceptionalTargetTraps.get(exceptPredIdx);
            for (Trap dest : dests) {
              if (dest.getHandlerStmt() == trap.getHandlerStmt()
                  && (dest.getBeginStmt() == oldStmt || dest.getEndStmt() == oldStmt)) {
                exceptionalTargetTraps.get(exceptPredIdx).remove(dest);
                exceptionalTargetTraps.get(exceptPredIdx).add(trap);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Build the map for stmt positions in a StmtGraph
   *
   * @param stmtGraph an instance of StmtGraph
   * @return a map with key: stmt value: the corresponding position number
   */
  private Map<Stmt, Integer> getStmtToPosInBody(StmtGraph stmtGraph) {
    Map<Stmt, Integer> stmtToPos = new HashMap<>();
    Integer pos = 0;
    for (Stmt stmt : stmtGraph) {
      stmtToPos.put(stmt, pos);
      pos++;
    }
    return stmtToPos;
  }

  /**
   * Check whether the range of trap1 includes the range of trap2 completely
   *
   * @param trap1 a trap maybe with bigger range
   * @param trap2 a trap maybe with smaller range
   * @param posTable a map that maps each stmt to a position num in body
   * @return true if the range of trap1 includes the range of trap2 completely, else false
   */
  private boolean isInclusive(Trap trap1, Trap trap2, Map<Stmt, Integer> posTable) {
    if (!trap1.getExceptionType().equals(trap2.getExceptionType())) {
      return false;
    }
    Integer posb1 = posTable.get(trap1.getBeginStmt());
    Integer pose1 = posTable.get(trap1.getEndStmt());
    Integer posb2 = posTable.get(trap2.getBeginStmt());
    Integer pose2 = posTable.get(trap2.getEndStmt());
    if (posb1 == null) {
      throw new RuntimeException(trap1.getBeginStmt() + " is not contained by pos-table!");
    } else if (pose1 == null) {
      throw new RuntimeException(trap1.getEndStmt() + " is not contained by pos-table!");
    } else if (posb2 == null) {
      throw new RuntimeException(trap2.getBeginStmt() + " is not contained by pos-table!");
    } else if (pose2 == null) {
      throw new RuntimeException(trap2.getEndStmt() + " is not contained by pos-table!");
    } else {
      if (posb1 < posb2 && pose1 > pose2) {
        return true;
      }
      return false;
    }
  }

  /**
   * Using the information of body position for each stmt and the information of traps infer the
   * exceptional destinations for a given stmt.
   *
   * @param stmt a given stmt
   * @param posTable a map that maps each stmt to its corresponding position number in the body
   * @param traps a given list of traps
   * @return
   */
  private List<Trap> inferExceptionalDestinations(
      Stmt stmt, Map<Stmt, Integer> posTable, List<Trap> traps) {
    List<Trap> destinations = new ArrayList<>();
    int pos = posTable.get(stmt);
    // 1.step if the stmt in a trap range, then this trap is a candidate for exceptional destination
    // of the stmt
    for (Trap trap : traps) {
      int beginPos = posTable.get(trap.getBeginStmt());
      int endPos = posTable.get(trap.getEndStmt());
      if (pos >= beginPos && pos < endPos) {
        destinations.add(trap);
      }
    }
    if (destinations.isEmpty()) {
      return Collections.emptyList();
    }

    // 2.step if a trap includes another trap completely, then delete this trap-candidate
    List<Trap> removedTraps = new ArrayList<>();
    for (Trap dest : destinations) {
      for (Trap anotherDest : destinations) {
        if (isInclusive(dest, anotherDest, posTable)) {
          removedTraps.add(dest);
        }
      }
    }
    if (!removedTraps.isEmpty()) {
      destinations.removeAll(removedTraps);
    }
    return destinations;
  }

  /**
   * Using the information of body position for each stmt and the information of traps infer the
   * exceptional successors for a given stmt.
   *
   * @param stmt a given stmt
   * @param posTable a map that maps each stmt to its corresponding position number in the body
   * @param traps a given list of traps
   * @return
   */
  private List<Stmt> inferExceptionalSuccs(
      Stmt stmt, Map<Stmt, Integer> posTable, List<Trap> traps) {
    List<Stmt> exceptionalSuccs = new ArrayList<>();

    // 1.step if the stmt in a trap range, then this trap's handlerStmt
    // is a candidate for exceptional successors of the stmt
    // 2.step if a trap-candidate includes another trap-candidate completely,
    // then delete this trap-candidate
    // the both steps are done in the method <code>inferExceptionalDestinations</code>
    List<Trap> candidates = inferExceptionalDestinations(stmt, posTable, traps);

    for (Trap trap : candidates) {
      if (!exceptionalSuccs.contains(trap.getHandlerStmt())) {
        exceptionalSuccs.add(trap.getHandlerStmt());
      }
    }
    // 3.step detect chained traps, if a handlerStmt(Succ) is trap's beginStmt,
    // then handlerStmt of this trap is also a successor.
    Deque<Stmt> queue = new ArrayDeque<>(exceptionalSuccs);
    while (!queue.isEmpty()) {
      Stmt first = queue.removeFirst();
      for (Trap t : traps) {
        if (first == t.getBeginStmt()) {
          Stmt handlerStmt = t.getHandlerStmt();
          if (!exceptionalSuccs.contains(handlerStmt)) {
            exceptionalSuccs.add(handlerStmt);
            queue.add(handlerStmt);
          }
        }
      }
    }
    return exceptionalSuccs;
  }
}
