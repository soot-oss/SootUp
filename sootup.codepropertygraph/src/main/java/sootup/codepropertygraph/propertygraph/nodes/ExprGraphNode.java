package sootup.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.Expr;

public class ExprGraphNode extends PropertyGraphNode implements ValueGraphNode {
  private final Expr expr;

  public ExprGraphNode(Expr expr) {
    this.expr = expr;
  }

  public Expr getExpr() {
    return expr;
  }

  @Override
  public Value getValue() {
    return expr;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
