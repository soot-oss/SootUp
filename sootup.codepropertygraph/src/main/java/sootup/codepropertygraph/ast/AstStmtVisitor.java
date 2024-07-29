package sootup.codepropertygraph.ast;

import javax.annotation.Nonnull;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;

class AstStmtVisitor extends AbstractStmtVisitor<Void> {
  private final PropertyGraph.Builder graphBuilder;
  private final PropertyGraphNode parentNode;
  private final Body body;

  AstStmtVisitor(PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode, Body body) {
    this.graphBuilder = graphBuilder;
    this.parentNode = parentNode;
    this.body = body;
  }

  private void addExprNode(PropertyGraphNode parent, Expr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parent, exprNode));
    expr.accept(new AstExprVisitor(graphBuilder, exprNode));
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
    if (stmt.getLeftOp() instanceof Immediate) {
      graphBuilder.addEdge(
          new LeftOpAstEdge(stmtNode, new ImmediateGraphNode((Immediate) stmt.getLeftOp())));
    } else if (stmt.getLeftOp() instanceof Ref) {
      graphBuilder.addEdge(new LeftOpAstEdge(stmtNode, new RefGraphNode((Ref) stmt.getLeftOp())));
    } else {
      graphBuilder.addEdge(new LeftOpAstEdge(stmtNode, new ExprGraphNode((Expr) stmt.getLeftOp())));
    }
    if (stmt.getRightOp() instanceof Immediate) {
      graphBuilder.addEdge(
          new RightOpAstEdge(stmtNode, new ImmediateGraphNode((Immediate) stmt.getRightOp())));
    } else if (stmt.getRightOp() instanceof Ref) {
      graphBuilder.addEdge(new RightOpAstEdge(stmtNode, new RefGraphNode((Ref) stmt.getRightOp())));
    } else {
      Expr rightOp = (Expr) stmt.getRightOp();
      ExprGraphNode rightOpNode = new ExprGraphNode(rightOp);
      graphBuilder.addEdge(new RightOpAstEdge(stmtNode, rightOpNode));
      rightOp.accept(new AstExprVisitor(graphBuilder, rightOpNode));

    }
  }

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
    ExprGraphNode invokeExprNode = new ExprGraphNode(invokeExpr);
    graphBuilder.addEdge(new InvokeAstEdge(stmtNode, invokeExprNode));
    invokeExpr.accept(new AstExprVisitor(graphBuilder, invokeExprNode));
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    AbstractConditionExpr condition = stmt.getCondition();
    ExprGraphNode exprNode = new ExprGraphNode(condition);
    graphBuilder.addEdge(new ConditionAstEdge(stmtNode, exprNode));
    condition.accept(new AstExprVisitor(graphBuilder, exprNode));
  }

  @Override
  public void caseNopStmt(@Nonnull JNopStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseIdentityStmt(@Nonnull JIdentityStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode leftOpNode = new ImmediateGraphNode(stmt.getLeftOp());
    graphBuilder.addEdge(new LeftOpAstEdge(stmtNode, leftOpNode));

    RefGraphNode rightOpNode = new RefGraphNode(stmt.getRightOp());
    graphBuilder.addEdge(new RightOpAstEdge(stmtNode, rightOpNode));
  }

  @Override
  public void caseGotoStmt(@Nonnull JGotoStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(stmt.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(stmtNode, opNode));
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));

    ImmediateGraphNode switchKeyNode = new ImmediateGraphNode(stmt.getKey());
    graphBuilder.addEdge(new SwitchKeyAstEdge(stmtNode, switchKeyNode));

    stmt.getTargetStmts(body)
        .forEach(
            target ->
                graphBuilder.addEdge(new SwitchTargetAstEdge(stmtNode, new StmtGraphNode(target))));
  }

  @Override
  public void defaultCaseStmt(@Nonnull Stmt stmt) {
    StmtGraphNode stmtNode = new StmtGraphNode(stmt);
    graphBuilder.addEdge(new StmtAstEdge(parentNode, stmtNode));
  }
}
