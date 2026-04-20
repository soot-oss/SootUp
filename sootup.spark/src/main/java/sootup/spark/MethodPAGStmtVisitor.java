package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

@Slf4j
@Builder
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MethodPAGStmtVisitor extends AbstractStmtVisitor {

  MethodSignature methodSignature;
  PAG PAG;
  CallGraph callGraph;
  View view;
  NodeFactory nodeFactory;

  @Override
  public void caseAssignStmt(JAssignStmt stmt) {
    if (stmt.isInvokableStmt() && stmt.asInvokableStmt().getInvokeExpr().isPresent()) {
      handleInvokeExpr(stmt.asInvokableStmt().getInvokeExpr().get(), Optional.of(stmt.getLeftOp()));
    } else { // regular assignment
      val left = stmt.getLeftOp();
      val right = stmt.getRightOp();
      val leftNode = nodeFactory.createNode(left, methodSignature);
      val rightNode = nodeFactory.createNode(right, methodSignature);
      if (leftNode.isPresent() && rightNode.isPresent()) {
        val source = rightNode.get();
        val target = leftNode.get();
        PAG.addEdge(source, target);
      } else {
        log.warn("Missing nodes for left: {} <- right: {}", left, right);
      }
    }
  }

  /**
   * At each call site, assignment edges are added from the nodes representing the actual arguments
   * to the nodes representing the corresponding parameters of all methods that may be targets of
   * the call site, and an assignment edge is added from the return node of each of these methods to
   * the node for the variable that receives the return value (if any) at the call site.
   */
  @Override
  public void caseInvokeStmt(JInvokeStmt stmt) {
    // handleInvokeExpr(stmt);
  }

  /**
   * the builder inserts edges into the pointer assignment graph to represent pointer flow through
   * method parameters and return values, based on the active caU graph
   *
   * @param expr
   */
  private void handleInvokeExpr(AbstractInvokeExpr expr, Optional<Value> lhs) {
    val targets = callGraph.callTargetsFrom(methodSignature);
    targets.stream()
        .filter(
            targetMethodSig ->
                targetMethodSig
                    .getSubSignature()
                    .equals(expr.getMethodSignature().getSubSignature()))
        .forEach(
            targetMethodSig ->
                view.getMethod(targetMethodSig)
                    .ifPresent(
                        sootMethod -> {
                          // add edges for parameter mapping
                          for (int i = 0; i < expr.getArgCount(); i++) {
                            val argNode = nodeFactory.createNode(expr.getArg(i), methodSignature);
                            final int index = i;
                            val paramLocal =
                                sootMethod.getBody().getStmts().stream()
                                    .filter(stmt -> stmt instanceof JIdentityStmt)
                                    .map(stmt -> (JIdentityStmt) stmt)
                                    .filter(stmt -> stmt.getRightOp() instanceof JParameterRef)
                                    .filter(
                                        stmt ->
                                            ((JParameterRef) stmt.getRightOp()).getIndex() == index)
                                    .map(JIdentityStmt::getLeftOp)
                                    .findFirst();
                            if (argNode.isPresent() && paramLocal.isPresent()) {
                              val paramNode =
                                  nodeFactory.createNode(paramLocal.get(), targetMethodSig);
                              paramNode.ifPresent(node -> PAG.addEdge(argNode.get(), node));
                            }
                          }
                          // add edges for return value mapping
                          lhs.flatMap(l -> nodeFactory.createNode(l, methodSignature))
                              .ifPresent(
                                  lhsNode ->
                                      sootMethod.getBody().getStmts().stream()
                                          .filter(stmt -> stmt instanceof JReturnStmt)
                                          .map(stmt -> (JReturnStmt) stmt)
                                          .forEach(
                                              returnStmt ->
                                                  nodeFactory
                                                      .createNode(
                                                          returnStmt.getOp(), targetMethodSig)
                                                      .ifPresent(
                                                          retOpNode ->
                                                              PAG.addEdge(retOpNode, lhsNode))));
                        }));
  }
}
