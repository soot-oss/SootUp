package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
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
 * A BodyInterceptor that attempts to identify and separate uses of a local variable that are
 * independent of each other.
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

    Set<Local> newLocals = new HashSet<>(originalBody.getLocals());
    int localIndex = 1;
    Map<Stmt, Stmt> insertPositions = findTrapPositions(bodyBuilder);

    Iterator<Stmt> graphIterator = bodyBuilder.getStmtGraph().iterator();
    List<Stmt> visitList = new ArrayList<>();
    while (graphIterator.hasNext()) {
      visitList.add(graphIterator.next());
    }

    while (!visitList.isEmpty()) {

      Stmt visitedStmt = visitList.get(0);
      visitList.remove(0);

      if ((!visitedStmt.getDefs().isEmpty()) && visitedStmt.getDefs().get(0) instanceof Local) {
        Local oriLocal = (Local) visitedStmt.getDefs().get(0);
        // If the local as def in the set toSplitLocals
        if (toSplitLocals.contains(oriLocal)) {
          Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + localIndex);
          newLocals.add(newLocal);
          localIndex++;

          // replace the oriLocal with newLocal
          Stmt newVisitedStmt = withNewDef(visitedStmt, newLocal);

          // replace visitedStmt with newVisitedStmt
          bodyBuilder.mergeStmt(visitedStmt, newVisitedStmt);
          adaptTraps(bodyBuilder, visitedStmt, newVisitedStmt);
          adaptVisitList(visitList, visitedStmt, newVisitedStmt);

          // build the forwardsQueue
          Deque<Stmt> forwardsQueue = new ArrayDeque<>();
          forwardsQueue.addAll(bodyBuilder.getStmtGraph().successors(newVisitedStmt));
          Set<Stmt> visitedInner = new HashSet<>();

          while (!forwardsQueue.isEmpty()) {
            Stmt head = forwardsQueue.remove();
            visitedInner.add(head);

            // 1.case: if uselist of head contains oriLocal, then modify this oriLocal to newLocal
            if (head.getUses().contains(oriLocal)) {
              Stmt newHead = withNewUse(head, oriLocal, newLocal);
              bodyBuilder.mergeStmt(head, newHead);
              adaptTraps(bodyBuilder, head, newHead);
              adaptVisitList(visitList, head, newHead);
              // if deflist of modified stmt contains no orilocal, then trace forwards on.
              if ((!newHead.getDefs().isEmpty() && !newHead.getDefs().get(0).equivTo(oriLocal))
                  || newHead.getDefs().isEmpty()) {
                for (Stmt succ : bodyBuilder.getStmtGraph().successors(newHead)) {
                  if (!visitedInner.contains(succ) && !forwardsQueue.contains(succ)) {
                    forwardsQueue.addLast(succ);
                  }
                }
              }
            }

            // 2.case: if uselist of head contains the modified orilocal
            else if (hasModifiedUse(head, oriLocal)) {

              Local modifiedLocal = getModifiedUse(head, oriLocal);
              // if modifed name is not same as the newLocal's name then, trace backwards
              if (!modifiedLocal.getName().equals(newLocal.getName())) {
                localIndex--;
                Deque<Stmt> backwardsQueue = new ArrayDeque<>();
                backwardsQueue.addAll(bodyBuilder.getStmtGraph().predecessors(head));
                while (!backwardsQueue.isEmpty()) {
                  Stmt backStmt = backwardsQueue.remove();

                  // 2.1 case: if backstmt's def is modified oriLocal
                  if (hasModifiedDef(backStmt, oriLocal)) {
                    if (hasLeftLocalHigherName((Local) backStmt.getDefs().get(0), modifiedLocal)) {
                      Stmt newBackStmt = withNewDef(backStmt, modifiedLocal);
                      bodyBuilder.mergeStmt(backStmt, newBackStmt);
                      adaptTraps(bodyBuilder, backStmt, newBackStmt);
                      adaptVisitList(visitList, backStmt, newBackStmt);
                      newLocals.remove(newLocal);
                    }
                  }
                  // 2.2 case: if backstmt's uselist contains the modified oriLocal
                  else if (hasModifiedUse(backStmt, oriLocal)) {
                    Local modifiedUse = getModifiedUse(backStmt, oriLocal);
                    if (hasLeftLocalHigherName(modifiedUse, modifiedLocal)) {
                      Stmt newBackStmt = withNewUse(backStmt, modifiedUse, modifiedLocal);
                      bodyBuilder.mergeStmt(backStmt, newBackStmt);
                      adaptTraps(bodyBuilder, backStmt, newBackStmt);
                      adaptVisitList(visitList, backStmt, newBackStmt);
                      backwardsQueue.addAll(bodyBuilder.getStmtGraph().predecessors(newBackStmt));
                    }
                  }
                  // 2.3 case: else, trace backwards on
                  else {
                    backwardsQueue.addAll(bodyBuilder.getStmtGraph().predecessors(backStmt));
                  }
                }
              }
            }
            // 3.case: if uselist of head contains neither orilocal nor the modified orilocal, do
            // nothing
            else {
              // if deflist of head contains no orilocal, then trace forwards on.
              if ((!head.getDefs().isEmpty() && !head.getDefs().get(0).equivTo(oriLocal)
                  || head.getDefs().isEmpty())) {
                for (Stmt succ : bodyBuilder.getStmtGraph().successors(head)) {
                  if (!visitedInner.contains(succ) && !forwardsQueue.contains(succ)) {
                    forwardsQueue.addLast(succ);
                  }
                }
              }
            }
          }
        }
        // if the uselist of stmt contains a orilocal without pre-definition. (This case can just
        // occur in trap stmtblock)
        // then find the proper definition in main-stmt-graph
      } else {
        for (Local oriL : toSplitLocals) {
          if (visitedStmt.getUses().contains(oriL)) {
            Local lastChange = null;
            Deque<Stmt> queue = new ArrayDeque<>();
            queue.add(visitedStmt);
            Set<Stmt> visited = new HashSet<>();
            List<Stmt> checkPoints = new ArrayList<>();
            while (!queue.isEmpty()) {
              Stmt stmt = queue.removeFirst();
              visited.add(stmt);
              if (insertPositions.keySet().contains(stmt)) {
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
            bodyBuilder.mergeStmt(visitedStmt, newVisitedStmt);
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
   * fit the modified stmt in trap
   *
   * @param builder a bodybuilder, use it to modify trap
   * @param oldStmt a Stmt which maybe a beginStmt or endStmt
   * @param newStmt a modified stmt
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
   * fit the modified stmt in visitedList
   *
   * @param visitList a list storing all stmts whichh are not yet visited
   * @param oldStmt a stmt which is modified
   * @param newStmt a modified stmt
   */
  protected void adaptVisitList(
      @Nonnull List<Stmt> visitList, @Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    final int index = visitList.indexOf(oldStmt);
    if (index > -1) {
      visitList.set(index, newStmt);
    }
  }

  /**
   * Use newDef to replace the def in oldStmt.
   *
   * @param oldStmt a Stmt whose def is to be replaced.
   * @param newDef a Local is to replace def of oldStmt
   * @return a new Stmt with newDef
   */
  @Nonnull
  protected Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if (oldStmt instanceof JAssignStmt) {
      return Jimple.newAssignStmt(
          newDef, ((JAssignStmt) oldStmt).getRightOp(), oldStmt.getPositionInfo());
    } else if (oldStmt instanceof JIdentityStmt) {
      return ((JIdentityStmt) oldStmt).withLocal(newDef);
    } else {
      throw new RuntimeException("Just JAssignStmt and JIdentityStmt allowed");
    }
  }

  /**
   * Use newUse to replace the oldUse in oldStmt
   *
   * @param oldStmt a Stmt that has oldUse
   * @param oldUse a Local in the useList of oldStmt
   * @param newUse a Local is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Nonnull
  protected Stmt withNewUse(@Nonnull Stmt oldStmt, @Nonnull Local oldUse, @Nonnull Local newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return visitor.getNewStmt();
  }

  /**
   * Check whether a Stmt's useList contains the given modified oriLocal
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
   * Check whether stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt
   * @param oriLocal: a local
   * @return if so, return this modified local, else return null
   */
  @Nullable
  protected Local getModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    if (hasModifiedUse(stmt, oriLocal)) {
      if (!stmt.getUses().isEmpty()) {
        for (Value use : stmt.getUses()) {
          if (isLocalFromSameOrigin(oriLocal, use)) {
            return (Local) use;
          }
        }
      }
    }
    return null;
  }

  private boolean isLocalFromSameOrigin(@Nonnull Local oriLocal, Value use) {
    if (use instanceof Local) {
      final String name = ((Local) use).getName();
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
   * Check whether leftLocal's name has bigger index than rightLocal's.
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
   * find all insert positions of trapblocks
   *
   * @param bodyBuilder:
   * @return a HashMap with key: a stmt after that a trapblock should be inserted value: a stmt list
   *     which stores the handlerStmts
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

      // build templist, remove all stmts which are not trapstmt
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

      // build the map trapBlocks, key: trap's handlerStmt, value: all the stmts which are in the
      // corresponding trapblock
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

      // delete all stmts in trapblocks, except for handlerStmt and the insertposition
      for (Stmt handlerStmt : trapBlocks.keySet()) {
        List<Stmt> trapStmts = new ArrayList<>(trapBlocks.get(handlerStmt));
        trapStmts.remove(0);
        int index = visitList.indexOf(handlerStmt) + 1;
        Stmt stmt = visitList.get(index);
        Stmt nextStmt = visitList.get(index + 1);
        Set<Stmt> trapHandlerStmts = trapBlocks.keySet();
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

      // build a map with key: handlerStmt of trap, value: insert-position of trap
      int i = 0;
      Set<Stmt> trapHandlerStmts = trapBlocks.keySet();
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
