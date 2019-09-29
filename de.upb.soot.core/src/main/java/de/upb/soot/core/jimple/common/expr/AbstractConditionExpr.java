package de.upb.soot.core.jimple.common.expr;

import de.upb.soot.core.jimple.basic.Value;

public abstract class AbstractConditionExpr extends AbstractIntBinopExpr {

  AbstractConditionExpr(Value op1, Value op2) {
    super(op1, op2);
  }
}
