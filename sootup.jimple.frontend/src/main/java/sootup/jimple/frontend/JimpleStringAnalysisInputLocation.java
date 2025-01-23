package sootup.jimple.frontend;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2024 Markus Schmidt
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
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;

/**
 * This AnalysisInputLocation encapsulates and represents a single Jimple "file" - the contents of
 * the Class are given via String.
 *
 * <p>see JimpleStringAnalysisInputLocationTest for an example.
 */
public class JimpleStringAnalysisInputLocation implements AnalysisInputLocation {

  @Nonnull final Path path = Paths.get("only-in-memory.jimple");
  @Nonnull final List<BodyInterceptor> bodyInterceptors;
  @Nonnull final SourceType sourceType;
  private String jimpleFileContents;

  public JimpleStringAnalysisInputLocation(@Nonnull String jimpleFileContents) {
    this(
        jimpleFileContents,
        SourceType.Application,
        BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public JimpleStringAnalysisInputLocation(
      @Nonnull String jimpleFileContents,
      @Nonnull SourceType sourceType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.jimpleFileContents = jimpleFileContents;
    this.bodyInterceptors = bodyInterceptors;
    this.sourceType = sourceType;
  }

  private OverridingClassSource getOverridingClassSource(
      String jimpleFileContents, List<BodyInterceptor> bodyInterceptors, View view) {
    final @Nonnull OverridingClassSource classSource;
    try {
      JimpleConverter jimpleConverter = new JimpleConverter();
      classSource =
          jimpleConverter.run(
              CharStreams.fromString(jimpleFileContents), this, path, bodyInterceptors, view);
    } catch (Exception e) {
      throw new IllegalArgumentException("No valid Jimple given.", e);
    }
    return classSource;
  }

  @Nonnull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @Nonnull ClassType type, @Nonnull View view) {
    return Optional.of(getOverridingClassSource(jimpleFileContents, bodyInterceptors, view));
  }

  @Nonnull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@Nonnull View view) {
    return Stream.of(getOverridingClassSource(jimpleFileContents, bodyInterceptors, view));
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }
}
