package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.common.expr.AbstractConditionExpr;

public class ConditionExprBox extends AbstractValueBox {
  /** */
  private static final long serialVersionUID = -3936572207750497150L;

  public ConditionExprBox(Value value) {
    super(value);
  }

  @Override
  public boolean canContainValue(Value value) {
    return value instanceof AbstractConditionExpr;
  }
}
