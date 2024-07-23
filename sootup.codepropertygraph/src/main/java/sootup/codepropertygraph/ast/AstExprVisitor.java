package sootup.codepropertygraph.ast;

import javax.annotation.Nonnull;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.visitor.AbstractExprVisitor;

class AstExprVisitor extends AbstractExprVisitor<Void> {
  private final PropertyGraph.Builder graphBuilder;
  private final PropertyGraphNode parentNode;

  AstExprVisitor(PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode) {
    this.graphBuilder = graphBuilder;
    this.parentNode = parentNode;
  }

  @Override
  public void defaultCaseExpr(@Nonnull Expr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
  }

  @Override
  public void caseAddExpr(@Nonnull JAddExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseAndExpr(@Nonnull JAndExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseDivExpr(@Nonnull JDivExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseEqExpr(@Nonnull JEqExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseNeExpr(@Nonnull JNeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseGeExpr(@Nonnull JGeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseGtExpr(@Nonnull JGtExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseLeExpr(@Nonnull JLeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseLtExpr(@Nonnull JLtExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseMulExpr(@Nonnull JMulExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseOrExpr(@Nonnull JOrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseRemExpr(@Nonnull JRemExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseShlExpr(@Nonnull JShlExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseShrExpr(@Nonnull JShrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseSubExpr(@Nonnull JSubExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseXorExpr(@Nonnull JXorExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseCastExpr(@Nonnull JCastExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode arraySizeNode = new ImmediateGraphNode(expr.getSize());
    graphBuilder.addEdge(new ArraySizeAstEdge(exprNode, arraySizeNode));
  }

  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
    for (Immediate size : expr.getSizes()) {
      ImmediateGraphNode arraySizeNode = new ImmediateGraphNode(size);
      graphBuilder.addEdge(new ArraySizeAstEdge(exprNode, arraySizeNode));
    }
  }

  @Override
  public void caseNewExpr(@Nonnull JNewExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
  }

  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    handleUnopExpr(expr);
  }

  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    handleUnopExpr(expr);
  }

  @Override
  public void casePhiExpr(@Nonnull JPhiExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
    for (Immediate arg : expr.getArgs()) {
      ImmediateGraphNode argAstEdge = new ImmediateGraphNode(arg);
      graphBuilder.addEdge(new ArgAstEdge(exprNode, argAstEdge));
    }
  }

  private void handleBinopExpr(AbstractBinopExpr expr) {
    ImmediateGraphNode op1Node = new ImmediateGraphNode(expr.getOp1());
    graphBuilder.addEdge(new Op1AstEdge(parentNode, op1Node));

    ImmediateGraphNode op2Node = new ImmediateGraphNode(expr.getOp2());
    graphBuilder.addEdge(new Op2AstEdge(parentNode, op2Node));
  }

  private void handleUnopExpr(AbstractUnopExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  private void handleInvokeExpr(AbstractInvokeExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new InvokeAstEdge(parentNode, exprNode));

    // Handle base for specific types of invoke expressions
    if (expr instanceof JInterfaceInvokeExpr) {
      JInterfaceInvokeExpr interfaceInvokeExpr = (JInterfaceInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(interfaceInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(exprNode, baseNode));
    } else if (expr instanceof JSpecialInvokeExpr) {
      JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(specialInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(exprNode, baseNode));
    } else if (expr instanceof JVirtualInvokeExpr) {
      JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(virtualInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(exprNode, baseNode));
    }

    // Handle arguments
    for (Immediate arg : expr.getArgs()) {
      ImmediateGraphNode argAstEdge = new ImmediateGraphNode(arg);
      graphBuilder.addEdge(new ArgAstEdge(exprNode, argAstEdge));
    }
  }
}
