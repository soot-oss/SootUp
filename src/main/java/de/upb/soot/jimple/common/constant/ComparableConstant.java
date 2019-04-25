package de.upb.soot.jimple.common.constant;

import javax.annotation.Nonnull;

public interface ComparableConstant<C extends ComparableConstant<C>> extends Constant {
  @Nonnull
  BooleanConstant equalEqual(@Nonnull C c);

  @Nonnull
  BooleanConstant notEqual(@Nonnull C c);
}
