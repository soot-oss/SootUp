package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Christian Brüggemann, Jan Martin Persch
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents Java's 'void' type as methods return's type. */
public class VoidType extends Type {

  @NonNull private static final VoidType INSTANCE = new VoidType();

  @NonNull
  public static VoidType getInstance() {
    return INSTANCE;
  }

  private VoidType() {}

  @Override
  @NonNull
  public String toString() {
    return "void";
  }

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    v.caseVoidType();
    return v;
  }
}
