package sootup.core.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Manuel Benz, Christian Brüggemann, Linghui Luo
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
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.model.AbstractClass;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Public interface to an input location. <code>AnalysisInputLocation</code>s are sources for {@link
 * SootClass}es, e.g. Java Classpath, Android APK, JAR file, etc. The strategy to traverse
 * something.
 *
 * <p>{@link #getClassSource(ClassType, View)} and {@link #getClassSources(View)} should in most
 * cases simply call {@link #getClassSource(ClassType, View)} or {@link #getClassSources(View)}
 * respectively with the default {@link BodyInterceptor}s of the frontend.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface AnalysisInputLocation<T extends AbstractClass> {
  /**
   * Create or find a class source for a given type.
   *
   * @param type The type of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<? extends AbstractClassSource<T>> getClassSource(
      @Nonnull ClassType type, @Nonnull View<?> view);

  /**
   * Scan the input location and create ClassSources for every compilation / interpretation unit.
   *
   * @return The source entries.
   */
  @Nonnull
  Collection<? extends AbstractClassSource<T>> getClassSources(@Nonnull View<?> view);

  /**
   * If the AnalysisInputLocation is initialized with the SourceType then this method should return
   * that specific SourceType. This is the default implementation and it returns null when no source
   * type is specified.
   *
   * @return returns null as source type
   */
  @Nullable
  SourceType getSourceType();
}
