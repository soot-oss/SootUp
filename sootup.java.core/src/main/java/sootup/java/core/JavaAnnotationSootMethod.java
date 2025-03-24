package sootup.java.core;

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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.BodySource;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class JavaAnnotationSootMethod extends JavaSootMethod {

  public JavaAnnotationSootMethod(
      @NonNull BodySource source,
      @NonNull MethodSignature methodSignature,
      @NonNull Iterable<MethodModifier> modifiers,
      @NonNull Iterable<ClassType> thrownExceptions,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Position position) {
    super(source, methodSignature, modifiers, thrownExceptions, annotations, position);
  }

  /**
   * @return returns default value of annotation. May be null, if there is no default value
   */
  @Nullable
  public Object getDefaultValue() {
    return this.bodySource.resolveAnnotationsDefaultValue();
  }
}
