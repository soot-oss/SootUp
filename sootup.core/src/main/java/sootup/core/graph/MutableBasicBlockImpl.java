package sootup.core.graph;

import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Markus Schmidt
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

/**
 * Block of a {@link MutableBlockControlFlowGraph}.
 *
 * <p>Edges are changed only via the package-private link primitives, which always update both ends:
 * normal edges ({@link #link}/{@link #unlink}) and exceptional edges ({@link #linkExc}/{@link
 * #unlinkExc}). Normal and exceptional predecessors are kept apart, one entry per edge, so removing
 * an edge removes exactly one entry.
 *
 * <p>The successor array always has the size of the tail's expected successor count; unset slots
 * are null.
 */
public class MutableBasicBlockImpl implements MutableBasicBlock {

  private static final MutableBasicBlockImpl[] NO_SUCCESSORS = new MutableBasicBlockImpl[0];

  @NonNull private final ArrayList<Stmt> stmts = new ArrayList<>();

  /** slot i: i-th successor of the tail, may be null */
  @NonNull private MutableBasicBlockImpl[] successors = NO_SUCCESSORS;

  /** one entry per normal edge into this block */
  @NonNull private final ArrayList<MutableBasicBlockImpl> normalPreds = new ArrayList<>(2);

  /** one entry per exceptional edge (block, exception type) into this block */
  @NonNull private final ArrayList<MutableBasicBlockImpl> exceptionalPreds = new ArrayList<>(0);

  @NonNull
  private final Map<ClassType, MutableBasicBlockImpl> exceptionalSuccessors = new HashMap<>();

  // cached read views, reset on change
  @Nullable private List<MutableBasicBlock> predecessorView;
  @Nullable private List<MutableBasicBlock> successorView;

  public MutableBasicBlockImpl() {}

