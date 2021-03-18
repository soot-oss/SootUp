package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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

import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Body.BodyBuilder;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable (definition)
 * that are independent of each other.
 *
 * <p>For example the code:
 *
 * <pre>
 *    l0 := @this Test
 *    l1 = 0
 *    l2 = 1
 *    l1 = l1 + 1
 *    l2 = l2 + 1
 *    return
 * </pre>
 *
 * to:
 *
 * <pre>
 *    l0 := @this Test
 *    l1#1 = 0
 *    l2#2 = 1
 *    l1#3 = l1#1 + 1
 *    l2#4 = l2#2 + 1
 *    return
 * </pre>
 *
 * @author Zun Wang
 */
public class LocalSplitter implements BodyInterceptor {

  @Override
  @Nonnull
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    // Find all Locals that must be split
    // If a local as a definition appears two or more times, then this local must be split
    List<Stmt> stmts = builder.getStmts();
    Set<Local> visitedLocals = new LinkedHashSet<>();
    Set<Local> toSplitLocals = new LinkedHashSet<>();
    for (Stmt stmt : stmts) {
      if (!stmt.getDefs().isEmpty()) {
        Value def = stmt.getDefs().get(0);
        if (def instanceof Local) {
          if (visitedLocals.contains(def)) {
            toSplitLocals.add((Local) def);
          }
          visitedLocals.add((Local) def);
        }
      }
    }

    StmtGraph graph = builder.getStmtGraph();
    ExceptionalStmtGraph exceptionalGraph = new ExceptionalStmtGraph(graph);

    // Create a new Local-Set for the modified new body.
    Set<Local> newLocals = new LinkedHashSet<>(builder.getLocals());
    int localIndex = 1;

    // Use bodyBuilder's iterator to create visitList, in order to control the visit-order of Stmt
    // in StmtGraph
    Iterator<Stmt> graphIterator = graph.iterator();
    List<Stmt> visitList = new ArrayList<>();
    while (graphIterator.hasNext()) {
      visitList.add(graphIterator.next());
    }

