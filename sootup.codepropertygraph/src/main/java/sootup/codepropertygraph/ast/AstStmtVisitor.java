package sootup.codepropertygraph.ast;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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
import org.jspecify.annotations.NonNull;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;

/** Visitor for statements in the AST. */
class AstStmtVisitor extends AbstractStmtVisitor {
  private final PropertyGraph.Builder graphBuilder;
  private final PropertyGraphNode parentNode;

  /**
   * Constructs an AST statement visitor.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   */
  AstStmtVisitor(PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode) {
    this.graphBuilder = graphBuilder;
    this.parentNode = parentNode;
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    PropertyGraphNode leftOpNode = createOperandNode(stmt.getLeftOp());
    graphBuilder.addEdge(new LeftOpAstEdge(stmtNode, leftOpNode));

    PropertyGraphNode rightOpNode = createOperandNode(stmt.getRightOp());
    graphBuilder.addEdge(new RightOpAstEdge(stmtNode, rightOpNode));
  }

  /**
   * Creates a property graph node for the given operand.
   *
   * @param operand the operand
   * @return the property graph node
   */
  private PropertyGraphNode createOperandNode(Value operand) {
    if (operand instanceof Immediate) {
      return new ImmediateGraphNode((Immediate) operand);
    } else if (operand instanceof Ref) {
      return new RefGraphNode((Ref) operand);
    } else if (operand instanceof Expr) {
      ExprGraphNode exprNode = new ExprGraphNode((Expr) operand);
      ((Expr) operand).accept(new AstExprVisitor(graphBuilder, exprNode));
      return exprNode;
    } else {
      throw new IllegalArgumentException("Unknown operand type: " + operand.getClass());
    }
  }

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    Optional<AbstractInvokeExpr> invokeStmtOpt = stmt.getInvokeExpr();

    if (invokeStmtOpt.isPresent()) {
      AbstractInvokeExpr invokeExpr = invokeStmtOpt.get();
      ExprGraphNode invokeExprNode = new ExprGraphNode(invokeExpr);
      graphBuilder.addEdge(new InvokeAstEdge(stmtNode, invokeExprNode));
      invokeExpr.accept(new AstExprVisitor(graphBuilder, invokeExprNode));
    }
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    AbstractConditionExpr condition = stmt.getCondition();
    ExprGraphNode exprNode = new ExprGraphNode(condition);
    graphBuilder.addEdge(new ConditionAstEdge(stmtNode, exprNode));
    condition.accept(new AstExprVisitor(graphBuilder, exprNode));
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode leftOpNode = new ImmediateGraphNode(stmt.getLeftOp());
    graphBuilder.addEdge(new LeftOpAstEdge(stmtNode, leftOpNode));

    RefGraphNode rightOpNode = new RefGraphNode(stmt.getRightOp());
    graphBuilder.addEdge(new RightOpAstEdge(stmtNode, rightOpNode));
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode switchKeyNode = new ImmediateGraphNode(stmt.getKey());
    graphBuilder.addEdge(new SwitchKeyAstEdge(stmtNode, switchKeyNode));
  }

  @Override
  public void defaultCaseStmt(@NonNull Stmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }
}
