package sootup.java.bytecode.frontend.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2023 Markus Schmidt
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

import java.nio.file.Paths;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;

/**
 * Refers to the rt.jar from &lt;=Java8 as an AnalysisInputLocation requires: JAVA_HOME to be set
 * and expects the jar in the "lib/" subdirectory. If you need to include the rt.jar from a custom
 * Location please make use of JavaClassPathAnalysisInputLocation.
 *
 * <p>Info: This only works if you are running java 8 or older. Otherwise use {@link
 * JrtFileSystemAnalysisInputLocation}.
 */
class DefaultRTJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  public DefaultRTJarAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public DefaultRTJarAnalysisInputLocation(@Nonnull SourceType srcType) {
    super(Paths.get(System.getProperty("java.home") + "/lib/rt.jar"), srcType);
  }

  public DefaultRTJarAnalysisInputLocation(
      @Nonnull SourceType srcType, @Nonnull List<BodyInterceptor> bodyInterceptors) {
    super(Paths.get(System.getProperty("java.home") + "/lib/rt.jar"), srcType, bodyInterceptors);
  }
}
