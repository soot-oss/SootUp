package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import javax.annotation.Nonnull;

public interface NumericConstant<N extends NumericConstant<N>>
    extends Constant, ComparableConstant<N> {

  // PTC 1999/06/28
  @Nonnull
  N add(@Nonnull N c);

  @Nonnull
  N subtract(@Nonnull N c);

  @Nonnull
  N multiply(@Nonnull N c);

  @Nonnull
  N divide(@Nonnull N c);

  @Nonnull
  N remainder(@Nonnull N c);

  @Nonnull
  BooleanConstant lessThan(@Nonnull N c);

  @Nonnull
  BooleanConstant lessThanOrEqual(@Nonnull N c);

  @Nonnull
  BooleanConstant greaterThan(@Nonnull N c);

  @Nonnull
  BooleanConstant greaterThanOrEqual(@Nonnull N c);

  @Nonnull
  N negate();
}
