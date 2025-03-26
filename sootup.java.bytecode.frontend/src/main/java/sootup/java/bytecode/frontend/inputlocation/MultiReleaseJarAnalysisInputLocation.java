package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Markus Schmidt and others
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.core.JavaSootClassSource;

/**
 * If the user wants to analyze a Multi-Release Jar, they have to specify the language level to
 * analyze explicitly. if there is no match for the given language level, the default location
 * inside the jar will be used.
 *
 * <p>see <a href="https://openjdk.org/jeps/238">JEP 238</a>
 */
public class MultiReleaseJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  // ModularInputLocations exist since Java 9 -> in previous language levels its structured like a
  // "usual" Jar
  protected static final Integer DEFAULT_VERSION = 0;

  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  @NonNull
  protected final Map<Integer, AnalysisInputLocation> inputLocations = new LinkedHashMap<>();

  private final int version;

  public static AnalysisInputLocation create(
      @NonNull Path path,
      @NonNull SourceType srcType,
      int version,
      List<BodyInterceptor> bodyInterceptors) {

    if (isMultiReleaseJar(path)) {
      return new MultiReleaseJarAnalysisInputLocation(
          path, srcType, version, bodyInterceptors, true);
    }

    return create(
        path, srcType, bodyInterceptors, Collections.singletonList(Paths.get("/META-INF")));
  }

  public MultiReleaseJarAnalysisInputLocation(@NonNull Path path, int version) {
    this(path, SourceType.Application, version);
  }

  public MultiReleaseJarAnalysisInputLocation(
      @NonNull Path path, @NonNull SourceType srcType, int version) {
    this(path, srcType, version, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public MultiReleaseJarAnalysisInputLocation(
      @NonNull Path path,
      @NonNull SourceType srcType,
      int version,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    this(path, srcType, version, bodyInterceptors, isMultiReleaseJar(path));
  }

  protected MultiReleaseJarAnalysisInputLocation(
      @NonNull Path path,
      @NonNull SourceType srcType,
      int version,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      boolean isMultiRelease) {
    super(path, srcType);
    this.version = version;
    this.bodyInterceptors = bodyInterceptors;

    if (!isMultiRelease) {
      throw new IllegalArgumentException("The given path does not point to a multi release jar.");
    }

    FileSystem fs;
    try {
      fs = fileSystemCache.get(path);
    } catch (ExecutionException e) {
      throw new IllegalArgumentException("Could not open filesystemcache.", e);
    }

    final Path archiveRoot = fs.getPath("/");
    Path versionedRoot = archiveRoot.getFileSystem().getPath("/META-INF/versions/");

    try (Stream<Path> list = Files.list(versionedRoot)) {
      list.map(dir -> dir.getFileName().toString())
          .map(Integer::valueOf)
          .filter(ver -> ver <= version)
          .sorted(Comparator.reverseOrder())
          .forEach(
              ver -> {
                final Path versionRoot =
                    archiveRoot.getFileSystem().getPath("/META-INF", "versions", ver.toString());
                inputLocations.put(
                    ver,
                    create(versionRoot, sourceType, bodyInterceptors, Collections.emptyList()));
              });

      inputLocations.put(
          DEFAULT_VERSION,
          createAnalysisInputLocation(archiveRoot, srcType, getBodyInterceptors()));
    } catch (IOException e) {
      throw new IllegalStateException("Can not index the given file.", e);
    }
  }

  protected AnalysisInputLocation createAnalysisInputLocation(
      Path archiveRoot, SourceType sourceType, List<BodyInterceptor> bodyInterceptors) {
    return create(
        archiveRoot,
        sourceType,
        bodyInterceptors,
        Collections.singletonList(Paths.get("/META-INF")));
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {
    for (AnalysisInputLocation analysisInputLocation : inputLocations.values()) {
      Optional<? extends SootClassSource> classSource =
          analysisInputLocation.getClassSource(type, view);
      if (classSource.isPresent()) {
        SootClassSource src = classSource.get();
        JavaSootClassSource javaSootClassSource = (JavaSootClassSource) src;
        return Optional.of(javaSootClassSource);
      }
    }
    return Optional.empty();
  }

  @Override
  @NonNull
  public Stream<JavaSootClassSource> getClassSources(@NonNull View view) {
    Collection<JavaSootClassSource> classSources = new ArrayList<>();
    inputLocations.values().stream()
        .flatMap(location -> location.getClassSources(view))
        .map(src -> (JavaSootClassSource) src)
        .forEach(
            cs -> {
              // do not add duplicate class sources
              if (classSources.stream()
                  .noneMatch(
                      bestMatchCS ->
                          bestMatchCS
                              .getClassType()
                              .getFullyQualifiedName()
                              .equals(cs.getClassType().getFullyQualifiedName()))) {
                classSources.add(cs);
              }
            });

    // TODO: return stream directly
    return classSources.stream();
  }

  public int getVersion() {
    return version;
  }

  public static boolean isMultiReleaseJar(Path path) {
    try (FileInputStream inputStream = new FileInputStream(path.toFile());
        JarInputStream jarStream = new JarInputStream(inputStream)) {
      Manifest mf = jarStream.getManifest();

      if (mf == null) {
        return false;
      }

      Attributes attributes = mf.getMainAttributes();

      String value = attributes.getValue("Multi-Release");

      if (value == null) {
        return false;
      }

      return Boolean.parseBoolean(value);
    } catch (IOException e) {
      throw new IllegalArgumentException("Manifest file not found.", e);
    }
  }

  @Override
  @NonNull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  /**
   * lists all versions from the version directories inside the META-INF/ directory - excluding the
   * default implemention version
   */
  protected static List<Integer> getLanguageVersions(@NonNull Path path) {
    FileSystem fs;
    try {
      fs = fileSystemCache.get(path);
    } catch (ExecutionException e) {
      throw new IllegalArgumentException("Could not open filesystemcache.", e);
    }

    final Path archiveRoot = fs.getPath("/");
    Path versionedRoot = archiveRoot.getFileSystem().getPath("/META-INF/versions/");

    try (Stream<Path> list = Files.list(versionedRoot)) {
      return list.map(dir -> dir.getFileName().toString())
          .map(Integer::valueOf)
          .sorted()
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      throw new IllegalStateException("Can not index the given file.", e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MultiReleaseJarAnalysisInputLocation)) {
      return false;
    }
    return path.equals(((MultiReleaseJarAnalysisInputLocation) o).path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }
}
