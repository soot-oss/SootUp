package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2020 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Body.BodyBuilder;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable (definition) that are
 * independent of each other.
 *
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

  @Nonnull
  public Body interceptBody(@Nonnull Body originalBody) {

    ImmutableStmtGraph oriGraph = originalBody.getStmtGraph();
    BodyBuilder bodyBuilder = Body.builder(originalBody);

    // Find all Locals that must be split
    // If a local as a definition appears two or more times, then this local must be split
    Set<Stmt> stmts = oriGraph.nodes();
    Set<Local> visitedLocals = new HashSet<>();
    Set<Local> toSplitLocals = new HashSet<>();
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

    //Create a new Local-Set for the modified new body.
    Set<Local> newLocals = new HashSet<>(originalBody.getLocals());
    int localIndex = 1;

    //Find out all positions where a trap is inserted, store them in a map
    //Key is handlerStmt of a trap
    //Value is a Stmt, behind it the corresponding trap is inserted.
    Map<Stmt, Stmt> insertPositions = findTrapPositions(bodyBuilder);

    //Use bodyBuilder's iterator to create visitList, in order to control the visit-order of Stmt in StmtGraph
    Iterator<Stmt> graphIterator = bodyBuilder.getStmtGraph().iterator();
    List<Stmt> visitList = new ArrayList<>();
    while (graphIterator.hasNext()) {
      visitList.add(graphIterator.next());
    }

    //Start to iterate the visitList:
    while (!visitList.isEmpty()) {
      //Remove the first(visited) Stmt in visitList, to avoid visiting a Stmt twice.
      Stmt visitedStmt = visitList.get(0);
      visitList.remove(0);

      //At first Check the definition(left side) of the visited Stmt
      if ((!visitedStmt.getDefs().isEmpty()) && visitedStmt.getDefs().get(0) instanceof Local) {
        Local oriLocal = (Local) visitedStmt.getDefs().get(0);
        // If definition of the visited Stmt is a local which must be split:
        if (toSplitLocals.contains(oriLocal)) {
          //then assign a new name to the oriLocal to get a new local which is called newLocal
          Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + localIndex);
          newLocals.add(newLocal);
          localIndex++;
          //create a new Stmt whose definition is replaced with the newLocal,
          Stmt newVisitedStmt = withNewDef(visitedStmt, newLocal);
          //replace the visited Stmt in the StmtGraph with the new Stmt,
          bodyBuilder.replaceStmt(visitedStmt, newVisitedStmt);
          //replace the corresponding Stmt in traps and visitList with the new Stmt.
          adaptTraps(bodyBuilder, visitedStmt, newVisitedStmt);
          adaptVisitList(visitList, visitedStmt, newVisitedStmt);

          //Build the forwardsQueue which is used to iterate all Stmts before the orilocal is defined again.
          //The direction of iteration is from root of the StmtGraph to the leaves. So the successors of the new Stmt are added into the forwardsQueue.
          Deque<Stmt> forwardsQueue =
              new ArrayDeque<>(bodyBuilder.getStmtGraph().successors(newVisitedStmt));
          //Create the visitedUsesStmt to store the visited Stmt for the forwardsQueue, to avoid, a Stmt is added twice into the forwardQueue.
          Set<Stmt> visitedUsesStmt = new HashSet<>();

          //Start to iterate the forwardsQueue:
          while (!forwardsQueue.isEmpty()) {
            //Remove the head from the forwardsQueue:
            Stmt head = forwardsQueue.remove();
            visitedUsesStmt.add(head);

            // 1.case: if useList of head contains oriLocal, then replace the oriLocal with newLocal.
            if (head.getUses().contains(oriLocal)) {
              Stmt newHead = withNewUse(head, oriLocal, newLocal);
              bodyBuilder.replaceStmt(head, newHead);
              adaptTraps(bodyBuilder, head, newHead);
              adaptVisitList(visitList, head, newHead);
              // if head doesn't define the the oriLocal again, then add all successors which are not in forwardsQueue and visitedUsesStmt, into the forwardsQueue.
              if ( newHead.getDefs().isEmpty() || !newHead.getDefs().get(0).equivTo(oriLocal)) {
                for (Stmt succ : bodyBuilder.getStmtGraph().successors(newHead)) {
                  if (!visitedUsesStmt.contains(succ) && !forwardsQueue.contains(succ)) {
                    forwardsQueue.addLast(succ);
                  }
                }
              }
            }

            // 2.case: if useList of head contains the modified orilocal, so a conflict maybe arise, then trace the StmtGraph backwards to resolve the conflict.
            else if (hasModifiedUse(head, oriLocal)) {

              Local modifiedLocal = getModifiedUse(head, oriLocal);
              // if modifed name is not same as the newLocal's name then -> conflict arises -> trace backwards
              if (!modifiedLocal.getName().equals(newLocal.getName())) {
                localIndex--;

                //Build the backwardsQueue which is used to iterate all Stmts between head and the Stmts which define the oriLocal in last time.
                //The direction of iteration is from leave of the StmtGraph to the root. So the predecessors of head are added into the BackwardsQueue.
                Deque<Stmt> backwardsQueue =
                    new ArrayDeque<>(bodyBuilder.getStmtGraph().predecessors(head));

                //Start to iterator the backwardQueue:
                while (!backwardsQueue.isEmpty()) {
                  //Remove the first Stmt of backwardQueue, and name it as backStmt.
                  Stmt backStmt = backwardsQueue.remove();

                  // 2.1 case: if backStmt's definition is the modified and has a higher local-name-index than the modifiedLocal of head
                  // then replace the definition of backStmt with the modifiedLocal of head, and remove the corresponding Local(definition of backStmt) from the set: newLocals
                  if (hasModifiedDef(backStmt, oriLocal)) {
                    if (hasLeftLocalHigherName((Local) backStmt.getDefs().get(0), modifiedLocal)) {
                      Stmt newBackStmt = withNewDef(backStmt, modifiedLocal);
                      bodyBuilder.replaceStmt(backStmt, newBackStmt);
                      adaptTraps(bodyBuilder, backStmt, newBackStmt);
                      adaptVisitList(visitList, backStmt, newBackStmt);
                      newLocals.remove(newLocal);
                    }
                  }
                  // 2.2 case: if backStmt's useList contains the modified oriLocal, and this modified oriLocal has a higher local-name-index that the modifiedLocal of head
                  // then replace the corresponding use of backStmt with modifiedLocal of head, and add all predecessors of the backStmt into the backwardsQueue.
                  else if (hasModifiedUse(backStmt, oriLocal)) {
                    Local modifiedUse = getModifiedUse(backStmt, oriLocal);
                    if (hasLeftLocalHigherName(modifiedUse, modifiedLocal)) {
                      Stmt newBackStmt = withNewUse(backStmt, modifiedUse, modifiedLocal);
                      bodyBuilder.replaceStmt(backStmt, newBackStmt);
                      adaptTraps(bodyBuilder, backStmt, newBackStmt);
                      adaptVisitList(visitList, backStmt, newBackStmt);
                      backwardsQueue.addAll(bodyBuilder.getStmtGraph().predecessors(newBackStmt));
                    }
                  }
                  // 2.3 case: if there's no relationship between backStmt's definition/useList and oriLocal, then add all predecessors of the backStmt into the backwardsQueue.
                  else {
                    backwardsQueue.addAll(bodyBuilder.getStmtGraph().predecessors(backStmt));
                  }
                }
              }
            }
            // 3.case: if uselist of head contains neither orilocal nor the modified orilocal,
            // then add all successors of head which are not in forwardsQueue and visitedUsesStmt, into the forwardsQueue.
            else {
              if (head.getDefs().isEmpty() || !head.getDefs().get(0).equivTo(oriLocal)) {
                for (Stmt succ : bodyBuilder.getStmtGraph().successors(head)) {
                  if (!visitedUsesStmt.contains(succ) && !forwardsQueue.contains(succ)) {
                    forwardsQueue.addLast(succ);
                  }
                }
              }
            }
          }
        }
      //Then check the useList of the visitedStmt:
      } else {
        // For each Local(oriL) which is to be split, check whether it is used in the visitedStmt without definition before.
        // We define a StmtGraph consists of a mainStmtGraph and none or more trapStmtGraphs.
        // This situation could arise just in a trapStmtGraph which is nested in mainStmtGraph or another trapStmtGraph (is called superTrapStmtGraph).
        for (Local oriL : toSplitLocals) {
          //If so, find the definition in mainStmtGraph or in another trapStmtGraph, replace the oriL in visitedStmt with the definition's Local.
          // We do it in 2 steps.
          if (visitedStmt.getUses().contains(oriL)) {

            //1.Step: find all checkpoints, store them in the List: checkPoints.
            //Each checkPoint is a stmt, after it the observed trapStmtGraph or a superTrapStmtGraph of the observed trapStmtGraph is inserted.
            //The ordering should be from the checkPoint in closest superTrapStmtGraph to the checkPoint in the mainStmtGraph.
            Local lastChange = null;
            Deque<Stmt> queue = new ArrayDeque<>();
            queue.add(visitedStmt);
            Set<Stmt> visited = new HashSet<>();
            List<Stmt> checkPoints = new ArrayList<>();
            while (!queue.isEmpty()) {
              Stmt stmt = queue.removeFirst();
              visited.add(stmt);
              if (insertPositions.containsKey(stmt)) {
                checkPoints.add(insertPositions.get(stmt));
                queue.clear();
                queue.add(insertPositions.get(stmt));
              } else {
                for (Stmt pred : bodyBuilder.getStmtGraph().predecessors(stmt)) {
                  if (!visited.contains(pred)) {
                    queue.add(pred);
                  }
                }
              }
            }
            //2.Step: Start looking for from the first checkPoint, before this checkPoint whether there is a modified oriL as definition in a Stmt.
            //If so, replace the oriL in visitedStmt with the modified oriL.
            //Else, looking for from the next checkPoint, until a such Stmt is found.
            visited.clear();
            boolean isFound = false;
            for (Stmt checkPoint : checkPoints) {
              queue.add(checkPoint);
              while (!queue.isEmpty()) {
                Stmt stmt = queue.removeFirst();
                visited.add(stmt);
                if (hasModifiedDef(stmt, oriL)) {
                  lastChange = (Local) stmt.getDefs().get(0);
                  isFound = true;
                  break;
                } else if (hasModifiedUse(stmt, oriL)) {
                  lastChange = getModifiedUse(stmt, oriL);
                  isFound = true;
                  break;
                } else {
                  for (Stmt pred : bodyBuilder.getStmtGraph().predecessors(stmt)) {
                    if (!visited.contains(pred)) {
                      queue.add(pred);
                    }
                  }
                }
              }
              if (isFound) {
                break;
              }
            }
            Stmt newVisitedStmt = withNewUse(visitedStmt, oriL, lastChange);
            bodyBuilder.replaceStmt(visitedStmt, newVisitedStmt);
            adaptTraps(bodyBuilder, visitedStmt, newVisitedStmt);
            adaptVisitList(visitList, visitedStmt, newVisitedStmt);
          }
        }
      }
    }

    bodyBuilder.setLocals(newLocals);
    return bodyBuilder.build();
  }

  // ******************assist_functions*************************

  /**
   * Fit the modified stmt in Traps
   *
   * @param builder a bodybuilder, use it to modify Trap
   * @param oldStmt a Stmt which maybe a beginStmt or endStmt in a Trap
   * @param newStmt a modified stmt to replace the oldStmt.
   */
  protected void adaptTraps(
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
  protected void adaptVisitList(
      @Nonnull List<Stmt> visitList, @Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    final int index = visitList.indexOf(oldStmt);
    if (index > -1) {
      visitList.set(index, newStmt);
    }
  }

  /**
   * Use newDef to replace the definition in oldStmt.
   *
   * @param oldStmt a Stmt whose def is to be replaced.
   * @param newDef a Local to replace definition Local of oldStmt.
   * @return a new Stmt with newDef
   */
  @Nonnull
  protected Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if (oldStmt instanceof JAssignStmt) {
      return ((JAssignStmt) oldStmt).withVariable(newDef);
    } else if (oldStmt instanceof JIdentityStmt) {
      return ((JIdentityStmt) oldStmt).withLocal(newDef);
    }
    throw new RuntimeException("Just JAssignStmt and JIdentityStmt allowed");
  }

  /**
   * Use newUse to replace the oldUse in oldStmt.
   *
   * @param oldStmt a Stmt that has oldUse.
   * @param oldUse a Local in the useList of oldStmt.
   * @param newUse a Local is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Nonnull
  protected Stmt withNewUse(@Nonnull Stmt oldStmt, @Nonnull Local oldUse, @Nonnull Local newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return Objects.requireNonNull(visitor.getNewStmt());
  }

  /**
   * Check whether a Stmt's useList contains the given modified oriLocal.
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  protected boolean hasModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
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
  protected Local getModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
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
  protected boolean hasModifiedDef(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
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
  protected boolean hasLeftLocalHigherName(@Nonnull Local leftLocal, @Nonnull Local rightLocal) {
    String leftName = leftLocal.getName();
    String rightName = rightLocal.getName();
    int i = leftName.lastIndexOf('#');
    int j = rightName.lastIndexOf('#');
    int leftNum = Integer.parseInt(leftName.substring(i + 1));
    int rightNum = Integer.parseInt(rightName.substring(j + 1));
    return leftNum > rightNum;
  }

  /**
   * Find all insert-positions of trapStmtGraphs
   *
   * @param bodyBuilder
   * @return a HashMap
   *         key: is a handlerStmt of a Trap
   *         value: is a Stmt, is so-called insert-position of a trapStmtGraph(or trapBlock), after it a trapStmtGraph(or trapBlock) is inserted.
   */
  @Nonnull
  protected Map<Stmt, Stmt> findTrapPositions(@Nonnull BodyBuilder bodyBuilder) {
    Map<Stmt, Stmt> insertPositions = new HashMap<>();
    StmtGraph graph = bodyBuilder.getStmtGraph();
    if (graph.getTraps().isEmpty()) {
      return insertPositions;
    } else {
      Iterator<Stmt> graphIterator = graph.iterator();
      List<Stmt> visitList = new ArrayList<>();
      while (graphIterator.hasNext()) {
        visitList.add(graphIterator.next());
      }

      //Remove all Stmts which are in trapStmtGraphs from the visitList.
      List<Stmt> tempList = new ArrayList<>(visitList);
      Deque<Stmt> deque = new ArrayDeque<>();
      deque.add(graph.getStartingStmt());
      while (!deque.isEmpty()) {
        Stmt head = deque.removeFirst();
        tempList.remove(head);
        for (Stmt succ : graph.successors(head)) {
          if (tempList.contains(succ)) {
            deque.add(succ);
          }
        }
      }

      // build the map trapBlocks:
      // key: is a Trap's handlerStmt,
      // value: is a Stmt-List which stores all the Stmts in the corresponding trapStmtGraph
      Map<Stmt, List<Stmt>> trapBlocks = new HashMap<>();
      for (Trap trap : graph.getTraps()) {
        Stmt trapSource = trap.getHandlerStmt();
        Deque<Stmt> queue = new ArrayDeque<>();
        queue.add(trapSource);
        List<Stmt> trapStmts = new ArrayList<>();
        while (!queue.isEmpty()) {
          Stmt stmt = queue.removeFirst();
          trapStmts.add(stmt);
          tempList.remove(stmt);
          List<Stmt> succs = graph.successors(stmt);
          for (Stmt succ : succs) {
            if (!trapStmts.contains(succ) && tempList.contains(succ)) {
              queue.addLast(succ);
            }
          }
        }
        trapBlocks.put(trap.getHandlerStmt(), trapStmts);
      }

      //For each Trap in trapBlocks, reemove all Stmts from the trapStmtGraph, except for the handlerStmt and the insert-position.
      Set<Stmt> trapHandlerStmts = trapBlocks.keySet();
      for (Stmt handlerStmt : trapHandlerStmts) {
        List<Stmt> trapStmts = new ArrayList<>(trapBlocks.get(handlerStmt));
        trapStmts.remove(0);
        int index = visitList.indexOf(handlerStmt) + 1;
        Stmt stmt = visitList.get(index);
        Stmt nextStmt = visitList.get(index + 1);
        while (!trapStmts.isEmpty()) {
          trapStmts.remove(stmt);
          if ((!trapHandlerStmts.contains(nextStmt) || trapStmts.isEmpty())
              && trapBlocks.get(handlerStmt).contains(stmt)) {
            visitList.remove(stmt);
          }
          stmt = nextStmt;
          index = visitList.indexOf(stmt);
          if (index != visitList.size() - 1) {
            nextStmt = visitList.get(index + 1);
          } else {
            nextStmt = null;
          }
        }
      }

      // Build a map with
      // key: handlerStmt of a Trap,
      // value: insert-position of the corresponding Trap
      int i = 0;
      while (i < visitList.size()) {
        Stmt stmt = visitList.get(i);
        if (trapHandlerStmts.contains(stmt)) {
          int index = visitList.indexOf(stmt);
          Stmt insertPosition = visitList.get(index - 1);
          visitList.remove(stmt);
          i--;
          insertPositions.put(stmt, insertPosition);
        }
        i++;
      }
      return insertPositions;
    }
  }
}
