package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.basic.Value;

public abstract class AbstractConditionExpr extends AbstractIntBinopExpr {

  AbstractConditionExpr(Value op1, Value op2) {
    super(op1, op2);
  }
}
