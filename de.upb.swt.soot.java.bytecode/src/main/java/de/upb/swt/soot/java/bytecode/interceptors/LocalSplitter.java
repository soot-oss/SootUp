package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.AbstractStmtGraph;
import de.upb.swt.soot.core.graph.BriefStmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.model.Body;
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
 *     i0 = 0
 *     i0 = 1
 *     i1 = i0 + 1
 *     i0 = 5
 * </pre>
 *
 * <pre>
 *    i0 = 0
 *    i0#0 = 1
 *    i1 = i0#0 + 1
 *    i0#0 = 5
 * </pre>
 *
 * @author Zun Wang
 */
public class LocalSplitter implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    // collect all Locals that must be splitted
    // If a local as a definition apears two or more times as a definition, then this local must be
    // splitted
    // Store such locals into a set "toSplitLocals"
    Set<Local> visitedLocals = new HashSet<>();
    Set<Local> toSplitLocals = new HashSet<>();
    List<Stmt> stmts = originalBody.getStmts();
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

    // Copy all original Locals into a newLocals set
    Set<Local> locals = originalBody.getLocals();
    Set<Local> newLocals = new HashSet<Local>();
    newLocals.addAll(locals);

    // Copy all original Stmts into a newStmts list
    List<Stmt> newStmts = new ArrayList<>();
    newStmts.addAll(stmts);

    // establish a graph for the originalBody
    AbstractStmtGraph graph = new BriefStmtGraph(originalBody);

    int newLocalIndex = 0;
    for (Stmt stmt : stmts) {
      if ((!stmt.getDefs().isEmpty()) && stmt.getDefs().get(0) instanceof Local) {
        Local oriLocal = (Local) stmt.getDefs().get(0);
        // If the local as def in the set toSplitLocals
        if (toSplitLocals.contains(oriLocal)) {
          Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + newLocalIndex);
          newLocalIndex++;
          // replace the def in the stmt with newLocal
          newStmts.set(
              stmts.indexOf(stmt), withNewDef(newStmts.get(stmts.indexOf(stmt)), newLocal));
          newLocals.add(newLocal);

          Deque<Stmt> forwardsQueue = new ArrayDeque<>();
          if (!graph.getSuccsOf(stmt).isEmpty()) {
            forwardsQueue.addAll(graph.getSuccsOf(stmt));
          }
          Stmt head = null;
          while (!forwardsQueue.isEmpty()) {
            head = forwardsQueue.remove();
            Stmt headInNewList = newStmts.get(stmts.indexOf(head));
            // 1.case: oriLocal is defined again.
            if ((!head.getDefs().isEmpty())
                && head.getDefs().get(0) instanceof Local
                && head.getDefs().get(0).equivTo(oriLocal)) {

              // 1.1 case: headInNewList uses the oriLocal, then modified this use with newLocal
              if (headInNewList.getUses().contains(oriLocal)) {
                newStmts.set(stmts.indexOf(head), withNewUse(headInNewList, oriLocal, newLocal));
                // 1.2 case: headInNewList uses the modified oriLocal, and modified name isn't equal
                // to newLocal's name
              } else if (hasRenamedUse(headInNewList, oriLocal)) {
                Local renamedLocal = getRenamedUse(headInNewList, oriLocal);
                if (!renamedLocal.getName().equals(newLocal.getName())) {
                  Deque<Stmt> backwardQueue = new ArrayDeque<>();
                  backwardQueue.addAll(graph.getSuccsOf(head));
                  int numOfRL =
                      Integer.parseInt(
                          renamedLocal.getName().substring(renamedLocal.getName().length() - 1));
                  Local smallerLocal = null;
                  Local biggerLocal = null;
                  if (numOfRL < newLocalIndex) {
                    smallerLocal = renamedLocal;
                    biggerLocal = newLocal;
                  } else {
                    smallerLocal = newLocal;
                    biggerLocal = renamedLocal;
                  }
                  while (!backwardQueue.isEmpty()) {
                    Stmt backStmt = backwardQueue.remove();
                    Stmt backStmtInNewList = newStmts.get(stmts.indexOf(backStmt));
                    // if backStmtInNewList def is the bigger local, then modify it with smaller
                    // local
                    if (!backStmtInNewList.getDefs().isEmpty()
                        && backStmtInNewList.getDefs().get(0) instanceof Local
                        && backStmtInNewList.getDefs().get(0).equivTo(biggerLocal)) {
                      newStmts.set(
                          stmts.indexOf(backStmt), withNewDef(backStmtInNewList, smallerLocal));
                      // if backStmtInNewList def is the smaller local, continue(do nothing)
                      // Fixme: Is there another case?
                    } else if (!backStmtInNewList.getDefs().isEmpty()
                        && backStmtInNewList.getDefs().get(0) instanceof Local) {
                      if (backStmtInNewList.getDefs().get(0).equivTo(smallerLocal)
                          || backStmtInNewList.getDefs().get(0).equivTo(oriLocal)) continue;

                      // if don't find bigger Local or smaller local as def, but we find a stmt uses
                      // smaller local or oriLocal, then continue
                    } else if (backStmtInNewList.getUses().contains(smallerLocal)
                        || backStmtInNewList.getUses().contains(oriLocal)) {
                      continue;
                    } else {
                      if (!backStmtInNewList.getUses().contains(biggerLocal)) {
                        newStmts.set(
                            stmts.indexOf(backStmt),
                            withNewUse(backStmtInNewList, biggerLocal, smallerLocal));
                      } else {
                        if (!graph.getPredsOf(backStmt).isEmpty()) {
                          backwardQueue.addAll(graph.getPredsOf(backStmt));
                        }
                      }
                    }
                  }
                }
              }
              // 1.3 case: head doesn't use the oriLocal, do nothing

              // 2.case: oriLocal is not defined again yet.
            } else {
              // 2.1 case: headInNewList uses oriLocal directly, then modify it directly
              if (headInNewList.getUses().contains(oriLocal)) {
                newStmts.set(stmts.indexOf(head), withNewUse(headInNewList, oriLocal, newLocal));

                // 2.2 case: oriLocal is not in the usesList of headInNewList
              } else {
                // 2.2.1 case: headInNewList uses the renamed oriLocal, and renamedLocal'name is not
                // equal the newLocal'name
                if (hasRenamedUse(headInNewList, oriLocal)) {
                  Local renamedLocal = getRenamedUse(headInNewList, oriLocal);
                  if (!renamedLocal.getName().equals(newLocal.getName())) {
                    Deque<Stmt> backwardQueue = new ArrayDeque<>();
                    backwardQueue.addAll(graph.getSuccsOf(head));
                    int numOfRL =
                        Integer.parseInt(
                            renamedLocal.getName().substring(renamedLocal.getName().length() - 1));
                    Local smallerLocal = null;
                    Local biggerLocal = null;
                    if (numOfRL < newLocalIndex) {
                      smallerLocal = renamedLocal;
                      biggerLocal = newLocal;
                    } else {
                      smallerLocal = newLocal;
                      biggerLocal = renamedLocal;
                    }
                    while (!backwardQueue.isEmpty()) {
                      Stmt backStmt = backwardQueue.remove();
                      Stmt backStmtInNewList = newStmts.get(stmts.indexOf(backStmt));
                      // if backStmtInNewList def is the bigger local, then modify it with smaller
                      // local
                      if (!backStmtInNewList.getDefs().isEmpty()
                          && backStmtInNewList.getDefs().get(0) instanceof Local
                          && backStmtInNewList.getDefs().get(0).equivTo(biggerLocal)) {
                        newStmts.set(
                            stmts.indexOf(backStmt), withNewDef(backStmtInNewList, smallerLocal));
                        // if backStmtInNewList def is the smaller local, continue(do nothing)
                      } else if (!backStmtInNewList.getDefs().isEmpty()
                          && backStmtInNewList.getDefs().get(0) instanceof Local) {
                        if (backStmtInNewList.getDefs().get(0).equivTo(smallerLocal)
                            || backStmtInNewList.getDefs().get(0).equivTo(oriLocal)) continue;

                        // if don't find bigger Local or smaller local as def, but we find a stmt
                        // uses smaller local or oriLocal, then continue
                      } else if (backStmtInNewList.getUses().contains(smallerLocal)
                          || backStmtInNewList.getUses().contains(oriLocal)) {
                        continue;
                      } else {
                        if (!backStmtInNewList.getUses().contains(biggerLocal)) {
                          newStmts.set(
                              stmts.indexOf(backStmt),
                              withNewUse(backStmtInNewList, biggerLocal, smallerLocal));
                        } else {
                          if (!graph.getPredsOf(backStmt).isEmpty()) {
                            backwardQueue.addAll(graph.getPredsOf(backStmt));
                          }
                        }
                      }
                    }
                  }
                }
                // 2.2.2 case: oriLocal is not renamed in the useList of head, then do nothing
              }
              if (!graph.getSuccsOf(head).isEmpty()) {
                forwardsQueue.addAll(graph.getSuccsOf(head));
              }
            }
          }
        }
      }
    }

    Body newBody = originalBody.withLocals(newLocals);
    // newBody = newBody.withStmts(newStmts);
    return newBody;
  }

  @Nonnull
  protected Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if (oldStmt instanceof JAssignStmt) {
      return Jimple.newAssignStmt(
          newDef, ((JAssignStmt) oldStmt).getRightOp(), oldStmt.getPositionInfo());
    } else {
      throw new RuntimeException("Just JAssignStmt allowed");
    }
  }

  @Nonnull
  protected Stmt withNewUse(@Nonnull Stmt oldStmt, @Nonnull Local oldUse, @Nonnull Local newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return visitor.getNewStmt();
  }

  /**
   * Check whether stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt
   * @param oriLocal: a local
   * @return if so, return true, else return false
   */
  @Nonnull
  protected boolean hasRenamedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    boolean isRenamed = false;
    if (!stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          String name = ((Local) use).getName();
          if (name.substring(0, name.length() - 2).equals(oriLocal.getName())) {
            isRenamed = true;
          }
        }
      }
    }
    return isRenamed;
  }

  /**
   * Check whether stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt
   * @param oriLocal: a local
   * @return if so, return this modified local, else return null
   */
  protected Local getRenamedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    Local renamedLocal = null;
    if (!stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          String name = ((Local) use).getName();
          if (name.substring(0, name.length() - 2).equals(oriLocal.getName())) {
            renamedLocal = (Local) use;
            break;
          }
        }
      }
    }
    return renamedLocal;
  }
}
