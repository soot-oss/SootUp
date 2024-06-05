package sootup.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Kaustubh Kelkar and others
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

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.PathUtils;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.interceptors.BytecodeBodyInterceptors;

/**
 * An implementation of the {@link AnalysisInputLocation} interface for the Java class path. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 *
 * @author Manuel Benz created on 22.05.18
 * @author Kaustubh Kelkar updated on 20.07.2020
 */
public class JavaClassPathAnalysisInputLocation implements AnalysisInputLocation {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaClassPathAnalysisInputLocation.class);
  private static final @Nonnull String WILDCARD_CHAR = "*";

  @Nonnull private final Collection<AnalysisInputLocation> cpEntries;

  /** Variable to track if user has specified the SourceType. By default, it will be set to null. */
  private final SourceType srcType;

  private final List<BodyInterceptor> bodyInterceptors;

  /**
   * Creates a {@link JavaClassPathAnalysisInputLocation} which locates classes in the given class
   * path.
   *
   * @param classPath The class path to search in
   */
  public JavaClassPathAnalysisInputLocation(@Nonnull String classPath) {
    this(classPath, SourceType.Application);
  }

  public JavaClassPathAnalysisInputLocation(
      @Nonnull String classPath, @Nonnull SourceType srcType) {
    this(classPath, srcType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  /**
   * Creates a {@link JavaClassPathAnalysisInputLocation} which locates classes in the given class
   * path.
   *
   * @param classPath the class path to search in
   * @param srcType the source type for the path can be Library, Application, Phantom.
   */
  public JavaClassPathAnalysisInputLocation(
      @Nonnull String classPath,
      @Nonnull SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.srcType = srcType;
    this.bodyInterceptors = bodyInterceptors;

    cpEntries = classPath.length() <= 0 ? Collections.emptyList() : explodeClassPath(classPath);
    if (cpEntries.isEmpty()) {
      throw new IllegalArgumentException(
          "The given classpath does not point to any existing directory/directories.");
    }
  }

  @Override
  @Nonnull
  public SourceType getSourceType() {
    return srcType;
  }

  @Override
  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   *
   * @param paths entries as one string
   * @param fileSystem filesystem in which the paths are resolved
   * @return path entries
   */
  static @Nonnull Stream<Path> explode(@Nonnull String paths, FileSystem fileSystem) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    final Stream<Path> exploded =
        Stream.of(paths.split(regex)).flatMap(e -> handleWildCards(e, fileSystem));
    // we need to filter out duplicates of the same files to not generate duplicate input locations
    return exploded.map(Path::normalize).distinct();
  }

  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   *
   * @param paths entries as one string
   * @return path entries
   */
  static @Nonnull Stream<Path> explode(@Nonnull String paths) {
    return explode(paths, FileSystems.getDefault());
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those
   * directories have to be added to the class path.
   *
   * @param entry A class path entry
   * @param fileSystem The filesystem the paths should be resolved for
   * @return A stream of class path entries with wildcards exploded
   */
  private static @Nonnull Stream<Path> handleWildCards(
      @Nonnull String entry, FileSystem fileSystem) {
    if (entry.endsWith(WILDCARD_CHAR)) {
      Path baseDir = fileSystem.getPath(entry.substring(0, entry.indexOf(WILDCARD_CHAR)));
      try (final DirectoryStream<Path> paths = Files.newDirectoryStream(baseDir, "*.{jar,JAR}"); ) {
        return StreamUtils.iteratorToStream(paths.iterator());
      } catch (PatternSyntaxException | NotDirectoryException e) {
        throw new IllegalStateException("Malformed wildcard entry", e);
      } catch (IOException e) {
        throw new IllegalStateException("Couldn't access entries denoted by wildcard", e);
      }
    } else {
      return Stream.of(fileSystem.getPath(entry));
    }
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those
   * directories have to be added to the class path.
   *
   * @param entry A class path entry
   * @return A stream of class path entries with wildcards exploded
   */
  private static @Nonnull Stream<Path> handleWildCards(@Nonnull String entry) {
    return handleWildCards(entry, FileSystems.getDefault());
  }

  @Override
  @Nonnull
  public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<SootClassSource> found = new HashSet<>();
    for (AnalysisInputLocation inputLocation : cpEntries) {
      found.addAll(inputLocation.getClassSources(view));
    }
    return found.stream().map(src -> (JavaSootClassSource) src).collect(Collectors.toList());
  }

  @Override
  @Nonnull
  public Optional<JavaSootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {
    for (AnalysisInputLocation inputLocation : cpEntries) {
      final Optional<? extends SootClassSource> classSource =
          inputLocation.getClassSource(type, view);
      if (classSource.isPresent()) {
        return classSource.map(src -> (JavaSootClassSource) src);
      }
    }
    return Optional.empty();
  }

  @Nonnull
  private Optional<AnalysisInputLocation> inputLocationForPath(@Nonnull Path path) {
    if (Files.exists(path) && (Files.isDirectory(path) || PathUtils.isArchive(path))) {
      return Optional.of(PathBasedAnalysisInputLocation.create(path, srcType, bodyInterceptors));
    } else {
      logger.warn("Invalid/Unknown class path entry: " + path);
      return Optional.empty();
    }
  }

  /**
   * extract the classes from the classpath
   *
   * @param jarPath The jar path for which the classes need to be listed
   * @return list of classpath entries
   */
  private List<AnalysisInputLocation> explodeClassPath(@Nonnull String jarPath) {
    return explodeClassPath(jarPath, FileSystems.getDefault());
  }

  /**
   * extract the classes from the classpath
   *
   * @param jarPath The jar path for which the classes need to be listed
   * @param fileSystem the filesystem the path should be resolved for
   * @return list of classpath entries
   */
  private List<AnalysisInputLocation> explodeClassPath(
      @Nonnull String jarPath, @Nonnull FileSystem fileSystem) {
    return explode(jarPath, fileSystem)
        .flatMap(cp -> StreamUtils.optionalToStream(inputLocationForPath(cp)))
        .collect(Collectors.toList());
  }

  @Override
  public int hashCode() {
    return cpEntries.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JavaClassPathAnalysisInputLocation)) {
      return false;
    }
    return cpEntries.equals(((JavaClassPathAnalysisInputLocation) o).cpEntries);
  }
}
