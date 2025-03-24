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

import org.jspecify.annotations.NonNull;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.visitor.AbstractExprVisitor;

/** Visitor for expressions in the AST. */
class AstExprVisitor extends AbstractExprVisitor {
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
  public void defaultCaseExpr(@NonNull Expr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
  }

  // Handle binary operations
  @Override
  public void caseAddExpr(@NonNull JAddExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseAndExpr(@NonNull JAndExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmpExpr(@NonNull JCmpExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmpgExpr(@NonNull JCmpgExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseCmplExpr(@NonNull JCmplExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseDivExpr(@NonNull JDivExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseEqExpr(@NonNull JEqExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseNeExpr(@NonNull JNeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseGeExpr(@NonNull JGeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseGtExpr(@NonNull JGtExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseLeExpr(@NonNull JLeExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseLtExpr(@NonNull JLtExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseMulExpr(@NonNull JMulExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseOrExpr(@NonNull JOrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseRemExpr(@NonNull JRemExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseShlExpr(@NonNull JShlExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseShrExpr(@NonNull JShrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseUshrExpr(@NonNull JUshrExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseSubExpr(@NonNull JSubExpr expr) {
    handleBinopExpr(expr);
  }

  @Override
  public void caseXorExpr(@NonNull JXorExpr expr) {
    handleBinopExpr(expr);
  }

  // Handle invocation expressions
  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@NonNull JVirtualInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {
    handleInvokeExpr(expr);
  }

  // Handle casting expressions
  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  // Handle instanceof expressions
  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode opNode = new ImmediateGraphNode(expr.getOp());
    graphBuilder.addEdge(new SingleOpAstEdge(exprNode, opNode));
  }

  // Handle new array expressions
  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));

    ImmediateGraphNode arraySizeNode = new ImmediateGraphNode(expr.getSize());
    graphBuilder.addEdge(new ArraySizeAstEdge(exprNode, arraySizeNode));
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
    for (Immediate size : expr.getSizes()) {
      ImmediateGraphNode arraySizeNode = new ImmediateGraphNode(size);
      graphBuilder.addEdge(new ArraySizeAstEdge(exprNode, arraySizeNode));
    }
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    ExprGraphNode exprNode = new ExprGraphNode(expr);
    graphBuilder.addEdge(new ExprAstEdge(parentNode, exprNode));
  }

  // Handle length expressions
  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    handleUnopExpr(expr);
  }

  // Handle negation expressions
  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    handleUnopExpr(expr);
  }

  // Handle phi expressions
  @Override
  public void casePhiExpr(@NonNull JPhiExpr expr) {
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
