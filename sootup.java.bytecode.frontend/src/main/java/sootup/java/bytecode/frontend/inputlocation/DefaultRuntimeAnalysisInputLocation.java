package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;

/** AnalysisInputLocation that points to the shipped Java Runtime of the current JVM execution */
public class DefaultRuntimeAnalysisInputLocation implements AnalysisInputLocation {

  @NonNull private final AnalysisInputLocation backingInputLocation;

  public DefaultRuntimeAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public DefaultRuntimeAnalysisInputLocation(@NonNull SourceType srcType) {
    this(srcType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public DefaultRuntimeAnalysisInputLocation(
      @NonNull SourceType srcType, @NonNull List<BodyInterceptor> bodyInterceptors) {

    String version = System.getProperty("java.version");
    // are we using Java 8 or lower in the current JVM execution?
    if (version.startsWith("1.")) {
      backingInputLocation = new DefaultRTJarAnalysisInputLocation(srcType, bodyInterceptors);
    } else {
      backingInputLocation = new JrtFileSystemAnalysisInputLocation(srcType, bodyInterceptors);
    }
  }

  @NonNull
  @Override
  public Optional<? extends SootClassSource> getClassSource(
      @NonNull ClassType type, @NonNull View view) {
    return backingInputLocation.getClassSource(type, view);
  }

  @NonNull
  @Override
  public Stream<? extends SootClassSource> getClassSources(@NonNull View view) {
    return backingInputLocation.getClassSources(view);
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return backingInputLocation.getSourceType();
  }

  @NonNull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    return backingInputLocation.getBodyInterceptors();
  }
}
