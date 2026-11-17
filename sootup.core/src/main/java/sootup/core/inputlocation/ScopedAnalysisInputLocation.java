package sootup.core.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2024 Markus Schmidt and others
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

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Base class for filtering ClassSources returned from the underlying AnalysisInputLocation. Either
 * override the filter function - e.g. in an anonymous class - or use {@link
 * #of(AnalysisInputLocation, Predicate)}.
 *
 * <p>Scoping is useful to analyze only parts of a large input, or to treat parts of one input
 * differently, e.g. to load selected packages of a runtime library as application classes:
 *
 * <pre>
 * Predicate&lt;ClassType&gt; included = type -&gt; type.getPackageName().getName().startsWith("java.util");
 * List&lt;AnalysisInputLocation&gt; locations = Arrays.asList(
 *     ScopedAnalysisInputLocation.of(new JavaClassPathAnalysisInputLocation(rtJar, SourceType.Application), included),
 *     ScopedAnalysisInputLocation.of(new JavaClassPathAnalysisInputLocation(rtJar, SourceType.Library), included.negate()));
 * </pre>
 */
public abstract class ScopedAnalysisInputLocation implements AnalysisInputLocation {

  /**
   * Creates an input location that only provides the classes of the given input location which
   * match the given filter.
   *
   * @param inputLocation the input location to filter
   * @param filter returns true for the class types to keep
   * @return the scoped input location
   */
  @NonNull
  public static ScopedAnalysisInputLocation of(
      @NonNull AnalysisInputLocation inputLocation, @NonNull Predicate<ClassType> filter) {
    return new ScopedAnalysisInputLocation(inputLocation) {
      @Override
      protected boolean filter(@NonNull ClassType type) {
        return filter.test(type);
      }
    };
  }

  @NonNull private final AnalysisInputLocation inputLocation;

  public ScopedAnalysisInputLocation(@NonNull AnalysisInputLocation inputLocation) {
    this.inputLocation = inputLocation;
  }

  /** Override this method. */
  protected abstract boolean filter(@NonNull ClassType type);

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    if (!filter(type)) {
      return Optional.empty();
    }
    return inputLocation.getClassSource(type, view);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return inputLocation.getClassSources(view).filter(type -> filter(type.getClassType()));
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return inputLocation.getSourceType();
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return inputLocation.getBodyInterceptors();
  }

  @Override
  public void close() throws Exception {
    inputLocation.close();
  }
}
