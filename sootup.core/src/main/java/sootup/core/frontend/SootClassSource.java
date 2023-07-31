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

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;

/**
 * Basic class for retrieving information that is needed to build a {@link SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public abstract class SootClassSource<S extends SootClass<? extends SootClassSource<S>>>
    extends AbstractClassSource<S> {

  /**
   * Creates and a {@link SootClassSource} for a specific source file. The file should be passed as
   * {@link Path} and can be located in an arbitrary {@link java.nio.file.FileSystem}.
   * Implementations should use {@link java.nio.file.Files#newInputStream(Path, OpenOption...)} to
   * access the file.
   *
   * @param srcNamespace The {@link AnalysisInputLocation} that holds the given file
   * @param sourcePath Path to the source file of the to-be-created {@link SootClassSource}. The
   *     given path has to exist and requires to be handled by this {@link ClassProvider}.
   *     Implementations might double check this if wanted.
   * @param classSignature the signature that has been used to resolve this class
   */
  public SootClassSource(
      @Nonnull AnalysisInputLocation<? extends SootClass<?>> srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  protected SootClassSource(SootClassSource<S> delegate) {
    super(delegate.classSource, delegate.getClassType(), delegate.getSourcePath());
  }

  /** Reads from the source to retrieve its methods. This may be an expensive operation. */
  @Nonnull
  public abstract Collection<? extends SootMethod> resolveMethods() throws ResolveException;

  /** Reads from the source to retrieve its fields. This may be an expensive operation. */
  @Nonnull
  public abstract Collection<? extends SootField> resolveFields() throws ResolveException;

  /** Reads from the source to retrieve its modifiers. This may be an expensive operation. */
  @Nonnull
  public abstract Set<ClassModifier> resolveModifiers();

  /**
   * Reads from the source to retrieve its directly implemented interfaces. This may be an expensive
   * operation.
   */
  @Nonnull
  public abstract Set<? extends ClassType> resolveInterfaces();

  /**
   * Reads from the source to retrieve its superclass, if present. This may be an expensive
   * operation.
   */
  @Nonnull
  public abstract Optional<? extends ClassType> resolveSuperclass();

  /**
   * Reads from the source to retrieve its outer class, if this is an inner class. This may be an
   * expensive operation.
   *
   * @return
   */
  @Nonnull
  public abstract Optional<? extends ClassType> resolveOuterClass();

  /**
   * Reads from the source to retrieve its position in the source code. This may be an expensive
   * operation.
   */
  @Nonnull
  public abstract Position resolvePosition();
}
