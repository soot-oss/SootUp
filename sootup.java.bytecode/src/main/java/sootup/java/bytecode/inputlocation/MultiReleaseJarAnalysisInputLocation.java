package sootup.java.bytecode.inputlocation;

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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.Language;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
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

  @Nonnull protected final Language language;
  @Nonnull protected final List<Integer> availableVersions;

  @Nonnull
  protected final Map<Integer, AnalysisInputLocation> inputLocations = new LinkedHashMap<>();

  public MultiReleaseJarAnalysisInputLocation(
      @Nonnull Path path, @Nonnull SourceType srcType, @Nonnull Language language) {
    super(path, srcType);
    this.language = language;

    if (!isMultiReleaseJar(path)) {
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

    availableVersions = new ArrayList<>();
    try (Stream<Path> list = Files.list(versionedRoot)) {
      list.map(
              dir -> {
                String versionDirName = dir.getFileName().toString();
                return versionDirName.substring(0, versionDirName.length() - 1);
              })
          .map(Integer::new)
          .sorted()
          .forEach(availableVersions::add);
    } catch (IOException e) {
      throw new IllegalStateException("Can not index the given file.", e);
    }

    for (int i = availableVersions.size() - 1; i >= 0; i--) {
      Integer version = availableVersions.get(i);
      if (version > language.getVersion()) {
        // TODO: use binSearch to find desired versions more efficiently?
        continue;
      }
      final Path versionRoot =
          archiveRoot.getFileSystem().getPath("/META-INF", "versions", version.toString());
      inputLocations.put(version, createAnalysisInputLocation(versionRoot));
    }
    inputLocations.put(DEFAULT_VERSION, createAnalysisInputLocation(archiveRoot));
  }

  protected AnalysisInputLocation createAnalysisInputLocation(Path archiveRoot) {
    // FIXME: make the inputlocation ignore /META_INF/*
    return PathBasedAnalysisInputLocation.create(archiveRoot, sourceType);
  }

  @Override
  @Nonnull
  public Optional<JavaSootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {
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
  @Nonnull
  public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {

    Collection<JavaSootClassSource> classSources = new ArrayList<>();
    inputLocations.values().stream()
        .map(location -> location.getClassSources(view))
        .flatMap(Collection::stream)
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

    return classSources;
  }

  @Nonnull
  public Language getLanguage() {
    return language;
  }

  @Nonnull
  public List<Integer> getAvailableVersions() {
    return availableVersions;
  }

  public static boolean isMultiReleaseJar(Path path) {
    try {
      FileInputStream inputStream = new FileInputStream(path.toFile());
      JarInputStream jarStream = new JarInputStream(inputStream);
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
      throw new IllegalArgumentException("File not found.", e);
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
