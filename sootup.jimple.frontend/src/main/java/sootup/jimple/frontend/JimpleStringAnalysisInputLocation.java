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
import java.util.*;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.core.OverridingJavaClassSource;

/**
 * This AnalysisInputLocation encapsulates and represents a single Jimple "file" - the contents of
 * the Class are given via String.
 *
 * <p>see JimpleStringAnalysisInputLocationTest for an example.
 */
public class JimpleStringAnalysisInputLocation implements AnalysisInputLocation {

  @NonNull final Path path = Paths.get("only-in-memory.jimple");
  @NonNull final List<BodyInterceptor> bodyInterceptors;
  @NonNull final SourceType sourceType;
  @NonNull private final Map<View, OverridingJavaClassSource> cache;
  private final String jimpleFileContents;

  public JimpleStringAnalysisInputLocation(@NonNull String jimpleFileContents) {
    this(
        jimpleFileContents,
        SourceType.Application,
        BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public JimpleStringAnalysisInputLocation(
      @NonNull String jimpleFileContents,
      @NonNull SourceType sourceType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    this.jimpleFileContents = jimpleFileContents;
    this.bodyInterceptors = bodyInterceptors;
    this.sourceType = sourceType;
    this.cache = new IdentityHashMap<>();
  }

  private synchronized OverridingJavaClassSource getOverridingClassSource(@NonNull View view) {
    return cache.computeIfAbsent(
        view,
        (v) -> {
          try {
            JimpleConverter jimpleConverter = new JimpleConverter();
            return jimpleConverter.run(
                CharStreams.fromString(jimpleFileContents), this, path, bodyInterceptors, v);
          } catch (Exception e) {
            throw new IllegalArgumentException("No valid Jimple given.", e);
          }
        });
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    OverridingJavaClassSource classSource = getOverridingClassSource(view);
    if (!type.equals(classSource.getClassType())) {
      return Optional.empty();
    }
    return Optional.of(classSource);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return Stream.of(getOverridingClassSource(view));
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }
}
