package sootup.interceptors.typeresolving.types;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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
import sootup.core.types.Type;

/**
 * The top type is a superclass of all other types. This is similar to {@code java.lang.Object} but
 * also includes primitive types. <br>
 * This type can't exist in Java source code, but it can implicitly exist in bytecode. This happens
 * when the compiler re-uses local variables with the same id, but different types.<br>
 * If you see this type when you didn't expect it, you probably need to <b>turn on the {@link
 * sootup.interceptors.LocalSplitter}</b>. The {@link sootup.interceptors.LocalSplitter} will remove
 * all situations where a {@code TopType} could be created by the {@link
 * sootup.interceptors.TypeAssigner} (at least when the bytecode has been generated from Java source
 * code).
 */
public class TopType extends Type {
  @Nonnull private static final TopType INSTANCE = new TopType();

  @Nonnull
  public static TopType getInstance() {
    return INSTANCE;
  }

  private TopType() {}

  @Override
  public <V extends TypeVisitor> V accept(@Nonnull V typeVisitor) {
    /*typeVisitor.defaultCaseType();
    return typeVisitor;
    */
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "TopType";
  }
}