    // Start to iterate the visitList:
    while (!visitList.isEmpty()) {
      // Remove the first(visited) Stmt in visitList, to avoid visiting a Stmt twice.
      Stmt visitedStmt = visitList.remove(0);

      // At first Check the definition(left side) of the visitedStmt is a local which must be split:
      if ((!visitedStmt.getDefs().isEmpty())
          && visitedStmt.getDefs().get(0) instanceof Local
          && toSplitLocals.contains(visitedStmt.getDefs().get(0))) {
        // then assign a new name to the oriLocal to get a new local which is called newLocal
        Local oriLocal = (Local) visitedStmt.getDefs().get(0);
        Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + localIndex);
        newLocals.add(newLocal);
        localIndex++;
        // create a new Stmt whose definition is replaced with the newLocal,
        Stmt newVisitedStmt = BodyUtils.withNewDef(visitedStmt, newLocal);
        // replace the visited Stmt in the StmtGraph with the new Stmt,
        builder.replaceStmt(visitedStmt, newVisitedStmt);
        exceptionalGraph.replaceNode(visitedStmt, newVisitedStmt);
        // replace the corresponding Stmt in traps and visitList with the new Stmt.
        adaptTraps(builder, visitedStmt, newVisitedStmt);
        adaptVisitList(visitList, visitedStmt, newVisitedStmt);

        // Build the forwardsQueue which is used to iterate all Stmts before the orilocal is
        // defined again.
        // The direction of iteration is from root of the StmtGraph to the leaves. So the
        // successors of the new Stmt are added into the forwardsQueue.
        Deque<Stmt> forwardsQueue =
            new ArrayDeque<>(builder.getStmtGraph().successors(newVisitedStmt));
        // Create the visitedUsesStmt to store the visited Stmt for the forwardsQueue, to avoid, a
        // Stmt is added twice into the forwardQueue.
        Set<Stmt> visitedUsesStmt = new HashSet<>();

        // Start to iterate the forwardsQueue:
        while (!forwardsQueue.isEmpty()) {
          // Remove the head from the forwardsQueue:
          Stmt head = forwardsQueue.remove();
          visitedUsesStmt.add(head);

          // 1.case: if useList of head contains oriLocal, then replace the oriLocal with
          // newLocal.
          if (head.getUses().contains(oriLocal)) {
            Stmt newHead = BodyUtils.withNewUse(head, oriLocal, newLocal);
            builder.replaceStmt(head, newHead);
            exceptionalGraph.replaceNode(head, newHead);
            adaptTraps(builder, head, newHead);
            adaptVisitList(visitList, head, newHead);
            // if head doesn't define the the oriLocal again, then add all successors which are
            // not in forwardsQueue and visitedUsesStmt, into the forwardsQueue.
            if (newHead.getDefs().isEmpty() || !newHead.getDefs().get(0).equivTo(oriLocal)) {
              for (Stmt succ : builder.getStmtGraph().successors(newHead)) {
                if (!visitedUsesStmt.contains(succ) && !forwardsQueue.contains(succ)) {
                  forwardsQueue.addLast(succ);
                }
              }
            }
          }

          // 2.case: if useList of head contains the modified orilocal, so a conflict maybe arise,
          // then trace the StmtGraph backwards to resolve the conflict.
          else if (hasModifiedUse(head, oriLocal)) {

            Local modifiedLocal = getModifiedUse(head, oriLocal);
            // if modifed name is not same as the newLocal's name then -> conflict arises -> trace
            // backwards
            if (!modifiedLocal.getName().equals(newLocal.getName())) {
              localIndex--;

              // Build the backwardsQueue which is used to iterate all Stmts between head and the
              // Stmts which define the oriLocal in last time.
              // The direction of iteration is from leave of the StmtGraph to the root. So the
              // predecessors of head are added into the BackwardsQueue.
              Deque<Stmt> backwardsQueue =
                  new ArrayDeque<>(builder.getStmtGraph().predecessors(head));

              // Start to iterator the backwardQueue:
              while (!backwardsQueue.isEmpty()) {
                // Remove the first Stmt of backwardQueue, and name it as backStmt.
                Stmt backStmt = backwardsQueue.remove();

                // 2.1 case: if backStmt's definition is the modified and has a higher
                // local-name-index than the modifiedLocal of head
                // then replace the definition of backStmt with the modifiedLocal of head, and
                // remove the corresponding Local(definition of backStmt) from the set: newLocals
                if (hasModifiedDef(backStmt, oriLocal)) {
                  if (hasHigherLocalName((Local) backStmt.getDefs().get(0), modifiedLocal)) {
                    Stmt newBackStmt = BodyUtils.withNewDef(backStmt, modifiedLocal);
                    builder.replaceStmt(backStmt, newBackStmt);
                    exceptionalGraph.replaceNode(backStmt, newBackStmt);
                    adaptTraps(builder, backStmt, newBackStmt);
                    adaptVisitList(visitList, backStmt, newBackStmt);
                    newLocals.remove(newLocal);
                  }
                }
                // 2.2 case: if backStmt's useList contains the modified oriLocal, and this
                // modified oriLocal has a higher local-name-index that the modifiedLocal of head
                // then replace the corresponding use of backStmt with modifiedLocal of head, and
                // add all predecessors of the backStmt into the backwardsQueue.
                else if (hasModifiedUse(backStmt, oriLocal)) {
                  Local modifiedUse = getModifiedUse(backStmt, oriLocal);
                  if (hasHigherLocalName(modifiedUse, modifiedLocal)) {
                    Stmt newBackStmt = BodyUtils.withNewUse(backStmt, modifiedUse, modifiedLocal);
                    builder.replaceStmt(backStmt, newBackStmt);
                    exceptionalGraph.replaceNode(backStmt, newBackStmt);
                    adaptTraps(builder, backStmt, newBackStmt);
                    adaptVisitList(visitList, backStmt, newBackStmt);
                    backwardsQueue.addAll(builder.getStmtGraph().predecessors(newBackStmt));
                  }
                }
                // 2.3 case: if there's no relationship between backStmt's definition/useList and
                // oriLocal, then add all predecessors of the backStmt into the backwardsQueue.
                else {
                  backwardsQueue.addAll(builder.getStmtGraph().predecessors(backStmt));
                }
              }
            }
          }
          // 3.case: if uselist of head contains neither orilocal nor the modified orilocal,
          // then add all successors of head which are not in forwardsQueue and visitedUsesStmt,
          // into the forwardsQueue.
          else {
            if (head.getDefs().isEmpty() || !head.getDefs().get(0).equivTo(oriLocal)) {
              for (Stmt succ : builder.getStmtGraph().successors(head)) {
                if (!visitedUsesStmt.contains(succ) && !forwardsQueue.contains(succ)) {
                  forwardsQueue.addLast(succ);
                }
              }
            }
          }
        }
        // Then check the useList of the visitedStmt:
      } else {
        // For each Local(oriL) which is to be split, check whether it is used in the visitedStmt
        // without definition before.
        // We define a StmtGraph consists of a mainStmtGraph and none or more trapStmtGraphs.
        // This situation could arise just in a trapStmtGraph.
        for (Local oriL : toSplitLocals) {
          // If so:
          // 1.step: find out all trapStmtGraphs' root(handlerStmts) which contain the visitedGraph,
          // namely a set of handlerStmts.
          // 2.step: find out all stmts whose exceptional destination-traps are with the found
          // handlerStmts.
          // 3.step: iterate these stmts, find a modified oriL((Local) with a maximum name index.
          // 4.step: Use this modified oriL to modify the visitedStmt

          if (visitedStmt.getUses().contains(oriL)) {
            // 1.step
            Set<Stmt> handlerStmts = traceHandlerStmts(visitedStmt, builder);
            Set<Stmt> stmtsWithDests = new HashSet<>();
            // 2.step
            for (Stmt handlerStmt : handlerStmts) {
              List<Stmt> preds = exceptionalGraph.exceptionalPredecessors(handlerStmt);
              for (Stmt pred : preds) {
                List<Trap> dests = exceptionalGraph.getDestTrap(pred);
                List<Stmt> destHandlerStmts = new ArrayList<>();
                dests.forEach(dest -> destHandlerStmts.add(dest.getHandlerStmt()));
                if (destHandlerStmts.contains(handlerStmt)) {
                  stmtsWithDests.add(pred);
                }
              }
            }
            // 3.step
            Local lastChange = null;
            for (Stmt stmt : stmtsWithDests) {
              if (hasModifiedDef(stmt, oriL)) {
                Local modifiedLocal = (Local) stmt.getDefs().get(0);
                if (lastChange == null || hasHigherLocalName(modifiedLocal, lastChange)) {
                  lastChange = modifiedLocal;
                }
              }
            }
            // 4.step
            Stmt newVisitedStmt = BodyUtils.withNewUse(visitedStmt, oriL, lastChange);
            builder.replaceStmt(visitedStmt, newVisitedStmt);
            exceptionalGraph.replaceNode(visitedStmt, newVisitedStmt);
            adaptTraps(builder, visitedStmt, newVisitedStmt);
            adaptVisitList(visitList, visitedStmt, newVisitedStmt);
          }
        }
      }
    }
    builder.setLocals(newLocals);
  }

  // ******************assist_functions*************************

  /**
   * Fit the modified stmt in Traps
   *
   * @param builder a bodybuilder, use it to modify Trap
   * @param oldStmt a Stmt which maybe a beginStmt or endStmt in a Trap
   * @param newStmt a modified stmt to replace the oldStmt.
   */
  private void adaptTraps(
      @Nonnull BodyBuilder builder, @Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    List<Trap> traps = new ArrayList<>(builder.getStmtGraph().getTraps());
    for (ListIterator<Trap> iterator = traps.listIterator(); iterator.hasNext(); ) {
      Trap trap = iterator.next();
      JTrap jtrap = (JTrap) trap;
      if (oldStmt.equivTo(trap.getBeginStmt())) {
        Trap newTrap = jtrap.withBeginStmt(newStmt);
        iterator.set(newTrap);
      } else if (oldStmt.equivTo(trap.getEndStmt())) {
        Trap newTrap = jtrap.withEndStmt(newStmt);
        iterator.set(newTrap);
      }
    }
    builder.setTraps(traps);
  }

  /**
   * Fit the modified Stmt in visitedList
   *
   * @param visitList a list storing all Stmts which are not yet visited.
   * @param oldStmt a stmt which is modified.
   * @param newStmt a modified stmt to replace the oldStmt.
   */
  private void adaptVisitList(
      @Nonnull List<Stmt> visitList, @Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    final int index = visitList.indexOf(oldStmt);
    if (index > -1) {
      visitList.set(index, newStmt);
    }
  }

  /**
   * Check whether a Stmt's useList contains the given modified oriLocal.
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  private boolean hasModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    if (!stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        return isLocalFromSameOrigin(oriLocal, use);
      }
    }
    return false;
  }

  /**
   * Get the modified Local if a Stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked.
   * @return if so, return this modified local, else return null
   */
  @Nullable
  private Local getModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    if (hasModifiedUse(stmt, oriLocal)) {
      List<Value> useList = stmt.getUses();
      if (!useList.isEmpty()) {
        for (Value use : useList) {
          if (isLocalFromSameOrigin(oriLocal, use)) {
            return (Local) use;
          }
        }
      }
    }
    return null;
  }

  /**
   * Check whether a local is modified from the given oriLocal
   *
   * @param local: a local is to be checked
   * @param oriLocal: the given oriLocal
   * @return if so, return true, else return false.
   */
  @Nonnull
  private boolean isLocalFromSameOrigin(@Nonnull Local oriLocal, Value local) {
    if (local instanceof Local) {
      final String name = ((Local) local).getName();
      final String origName = oriLocal.getName();
      final int origLength = origName.length();
      return name.startsWith(origName)
          && name.length() > origLength
          && name.charAt(origLength) == '#';
    }
    return false;
  }

  /**
   * Check whether a Stmt's def is the modified oriLocal
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  private boolean hasModifiedDef(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
      return isLocalFromSameOrigin(oriLocal, stmt.getDefs().get(0));
    }
    return false;
  }

  /**
   * Check whether leftLocal's name has higher index than rightLocal's.
   *
   * @param leftLocal: a local in form oriLocal#num1
   * @param rightLocal: a local in form oriLocal#num2
   * @return if so return true, else return false
   */
  private boolean hasHigherLocalName(@Nonnull Local leftLocal, @Nonnull Local rightLocal) {
    String leftName = leftLocal.getName();
    String rightName = rightLocal.getName();
    int i = leftName.lastIndexOf('#');
    int j = rightName.lastIndexOf('#');
    int leftNum = Integer.parseInt(leftName.substring(i + 1));
    int rightNum = Integer.parseInt(rightName.substring(j + 1));
    return leftNum > rightNum;
  }

  /**
   * A given stmt maybe in one or several trapStmtGraphs, return these trapStmtGraphs' handlerStmt
   *
   * @param stmt a given stmt which is in one or several trapStmtGraphs
   * @param bodyBuilder use its graph to trace handlerStmts
   * @return a set of handlerStmts
   */
  @Nonnull
  private Set<Stmt> traceHandlerStmts(@Nonnull Stmt stmt, @Nonnull BodyBuilder bodyBuilder) {

    Set<Stmt> handlerStmts = new HashSet<>();

    StmtGraph graph = bodyBuilder.getStmtGraph();

    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(stmt);
    while (!queue.isEmpty()) {
      Stmt first = queue.removeFirst();
      if (graph.predecessors(first).isEmpty()) {
        handlerStmts.add(first);
      } else {
        graph.predecessors(first).forEach(pred -> queue.add(pred));
      }
    }
    return handlerStmts;
  }
}
