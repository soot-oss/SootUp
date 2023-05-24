package sootup.java.bytecode.interceptors;

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

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

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
  // FIXME: [ms] assumes that names of Locals do not contain a '#' already -> could lead to problems
  // TODO: [ms] check equivTo()'s - I guess they can be equals()'s - or even: '=='s

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    // Find all Locals that must be split
    // If a local as a definition appears two or more times, then this local must be split
    List<Stmt> stmts = builder.getStmts();
    Set<Local> visitedLocals = new LinkedHashSet<>();
    Set<Local> toSplitLocals = new LinkedHashSet<>();
    for (Stmt stmt : stmts) {
      final List<Value> defs = stmt.getDefs();
      if (!defs.isEmpty()) {
        Value def = defs.get(0);
        if (def instanceof Local) {
          if (visitedLocals.contains(def)) {
            toSplitLocals.add((Local) def);
          }
          visitedLocals.add((Local) def);
        }
      }
    }

    StmtGraph<?> graph = builder.getStmtGraph();

    // Create a new Local-Set for the modified new body.
    Set<Local> newLocals = new LinkedHashSet<>(builder.getLocals());
    int localIndex = 1;

    // iterate stmts
    while (!stmts.isEmpty()) {
      Stmt currentStmt = stmts.remove(0);
      // At first Check the definition(left side) of the currentStmt is a local which must be split:
      final List<Value> defs = currentStmt.getDefs();
      if (!defs.isEmpty() && defs.get(0) instanceof Local && toSplitLocals.contains(defs.get(0))) {
        // then assign a new name to the oriLocal to get a new local which is called newLocal
        Local oriLocal = (Local) defs.get(0);
        Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + localIndex);
        newLocals.add(newLocal);
        localIndex++;

        // create newStmt whose definition is replaced with the newLocal,
        Stmt newStmt = ((AbstractDefinitionStmt<?, ?>) currentStmt).withNewDef(newLocal);
        // replace corresponding oldStmt with newStmt in builder
        replaceStmtInBuilder(builder, stmts, currentStmt, newStmt);

        // Build the forwardsQueue which is used to iterate all Stmts before the orilocal is defined
        // again.
        // The direction of iteration is from root of the StmtGraph to leafs. So the successors of
        // the newStmt are added into the forwardsQueue.
        Deque<Stmt> forwardsQueue = new ArrayDeque<>(graph.successors(newStmt));
        // Create the visitedStmt to store the visited Stmts for the forwardsQueue, to avoid, a
        // Stmt is added twice into the forwardQueue.
        Set<Stmt> visitedStmts = new HashSet<>();

        while (!forwardsQueue.isEmpty()) {
          Stmt head = forwardsQueue.remove();
          visitedStmts.add(head);

          // 1.case: if useList of head contains oriLocal, then replace the oriLocal with
          // newLocal.
          if (head.getUses().contains(oriLocal)) {
            Stmt newHead = head.withNewUse(oriLocal, newLocal);
            replaceStmtInBuilder(builder, stmts, head, newHead);

            // if head doesn't define the the oriLocal again, then add all successors which are
            // not in forwardsQueue and visitedUsesStmt, into the forwardsQueue.
            if (newHead.getDefs().isEmpty() || !newHead.getDefs().get(0).equivTo(oriLocal)) {
              for (Stmt succ : graph.successors(newHead)) {
                if (!visitedStmts.contains(succ) && !forwardsQueue.contains(succ)) {
                  forwardsQueue.addLast(succ);
                }
              }
            }
          }

          // 2.case: if uses of head contains the modified orilocal, so a conflict maybe arise,
          // then trace the StmtGraph backwards to resolve the conflict.
          else if (hasModifiedUse(head, oriLocal)) {

            Local modifiedLocal = getModifiedUse(head, oriLocal);
            if (modifiedLocal == null) {
              throw new IllegalStateException("Modified Use is not found.");
            }

            // if modifed name is not same as the newLocal's name then -> conflict arises -> trace
            // backwards
            if (!modifiedLocal.getName().equals(newLocal.getName())) {
              localIndex--;

              // Build the backwardsQueue which is used to iterate all Stmts between head and the
              // Stmts which define the oriLocal in last time.
              // The direction of iteration is from leave of the StmtGraph to the root. So the
              // predecessors of head are added into the BackwardsQueue.
              Deque<Stmt> backwardsQueue = new ArrayDeque<>(graph.predecessors(head));

              while (!backwardsQueue.isEmpty()) {
                // Remove the first Stmt of backwardQueue, and name it as backStmt.
                Stmt backStmt = backwardsQueue.remove();

                // 2.1 case: if backStmt's definition is the modified and has a higher
                // local-name-index than the modifiedLocal of head
                // then replace the definition of backStmt with the modifiedLocal of head, and
                // remove the corresponding Local(definition of backStmt) from the set: newLocals
                if (hasModifiedDef(backStmt, oriLocal)) {
                  if (hasHigherLocalName((Local) backStmt.getDefs().get(0), modifiedLocal)) {
                    Stmt newBackStmt =
                        ((AbstractDefinitionStmt<?, ?>) backStmt).withNewDef(modifiedLocal);
                    replaceStmtInBuilder(builder, stmts, backStmt, newBackStmt);
                    newLocals.remove(newLocal);
                  }
                }
                // 2.2 case: if backStmt's uses contains the modified oriLocal, and this
                // modified oriLocal has a higher local-name-index that the modifiedLocal of head
                // then replace the corresponding use of backStmt with modifiedLocal of head, and
                // add all predecessors of the backStmt into the backwardsQueue.
                else if (hasModifiedUse(backStmt, oriLocal)) {
                  Local modifiedUse = getModifiedUse(backStmt, oriLocal);
                  if (hasHigherLocalName(modifiedUse, modifiedLocal)) {
                    Stmt newBackStmt = backStmt.withNewUse(modifiedUse, modifiedLocal);
                    replaceStmtInBuilder(builder, stmts, backStmt, newBackStmt);
                    backwardsQueue.addAll(graph.predecessors(newBackStmt));
                  }
                }
                // 2.3 case: if there's no relationship between backStmt's defs/uses and
                // oriLocal, then add all predecessors of the backStmt into the backwardsQueue.
                else {
                  backwardsQueue.addAll(graph.predecessors(backStmt));
                }
              }
            }
          }
          // 3.case: if uses of head contains neither orilocal nor the modified orilocal,
          // then add all successors of head which are not in forwardsQueue and visitedStmts,
          // into the forwardsQueue.
          else {
            final List<Value> headDefs = head.getDefs();
            if (headDefs.isEmpty() || !headDefs.get(0).equivTo(oriLocal)) {
              for (Stmt succ : graph.successors(head)) {
                if (!visitedStmts.contains(succ) && !forwardsQueue.contains(succ)) {
                  forwardsQueue.addLast(succ);
                }
              }
            }
          }
        }
        // Then check the uses of currentStmt:
      } else {
        // For each Local(oriL) which is to be split, check whether it is used in currentStmt
        // without definition before.
        // We define a StmtGraph consists of a mainStmtGraph and none or more trapStmtGraphs.
        // This situation could arise just in a trapStmtGraph.
        for (Local oriLocal : toSplitLocals) {
          // If so:
          // 1.step: find out all trapStmtGraphs' root(handlerStmts) which contain currentStmt,
          // namely a set of handlerStmts.
          // 2.step: find out all stmts whose exceptional destination-traps are with the found
          // handlerStmts.
          // 3.step: iterate these stmts, find a modified oriL((Local) with a maximum name index.
          // 4.step: Use this modified oriL to modify the visitedStmt
          if (currentStmt.getUses().contains(oriLocal)) {
            // 1.step:
            Set<Stmt> handlerStmts = traceHandlerStmts(graph, currentStmt);
            // 2.step:
            Set<Stmt> stmtsWithDests = new HashSet<>();
            for (Stmt handlerStmt : handlerStmts) {
              for (Stmt exceptionalPred : graph.predecessors(handlerStmt)) {
                Map<ClassType, Stmt> dests = graph.exceptionalSuccessors(exceptionalPred);
                List<Stmt> destHandlerStmts = new ArrayList<>();
                dests.forEach((key, dest) -> destHandlerStmts.add(dest));
                if (destHandlerStmts.contains(handlerStmt)) {
                  stmtsWithDests.add(exceptionalPred);
                }
              }
            }
            // 3.step:
            Local lastChange = null;
            for (Stmt stmt : stmtsWithDests) {
              if (hasModifiedDef(stmt, oriLocal)) {
                Local modifiedLocal = (Local) stmt.getDefs().get(0);
                if (lastChange == null || hasHigherLocalName(modifiedLocal, lastChange)) {
                  lastChange = modifiedLocal;
                }
              }
            }
            // 4.step:
            if (lastChange != null) {
              Stmt newStmt = currentStmt.withNewUse(oriLocal, lastChange);
              replaceStmtInBuilder(builder, stmts, currentStmt, newStmt);
            }
          }
        }
      }
    }
    builder.setLocals(newLocals);
  }

  // ******************assist_functions*************************

  /**
   * Replace corresponding oldStmt with newStmt in BodyBuilder and visitList
   *
   * @param builder
   * @param stmtIterationList
   * @param oldStmt
   * @param newStmt
   */
  private void replaceStmtInBuilder(
      @Nonnull BodyBuilder builder,
      @Nonnull List<Stmt> stmtIterationList,
      @Nonnull Stmt oldStmt,
      @Nonnull Stmt newStmt) {

    builder.replaceStmt(oldStmt, newStmt);

    // adapt VisitList
    final int index = stmtIterationList.indexOf(oldStmt);
    if (index > -1) {
      stmtIterationList.set(index, newStmt);
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
    for (Value use : stmt.getUses()) {
      return isLocalFromSameOrigin(oriLocal, use);
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
      for (Value use : useList) {
        if (isLocalFromSameOrigin(oriLocal, use)) {
          return (Local) use;
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
  private boolean isLocalFromSameOrigin(@Nonnull Local oriLocal, Value local) {
    if (local instanceof Local) {
      final String name = ((Local) local).getName();
      final String origName = oriLocal.getName();
      final int origLength = origName.length();
      return name.length() > origLength
          && name.startsWith(origName)
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
    final List<Value> defs = stmt.getDefs();
    if (!defs.isEmpty() && defs.get(0) instanceof Local) {
      return isLocalFromSameOrigin(oriLocal, defs.get(0));
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
    int lIdx = leftName.lastIndexOf('#');
    int rIdx = rightName.lastIndexOf('#');
    int leftNum = Integer.parseInt(leftName.substring(lIdx + 1));
    int rightNum = Integer.parseInt(rightName.substring(rIdx + 1));
    return leftNum > rightNum;
  }

  /**
   * A given entryStmt may be in one or several trapStmtGraphs, return these trapStmtGraphs'
   * handlerStmts
   *
   * @param entryStmt a given entryStmt which is in one or several trapStmtGraphs
   * @param graph to trace handlerStmts
   * @return a set of handlerStmts
   */
  @Nonnull
  private Set<Stmt> traceHandlerStmts(@Nonnull StmtGraph<?> graph, @Nonnull Stmt entryStmt) {

    Set<Stmt> handlerStmts = new HashSet<>();

    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(entryStmt);
    while (!queue.isEmpty()) {
      Stmt stmt = queue.removeFirst();
      if (stmt instanceof JIdentityStmt
          && ((JIdentityStmt<?>) stmt).getRightOp() instanceof JCaughtExceptionRef) {
        handlerStmts.add(stmt);
      } else {
        final List<Stmt> predecessors = graph.predecessors(stmt);
        queue.addAll(predecessors);
      }
    }
    return handlerStmts;
  }
}
