package de.upb.swt.soot.core.jimple.common.constant;

import javax.annotation.Nonnull;

/**
 * A Constant that supports {@link #and(LogicalConstant)}, {@link #or(LogicalConstant)} and {@link
 * #xor(LogicalConstant)}
 */
public interface LogicalConstant<L extends LogicalConstant<L>> extends Constant {
  @Nonnull
  L and(@Nonnull L c);

  @Nonnull
  L or(@Nonnull L c);

  @Nonnull
  L xor(@Nonnull L c);
}
