package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.common.expr.Expr;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class ExprGraphNode extends PropertyGraphNode {
  private final Expr expr;

  public ExprGraphNode(Expr expr) {
    this.expr = expr;
  }

  public Expr getExpr() {
    return expr;
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
