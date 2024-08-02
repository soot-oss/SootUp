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

import javax.annotation.Nonnull;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.visitor.AbstractExprVisitor;

/** Visitor for expressions in the AST. */
class AstExprVisitor extends AbstractExprVisitor<Void> {
  private final PropertyGraph.Builder graphBuilder;
  private final PropertyGraphNode parentNode;

  /**
   * Constructs an AST expression visitor.
   *
   * @param graphBuilder the property graph builder
   * @param parentNode the parent node
   */
  AstExprVisitor(PropertyGraph.Builder graphBuilder, PropertyGraphNode parentNode) {
    this.graphBuilder = graphBuilder;
    this.parentNode = parentNode;
  }

  @Override
  public void defaultCaseExpr(@Nonnull Expr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
  }

  // Handle binary operations
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

  // Handle invocation expressions
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

  // Handle casting expressions
  @Override
  public void caseCastExpr(@Nonnull JCastExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  // Handle instanceof expressions
  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  // Handle new array expressions
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

  // Handle length expressions
  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    handleUnopExpr(expr);
  }

  // Handle negation expressions
  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    handleUnopExpr(expr);
  }

  // Handle phi expressions
  @Override
  public void casePhiExpr(@Nonnull JPhiExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
    for (Immediate arg : expr.getArgs()) {
      ImmediateGraphNode argAstEdge = new ImmediateGraphNode(arg);
      graphBuilder.addEdge(new ArgAstEdge(exprNode, argAstEdge));
    }
  }

  // Helper methods to handle different types of expressions
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
    // Handle base for specific types of invoke expressions
    if (expr instanceof JInterfaceInvokeExpr) {
      JInterfaceInvokeExpr interfaceInvokeExpr = (JInterfaceInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(interfaceInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(parentNode, baseNode));
    } else if (expr instanceof JSpecialInvokeExpr) {
      JSpecialInvokeExpr specialInvokeExpr = (JSpecialInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(specialInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(parentNode, baseNode));
    } else if (expr instanceof JVirtualInvokeExpr) {
      JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) expr;
      ImmediateGraphNode baseNode = new ImmediateGraphNode(virtualInvokeExpr.getBase());

      graphBuilder.addEdge(new BaseAstEdge(parentNode, baseNode));
    }

    // Handle arguments
    for (Immediate arg : expr.getArgs()) {
      ImmediateGraphNode argAstEdge = new ImmediateGraphNode(arg);
      graphBuilder.addEdge(new ArgAstEdge(parentNode, argAstEdge));
    }
  }
}
