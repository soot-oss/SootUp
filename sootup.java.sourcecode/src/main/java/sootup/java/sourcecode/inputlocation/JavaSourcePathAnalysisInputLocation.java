package sootup.java.sourcecode.inputlocation;
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

import java.nio.file.Paths;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.sourcecode.frontend.WalaJavaClassProvider;

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
   * Variable to track if user has specified the SourceType. By default, it will be set to false.
   */
  private SourceType srcType = null;

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

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param srcType the source type for the path can be Library, Application, Phantom.
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(
      @Nullable SourceType srcType, @Nonnull Set<String> sourcePaths) {
    this(sourcePaths, null);
    setSpecifiedAsBuiltInByUser(srcType);
    // this.classProvider = new WalaJavaClassProvider(sourcePaths, exclusionFilePath,
    // DefaultSourceTypeSpecifier.getInstance());
  }

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param srcType the source type for the path can be Library, Application, Phantom.
   * @param sourcePath the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(
      @Nullable SourceType srcType, @Nonnull String sourcePath) {
    this(Collections.singleton(sourcePath), null);
    setSpecifiedAsBuiltInByUser(srcType);
  }

  /**
   * Create a {@link JavaSourcePathAnalysisInputLocation} which locates java source code in the
   * given source path.
   *
   * @param srcType the source type for the path can be Library, Application, Phantom.
   * @param sourcePaths the source code path to search in
   */
  public JavaSourcePathAnalysisInputLocation(
      @Nonnull SourceType srcType,
      @Nonnull Set<String> sourcePaths,
      @Nullable String exclusionFilePath) {
    this.sourcePaths = sourcePaths;
    this.exclusionFilePath = exclusionFilePath;
    this.classProvider = new WalaJavaClassProvider(sourcePaths, exclusionFilePath);
    setSpecifiedAsBuiltInByUser(srcType);
  }

  /**
   * The method sets the value of the variable srcType.
   *
   * @param srcType the source type for the path can be Library, Application, Phantom.
   */
  public void setSpecifiedAsBuiltInByUser(@Nullable SourceType srcType) {
    this.srcType = srcType;
  }

  @Override
  public SourceType getSourceType() {
    return srcType;
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull View<?> view) {

    return classProvider.getClassSources(srcType);
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType type, @Nonnull View<?> view) {
    for (String path : sourcePaths) {
      try {
        return classProvider.createClassSource(this, Paths.get(path), type);
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
