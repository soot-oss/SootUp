package sootup.core.signatures;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Jan Martin Persch, Christian Brüggemann and others
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
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * A marker subclass for {@code MethodSignature}. Used to identify the fully qualified signature of
 * a polymorphic call site within the framework. Allows downstream analyses to check {@code (sig
 * instanceof PolymorphicMethodSignature)}.
 */
public class PolymorphicMethodSignature extends MethodSignature {

  /** Pass-through constructor to instantiate a polymorphic method signature. */
  public PolymorphicMethodSignature(
      @NonNull ClassType declaringClass,
      @NonNull String methodName,
      @NonNull Iterable<Type> parameters,
      @NonNull Type fqReturnType) {
    super(declaringClass, new PolymorphicMethodSubSignature(methodName, parameters, fqReturnType));
  }

  /**
   * Constructs a polymorphic method signature using an already instantiated polymorphic
   * sub-signature.
   */
  public PolymorphicMethodSignature(
      @NonNull ClassType declaringClass, @NonNull PolymorphicMethodSubSignature polySubSignature) {
    super(declaringClass, polySubSignature);
  }
}