  /** standalone block with the given stmts; exceptional successors are linked both ways */
  public MutableBasicBlockImpl(
      @NonNull List<Stmt> stmts, @NonNull Map<ClassType, MutableBasicBlock> exceptionMap) {
    stmts.forEach(this::addStmt);
    exceptionMap.forEach((type, block) -> linkExc(type, (MutableBasicBlockImpl) block));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ForwardingBasicBlock) {
      return o.equals(this);
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  // ===========================================================================================
  // read API

  @NonNull
  @Override
  public List<MutableBasicBlock> getPredecessors() {
    List<MutableBasicBlock> view = predecessorView;
    if (view == null) {
      List<MutableBasicBlock> l = new ArrayList<>(normalPreds.size() + exceptionalPreds.size());
      l.addAll(normalPreds);
      l.addAll(exceptionalPreds);
      view = predecessorView = Collections.unmodifiableList(l);
    }
    return view;
  }

  @NonNull
  @Override
  public List<MutableBasicBlock> getSuccessors() {
    if (stmts.isEmpty()) {
      throw new IllegalStateException(
          "Can't determine how many successors would be allowed as this block contains no Stmts(, yet).");
    }
    List<MutableBasicBlock> view = successorView;
    if (view == null) {
      List<MutableBasicBlock> l = new ArrayList<>(successors.length);
      for (MutableBasicBlockImpl s : successors) {
        if (s != null) {
          l.add(s);
        }
      }
      view = successorView = Collections.unmodifiableList(l);
    }
    return view;
  }

  @Override
  public Map<ClassType, MutableBasicBlock> getExceptionalPredecessors() {
    Map<ClassType, MutableBasicBlock> excPreds = new HashMap<>();
    for (MutableBasicBlockImpl pred : distinct(exceptionalPreds)) {
      pred.exceptionalSuccessors.forEach(
          (type, handler) -> {
            if (handler == this) {
              excPreds.put(type, pred);
            }
          });
    }
    return excPreds;
  }

  @NonNull
  @Override
  public Map<ClassType, MutableBasicBlock> getExceptionalSuccessors() {
    return Collections.unmodifiableMap(exceptionalSuccessors);
  }

  @NonNull
  @Override
  public List<Stmt> getStmts() {
    return Collections.unmodifiableList(stmts);
  }

  @Override
  public int getStmtCount() {
    return stmts.size();
  }

  @NonNull
  @Override
  public Stmt getHead() {
    if (stmts.isEmpty()) {
      throw new IndexOutOfBoundsException("Cant get the head - this Block has no assigned Stmts.");
    }
    return stmts.get(0);
  }

  @NonNull
  @Override
  public Stmt getTail() {
    int size = stmts.size();
    if (size < 1) {
      throw new IndexOutOfBoundsException("Cant get the tail - this Block has no assigned Stmts.");
    }
    return stmts.get(size - 1);
  }

  @Override
  public String toString() {
    return "Block " + getStmts();
  }

  // ===========================================================================================
  // stmts (package-private; the graph keeps its stmt -> block index in sync)

  int size() {
    return stmts.size();
  }

  Stmt get(int idx) {
    return stmts.get(idx);
  }

  /** identity based index of stmt or -1 */
  int indexOf(@NonNull Stmt stmt) {
    for (int i = 0, n = stmts.size(); i < n; i++) {
      if (stmts.get(i) == stmt) {
        return i;
      }
    }
    return -1;
  }

  /** appends stmt as new tail; the current tail must fall through and have no successor set */
  void addStmt(@NonNull Stmt newStmt) {
    if (!stmts.isEmpty()) {
      Stmt tail = getTail();
      if (tail instanceof BranchingStmt || !(tail instanceof FallsThroughStmt)) {
        throw new IllegalArgumentException(
            "Can't add another Stmt to a Block after a BranchingStmt.");
      }
      if (hasSuccessor()) {
        throw new IllegalStateException("unlink the successor of the tail before appending");
      }
    }
    stmts.add(newStmt);
    adjustArity();
  }

  /** puts stmts in front of the head; they must all fall through and not branch */
  void prependStmts(@NonNull List<Stmt> front) {
    for (Stmt s : front) {
      if (s instanceof BranchingStmt || !(s instanceof FallsThroughStmt)) {
        throw new IllegalArgumentException("only the tail of a Block may branch or end the flow");
      }
    }
    stmts.addAll(0, front);
    if (stmts.size() == front.size()) {
      adjustArity();
    }
  }

  /** removes the stmt at idx; removing the tail keeps the successor slots the new tail can hold */
  Stmt removeStmt(int idx) {
    Stmt removed = stmts.remove(idx);
    if (idx == stmts.size()) {
      adjustArity();
    }
    return removed;
  }

  /** removes and returns the stmts [from, size); the block's successors must be unlinked */
  List<Stmt> removeStmtsFrom(int from) {
    if (hasSuccessor()) {
      throw new IllegalStateException("unlink the successors before truncating");
    }
    List<Stmt> tail = stmts.subList(from, stmts.size());
    List<Stmt> removed = new ArrayList<>(tail);
    tail.clear();
    adjustArity();
    return removed;
  }

  /**
   * replaces the stmt at idx; replacing the tail keeps the successor slots the new tail can hold
   */
  void setStmt(int idx, @NonNull Stmt stmt) {
    if (idx < stmts.size() - 1
        && (stmt instanceof BranchingStmt || !(stmt instanceof FallsThroughStmt))) {
      throw new IllegalArgumentException("only the tail of a Block may branch or end the flow");
    }
    stmts.set(idx, stmt);
    if (idx == stmts.size() - 1) {
      adjustArity();
    }
  }

  /** resizes the successor array to the tail's expected count; dropped slots are unlinked */
  private void adjustArity() {
    int n = stmts.isEmpty() ? 0 : getTail().getExpectedSuccessorCount();
    if (n == successors.length) {
      return;
    }
    for (int i = n; i < successors.length; i++) {
      unlink(i);
    }
    MutableBasicBlockImpl[] s = n == 0 ? NO_SUCCESSORS : new MutableBasicBlockImpl[n];
    System.arraycopy(successors, 0, s, 0, Math.min(n, successors.length));
    successors = s;
    successorView = null;
  }

  // ===========================================================================================
  // normal edges

  int successorSlots() {
    return successors.length;
  }

  @Nullable MutableBasicBlockImpl successorAt(int idx) {
    return successors[idx];
  }

  boolean hasSuccessor() {
    for (MutableBasicBlockImpl s : successors) {
      if (s != null) {
        return true;
      }
    }
    return false;
  }

  /** sets successor slot idx to target (replacing and unlinking a previous one) */
  void link(int idx, @NonNull MutableBasicBlockImpl target) {
    if (idx < 0 || idx >= successors.length) {
      throw new IndexOutOfBoundsException(
          "successorIdx '"
              + idx
              + "' is out of bounds ('"
              + successors.length
              + " for "
              + (stmts.isEmpty() ? "<empty>" : getTail())
              + "')");
    }
    MutableBasicBlockImpl old = successors[idx];
    if (old == target) {
      return;
    }
    if (old != null) {
      removeOne(old.normalPreds, this);
      old.predecessorView = null;
    }
    successors[idx] = target;
    target.normalPreds.add(this);
    target.predecessorView = null;
    successorView = null;
  }

  /** clears successor slot idx */
  void unlink(int idx) {
    MutableBasicBlockImpl old = successors[idx];
    if (old != null) {
      successors[idx] = null;
      removeOne(old.normalPreds, this);
      old.predecessorView = null;
      successorView = null;
    }
  }

  void unlinkAllSuccessors() {
    for (int i = 0; i < successors.length; i++) {
      unlink(i);
    }
  }

  /** clears every slot pointing to target; returns the cleared indices */
  List<Integer> unlinkAllTo(@NonNull MutableBasicBlockImpl target) {
    List<Integer> idx = new ArrayList<>(1);
    for (int i = 0; i < successors.length; i++) {
      if (successors[i] == target) {
        unlink(i);
        idx.add(i);
      }
    }
    return idx;
  }

  /** the normal predecessors, one entry per edge (internal, do not modify) */
  List<MutableBasicBlockImpl> normalPredecessors() {
    return normalPreds;
  }

  // ===========================================================================================
  // exceptional edges

  @Nullable MutableBasicBlockImpl exceptionalSuccessor(@NonNull ClassType type) {
    return exceptionalSuccessors.get(type);
  }

  Map<ClassType, MutableBasicBlockImpl> exceptionalSuccessorMap() {
    return exceptionalSuccessors;
  }

  void linkExc(@NonNull ClassType type, @NonNull MutableBasicBlockImpl handler) {
    MutableBasicBlockImpl old = exceptionalSuccessors.put(type, handler);
    if (old == handler) {
      return;
    }
    if (old != null) {
      removeOne(old.exceptionalPreds, this);
      old.predecessorView = null;
    }
    handler.exceptionalPreds.add(this);
    handler.predecessorView = null;
  }

  /** returns false if there was no exceptional successor for type */
  boolean unlinkExc(@NonNull ClassType type) {
    MutableBasicBlockImpl old = exceptionalSuccessors.remove(type);
    if (old == null) {
      return false;
    }
    removeOne(old.exceptionalPreds, this);
    old.predecessorView = null;
    return true;
  }

  void unlinkAllExc() {
    for (ClassType type : new ArrayList<>(exceptionalSuccessors.keySet())) {
      unlinkExc(type);
    }
  }

  /** removes every exceptional edge to handler */
  void unlinkExcTo(@NonNull MutableBasicBlockImpl handler) {
    List<ClassType> types = new ArrayList<>(1);
    exceptionalSuccessors.forEach(
        (t, h) -> {
          if (h == handler) {
            types.add(t);
          }
        });
    types.forEach(this::unlinkExc);
  }

  void copyExcFrom(@NonNull MutableBasicBlockImpl source) {
    source.exceptionalSuccessors.forEach(this::linkExc);
  }

  boolean hasSameExceptionalSuccessors(@NonNull MutableBasicBlockImpl other) {
    if (exceptionalSuccessors.size() != other.exceptionalSuccessors.size()) {
      return false;
    }
    for (Map.Entry<ClassType, MutableBasicBlockImpl> e : exceptionalSuccessors.entrySet()) {
      if (other.exceptionalSuccessors.get(e.getKey()) != e.getValue()) {
        return false;
      }
    }
    return true;
  }

  /** the exceptional predecessors, one entry per (block, type) edge (internal, do not modify) */
  List<MutableBasicBlockImpl> exceptionalPredecessors() {
    return exceptionalPreds;
  }

  // ===========================================================================================

  /** removes the last identical occurrence */
  private static void removeOne(ArrayList<MutableBasicBlockImpl> l, MutableBasicBlockImpl b) {
    for (int i = l.size() - 1; i >= 0; i--) {
      if (l.get(i) == b) {
        l.remove(i);
        return;
      }
    }
    throw new IllegalStateException("predecessor entry missing - edge bookkeeping out of sync");
  }

  /** identity-distinct copy, keeps first-occurrence order */
  static List<MutableBasicBlockImpl> distinct(List<MutableBasicBlockImpl> l) {
    if (l.size() <= 1) {
      return new ArrayList<>(l);
    }
    Set<MutableBasicBlockImpl> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    List<MutableBasicBlockImpl> res = new ArrayList<>(l.size());
    for (MutableBasicBlockImpl b : l) {
      if (seen.add(b)) {
        res.add(b);
      }
    }
    return res;
  }
}
