package sootup.core.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Linghui Luo and others
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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.*;
import sootup.core.types.ClassType;

/**
 * {@link SootClassSource} represents a Compilation Unit (Interpretation Unit for interpreted
 * languages). e.g. its connecting a file with source(code) to a {@link
 * sootup.core.signatures.Signature} that a {@link sootup.core.views.View} can resolve. Basic class
 * for retrieving information that is needed to build a {@link SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface SootClassSource {

  /**
   * @param sourceType instantiates the Subclass of SootClassSource to create a *SootClass
   * @return a *SootClass
   */
  SootClass buildClass(@NonNull SourceType sourceType);

  ClassType getClassType();

  AnalysisInputLocation getAnalysisInputLocation();

  Path getSourcePath();

  /** Reads from the source to retrieve its methods. This may be an expensive operation. */
  @NonNull Collection<? extends SootMethod> resolveMethods() throws ResolveException;

  /** Reads from the source to retrieve its fields. This may be an expensive operation. */
  @NonNull Collection<? extends SootField> resolveFields() throws ResolveException;

  /** Reads from the source to retrieve its modifiers. This may be an expensive operation. */
  @NonNull Set<ClassModifier> resolveModifiers();

  /**
   * Reads from the source to retrieve its directly implemented interfaces. This may be an expensive
   * operation.
   */
  @NonNull Set<? extends ClassType> resolveInterfaces();

  /**
   * Reads from the source to retrieve its superclass, if present. This may be an expensive
   * operation.
   */
  @NonNull Optional<? extends ClassType> resolveSuperclass();

  /**
   * Reads from the source to retrieve its outer class, if this is an inner class. This may be an
   * expensive operation.
   *
   * @return
   */
  @NonNull Optional<? extends ClassType> resolveOuterClass();

  /**
   * Reads from the source to retrieve its position in the source code. This may be an expensive
   * operation.
   */
  @NonNull Position resolvePosition();
}
