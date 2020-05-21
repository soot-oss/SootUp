package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;

/**
 * Contains an expression used as a conditional.
 *
 * <p>Prefer to use the factory methods in {@link de.upb.swt.soot.core.jimple.Jimple}.
 */
public class ConditionExprBox extends ValueBox {

  public ConditionExprBox(Value value) {
    super(value);
  }

  @Override
  public boolean canContainValue(Value value) {
    return value instanceof AbstractConditionExpr;
  }
}
