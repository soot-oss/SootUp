package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
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

  public Body interceptBody(@Nonnull Body originalBody) {

    ImmutableStmtGraph oriGraph = originalBody.getStmtGraph();
    BodyBuilder bodyBuilder = Body.builder(originalBody);
    Body newBody = bodyBuilder.build();
    // debug
    for (Stmt node : newBody.getStmtGraph().nodes()) {
      // System.out.println(node);
      // System.out.println("\tDef: " + node.getDefs());
      // System.out.println("\tUse: " + node.getUses());
    }

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

    Set<Local> newLocals = new HashSet<>(newBody.getLocals());

    int localIndex = 1;
    Deque<Stmt> visitedQueue = new ArrayDeque<>();
    visitedQueue.add(newBody.getStmtGraph().getStartingStmt());
    for (Trap trap : newBody.getTraps()) {
      if (!visitedQueue.contains(trap.getHandlerStmt())) {
        visitedQueue.add(trap.getHandlerStmt());
      }
    }

    // store the visited modified stmts in graph, avoid visiting a stmt twice
    Set<Stmt> visited = new HashSet<>();

    while (!visitedQueue.isEmpty()) {

      Stmt visitedStmt = visitedQueue.remove();

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

          // build the forwardsQueue
          Deque<Stmt> forwardsQueue = new ArrayDeque<>();
          forwardsQueue.addAll(bodyBuilder.getStmtGraph().successors(newVisitedStmt));
          Set<Stmt> visitedInner = new HashSet<>();
          visitedStmt = newVisitedStmt;

          while (!forwardsQueue.isEmpty()) {
            Stmt head = forwardsQueue.remove();
            visitedInner.add(head);

            // 1.case: if uselist of head contains oriLocal, then modify this oriLocal to newLocal
            if (head.getUses().contains(oriLocal)) {

              Stmt newHead = withNewUse(head, oriLocal, newLocal);

              bodyBuilder.mergeStmt(head, newHead);
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
                    if (isBiggerName((Local) backStmt.getDefs().get(0), modifiedLocal)) {
                      Stmt newBackStmt = withNewDef(backStmt, modifiedLocal);
                      bodyBuilder.mergeStmt(backStmt, newBackStmt);
                      visitedStmt = newBackStmt;
                      newLocals.remove(newLocal);
                    }
                  }
                  // 2.2 case: if backstmt's uselist contains the modified oriLocal
                  else if (hasModifiedUse(backStmt, oriLocal)) {
                    Local modifiedUse = getModifiedUse(backStmt, oriLocal);
                    if (isBiggerName(modifiedUse, modifiedLocal)) {
                      Stmt newBackStmt = withNewUse(backStmt, modifiedUse, modifiedLocal);
                      bodyBuilder.mergeStmt(backStmt, newBackStmt);
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
            // 3.case:
            else {
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
      }
      visited.add(visitedStmt);
      for (Stmt stmt : bodyBuilder.getStmtGraph().successors(visitedStmt)) {
        if (!visited.contains(stmt) && !visitedQueue.contains(stmt)) {
          visitedQueue.add(stmt);
        }
      }
    }

    bodyBuilder.setLocals(newLocals);
    newBody = bodyBuilder.build();

    return newBody;
  }

  // ******************assist_functions*************************

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
  @Nonnull
  protected boolean hasModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    boolean isModified = false;
    if (!stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          String name = ((Local) use).getName();
          if (name.contains("#")) {
            int i = name.indexOf('#');
            if (name.substring(0, i).equals(oriLocal.getName())) {
              isModified = true;
              break;
            }
          }
        }
      }
    }
    return isModified;
  }

  /**
   * Check whether stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt
   * @param oriLocal: a local
   * @return if so, return this modified local, else return null
   */
  protected Local getModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    Local modifiedLocal = null;
    if (hasModifiedUse(stmt, oriLocal)) {
      if (!stmt.getUses().isEmpty()) {
        for (Value use : stmt.getUses()) {
          if (use instanceof Local) {
            String name = ((Local) use).getName();
            if (name.contains("#")) {
              int i = name.indexOf('#');
              if (name.substring(0, i).equals(oriLocal.getName())) {
                modifiedLocal = (Local) use;
                break;
              }
            }
          }
        }
      }
    }
    return modifiedLocal;
  }

  /**
   * Check whether a Stmt's def is the modified oriLocal
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  @Nonnull
  protected boolean hasModifiedDef(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    boolean isModified = false;
    if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
      String name = ((Local) stmt.getDefs().get(0)).getName();
      if (name.contains("#")) {
        int i = name.indexOf('#');
        if (name.substring(0, i).equals(oriLocal.getName())) {
          isModified = true;
        }
      }
    }
    return isModified;
  }

  /**
   * Check whether leftLocal's name has bigger index than rightLocal's.
   *
   * @param leftLocal: a local in form oriLocal#num1
   * @param rigthLocal: a local in form oriLocal#num2
   * @return if so return true, else return false
   */
  @Nonnull
  protected boolean isBiggerName(@Nonnull Local leftLocal, @Nonnull Local rigthLocal) {
    boolean isBigger = false;
    String leftName = leftLocal.getName();
    String rightName = rigthLocal.getName();
    int i = leftName.indexOf('#');
    int j = rightName.indexOf('#');
    int leftNum = Integer.parseInt(leftName.substring(i + 1));
    int rightNum = Integer.parseInt(rightName.substring(j + 1));
    if (leftNum > rightNum) {
      isBigger = true;
    }
    return isBigger;
  }
}
