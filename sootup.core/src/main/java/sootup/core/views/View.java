package sootup.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Ben Hermann, Christian Brüggemann and others
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.Project;
import sootup.core.Scope;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;

/**
 * A View is essentially a collection of code belonging to a {@link Project}.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface View<T extends SootClass> {

  Project getProject();

  @Nonnull
  List<BodyInterceptor> getBodyInterceptors(AnalysisInputLocation inputLocation);

  /** Return all classes in the view. */
  @Nonnull
  Collection<T> getClasses();

  /**
   * Return a class with given signature.
   *
   * @return A class with given signature.
   */
  @Nonnull
  Optional<T> getClass(@Nonnull ClassType signature);

  Optional<? extends SootField> getField(@Nonnull FieldSignature signature);

  Optional<? extends SootMethod> getMethod(@Nonnull MethodSignature signature);

  /**
   * Returns the scope if the view is scoped.
   *
   * @return The scope that led to the view
   */
  @Nonnull
  Optional<Scope> getScope();

  @Nonnull
  TypeHierarchy getTypeHierarchy();

  /** Returns the {@link IdentifierFactory} for this view. */
  @Nonnull
  IdentifierFactory getIdentifierFactory();

  @Nonnull
  default T getClassOrThrow(@Nonnull ClassType classType) {
    return getClass(classType)
        .orElseThrow(
            () -> new IllegalArgumentException("Could not find " + classType + " in View."));
  }
}
