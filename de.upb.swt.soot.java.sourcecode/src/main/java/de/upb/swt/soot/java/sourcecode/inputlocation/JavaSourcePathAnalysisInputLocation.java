package de.upb.swt.soot.java.sourcecode.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann and other
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

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.sourcecode.frontend.WalaJavaClassProvider;
import java.nio.file.Paths;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link AnalysisInputLocation} interface for the Java source code path.
 *
 * <p>Provides default {@link ClassLoadingOptions} from {@link SourcecodeClassLoadingOptions}.
 *
 * @author Linghui Luo
 */
public class JavaSourcePathAnalysisInputLocation implements AnalysisInputLocation<JavaSootClass> {

  private static final Logger log =
      LoggerFactory.getLogger(JavaSourcePathAnalysisInputLocation.class);

  @Nonnull private final Set<String> sourcePaths;
  @Nonnull private final WalaJavaClassProvider classProvider;

  @Nullable private final String exclusionFilePath;

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(@Nonnull Set<String> sourcePaths) {
    this(sourcePaths, null);
  }

  public JavaSourcePathAnalysisInputLocation(@Nonnull String sourcePath) {
    this(Collections.singleton(sourcePath), null);
  }

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(
      @Nonnull Set<String> sourcePaths, @Nullable String exclusionFilePath) {
    this.sourcePaths = sourcePaths;
    this.exclusionFilePath = exclusionFilePath;
    this.classProvider = new WalaJavaClassProvider(sourcePaths, exclusionFilePath);
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory, @Nonnull View<?> view) {
    return classProvider.getClassSources();
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType type, @Nonnull View<?> view) {
    for (String path : sourcePaths) {
      try {
        return Optional.ofNullable(classProvider.createClassSource(this, Paths.get(path), type));
      } catch (ResolveException e) {
        log.debug(type + " not found in sourcePath " + path, e);
      }
    }
    return Optional.empty();
  }

  public String getExclusionFilePath() {
    return exclusionFilePath;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourcePaths, exclusionFilePath);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JavaSourcePathAnalysisInputLocation)) {
      return false;
    }
    return sourcePaths.equals(((JavaSourcePathAnalysisInputLocation) o).sourcePaths)
        && exclusionFilePath.equals(((JavaSourcePathAnalysisInputLocation) o).exclusionFilePath);
  }
}
