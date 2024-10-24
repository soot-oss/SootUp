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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.frontend.SootClassSource;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Base class for filtering ClassSources returned from the underlying AnalysisInputLocation you need
 * to override the filter function - e.g. override it in an anonymous class
 */
abstract class ScopedAnalysisInputLocation implements AnalysisInputLocation {

  @Nonnull private final AnalysisInputLocation inputLocation;

  public ScopedAnalysisInputLocation(@Nonnull AnalysisInputLocation inputLocation) {
    this.inputLocation = inputLocation;
  }

  /** Override this method. */
  protected abstract boolean filter(@Nonnull ClassType type);

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    if (!filter(type)) {
      return Optional.empty();
    }
    return inputLocation.getClassSource(type, view);
  }

  @Nonnull
  @Override
  public Collection<? extends SootClassSource> getClassSources(@Nonnull View view) {
    // possibility to streamify this method to apply the filter at earlier stage i.e. before
    // creating the ClassSources would be a faster approach..
    return inputLocation.getClassSources(view).stream()
        .filter(type -> filter(type.getClassType()))
        .collect(Collectors.toList());
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return inputLocation.getSourceType();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return inputLocation.getBodyInterceptors();
  }
}
