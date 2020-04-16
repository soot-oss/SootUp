package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;

import javax.annotation.Nonnull;

public abstract class AbstractConditionExpr extends AbstractIntBinopExpr {

  AbstractConditionExpr(@Nonnull Immediate op1, @Nonnull Immediate op2) {
    super(op1, op2);
  }
}
