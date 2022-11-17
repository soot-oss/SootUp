package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Jan Martin Persch, Christian Brüggemann, Andreas Dann
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
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents a signature for a <code>null</code>-reference. */
public class NullType extends ReferenceType {

  @Nonnull private static final NullType INSTANCE = new NullType();

  @Nonnull
  public static NullType getInstance() {
    return INSTANCE;
  }

  private NullType() {}

  @Override
  @Nonnull
  public String toString() {
    return "null";
  }

  @Override
  public void accept(@Nonnull TypeVisitor v) {
    v.caseNullType();
  }
}
