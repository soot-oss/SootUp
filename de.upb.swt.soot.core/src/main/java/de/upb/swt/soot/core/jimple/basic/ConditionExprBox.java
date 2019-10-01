package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;

public class ConditionExprBox extends ValueBox {

  public ConditionExprBox(Value value) {
    super(value);
  }

  @Override
  public boolean canContainValue(Value value) {
    return value instanceof AbstractConditionExpr;
  }
}
