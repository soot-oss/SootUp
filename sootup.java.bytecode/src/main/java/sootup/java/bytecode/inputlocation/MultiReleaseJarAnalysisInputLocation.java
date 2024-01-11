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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.Language;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.AsmModuleSource;
import sootup.java.core.*;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;

/**
 * If the user wants to analyze a Multi-Release Jar, they have to specify the language level to
 * analyze explicitly
 */
public class MultiReleaseJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation
    implements ModuleInfoAnalysisInputLocation {

  @Nonnull private final Language language;
  @Nonnull private final int[] availableVersions;

  @Nonnull
  private final Map<Integer, Map<ModuleSignature, JavaModuleInfo>> moduleInfoMap = new HashMap<>();

  @Nonnull private final Map<Integer, List<AnalysisInputLocation>> inputLocations = new HashMap<>();

  @Nonnull private final List<AnalysisInputLocation> baseInputLocations = new ArrayList<>();

  boolean isResolved = false;

  public MultiReleaseJarAnalysisInputLocation(
      @Nonnull Path path, @Nullable SourceType srcType, @Nonnull Language language) {
    super(path, srcType);
    this.language = language;

    int[] tmp;
    try {
      FileSystem fs = fileSystemCache.get(path);
      final Path archiveRoot = fs.getPath("/");
      tmp =
          Files.list(archiveRoot.getFileSystem().getPath("/META-INF/versions/"))
              .map(dir -> dir.getFileName().toString().replace("/", ""))
              .mapToInt(Integer::new)
              .sorted()
              .toArray();
    } catch (IOException | ExecutionException e) {
      e.printStackTrace();
      tmp = new int[] {};
    }
    availableVersions = tmp;

    discoverInputLocations(srcType);
  }

  /** Discovers all input locations for different java versions in this multi release jar */
  private void discoverInputLocations(@Nullable SourceType srcType) {
    FileSystem fs = null;
    try {
      fs = fileSystemCache.get(path);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    final Path archiveRoot = fs.getPath("/");
    final String moduleInfoFilename = JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class";

    baseInputLocations.add(PathBasedAnalysisInputLocation.create(archiveRoot, srcType));

    String sep = archiveRoot.getFileSystem().getSeparator();

    if (!isResolved) {

      for (int i = availableVersions.length - 1; i >= 0; i--) {
        inputLocations.put(availableVersions[i], new ArrayList<>());

        final Path versionRoot =
            archiveRoot.getFileSystem().getPath("/META-INF/versions/" + availableVersions[i] + sep);

        // only versions >= 9 support java modules
        if (availableVersions[i] > 8) {
          moduleInfoMap.put(availableVersions[i], new HashMap<>());
          try (DirectoryStream<Path> stream = Files.newDirectoryStream(versionRoot)) {
            for (Path entry : stream) {

              Path mi = path.resolve(moduleInfoFilename);

              if (Files.exists(mi)) {
                JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
                ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
                JavaModulePathAnalysisInputLocation inputLocation =
                    new JavaModulePathAnalysisInputLocation(
                        versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

                inputLocations.get(availableVersions[i]).add(inputLocation);
                moduleInfoMap.get(availableVersions[i]).put(moduleSignature, moduleInfo);
              }

              if (Files.isDirectory(entry)) {
                mi = versionRoot.resolve(moduleInfoFilename);

                if (Files.exists(mi)) {
                  JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
                  ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
                  JavaModulePathAnalysisInputLocation inputLocation =
                      new JavaModulePathAnalysisInputLocation(
                          versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

                  inputLocations.get(availableVersions[i]).add(inputLocation);
                  moduleInfoMap.get(availableVersions[i]).put(moduleSignature, moduleInfo);
                }
                // else TODO [bh] can we have automatic modules here?
              }
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        // if there was no module or the version is not > 8, we just add a directory based input
        // location
        if (inputLocations.get(availableVersions[i]).size() == 0) {
          inputLocations
              .get(availableVersions[i])
              .add(PathBasedAnalysisInputLocation.create(versionRoot, srcType));
        }
      }
    }

    isResolved = true;
  }

  @Override
  @Nonnull
  public Optional<JavaSootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {

    Collection<AnalysisInputLocation> il = getBestMatchingInputLocationsRaw(language.getVersion());

    Collection<AnalysisInputLocation> baseIl = getBaseInputLocations();

    if (type instanceof ModuleJavaClassType) {
      il =
          il.stream()
              .filter(location -> location instanceof ModuleInfoAnalysisInputLocation)
              .collect(Collectors.toList());
      baseIl =
          baseIl.stream()
              .filter(location -> location instanceof ModuleInfoAnalysisInputLocation)
              .collect(Collectors.toList());
    } else {
      il =
          il.stream()
              .filter(location -> !(location instanceof ModuleInfoAnalysisInputLocation))
              .collect(Collectors.toList());
      baseIl =
          baseIl.stream()
              .filter(location -> !(location instanceof ModuleInfoAnalysisInputLocation))
              .collect(Collectors.toList());
    }

    Optional<JavaSootClassSource> foundClass =
        il.stream()
            .map(location -> location.getClassSource(type, view))
            .filter(Optional::isPresent)
            .limit(1)
            .map(Optional::get)
            .map(src -> (JavaSootClassSource) src)
            .findAny();

    if (foundClass.isPresent()) {
      return foundClass;
    } else {
      return baseIl.stream()
          .map(location -> location.getClassSource(type, view))
          .filter(Optional::isPresent)
          .limit(1)
          .map(Optional::get)
          .map(src -> (JavaSootClassSource) src)
          .findAny();
    }
  }

  @Nonnull
  @Override
  public Collection<JavaSootClassSource> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View view) {
    return inputLocations.get(language.getVersion()).stream()
        .filter(location -> location instanceof ModuleInfoAnalysisInputLocation)
        .map(
            location ->
                ((ModuleInfoAnalysisInputLocation) location)
                    .getModulesClassSources(moduleSignature, view))
        .flatMap(Collection::stream)
        .map(src -> (JavaSootClassSource) src)
        .collect(Collectors.toList());
  }

  /**
   * Returns the best matching input locations or the base input location.
   *
   * @param javaVersion version to find best match to
   * @return best match or base input locations
   */
  private Collection<AnalysisInputLocation> getBestMatchingInputLocationsRaw(int javaVersion) {
    for (int i = availableVersions.length - 1; i >= 0; i--) {

      if (availableVersions[i] > javaVersion) continue;

      return new ArrayList<>(inputLocations.get(availableVersions[i]));
    }

    return getBaseInputLocations();
  }

  private Collection<AnalysisInputLocation> getBaseInputLocations() {
    return baseInputLocations;
  }

  @Override
  @Nonnull
  public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
    Collection<AnalysisInputLocation> il = getBestMatchingInputLocationsRaw(language.getVersion());

    Collection<JavaSootClassSource> result =
        il.stream()
            .map(location -> location.getClassSources(view))
            .flatMap(Collection::stream)
            .map(src -> (JavaSootClassSource) src)
            .collect(Collectors.toList());

    if (il != getBaseInputLocations()) {

      Collection<JavaSootClassSource> baseSources =
          getBaseInputLocations().stream()
              .map(location -> location.getClassSources(view))
              .flatMap(Collection::stream)
              .map(src -> (JavaSootClassSource) src)
              .collect(Collectors.toList());

      baseSources.forEach(
          cs -> {
            // do not add duplicate class sources
            if (result.stream()
                .noneMatch(
                    bestMatchCS ->
                        bestMatchCS
                            .getClassType()
                            .getFullyQualifiedName()
                            .equals(cs.getClassType().getFullyQualifiedName()))) {
              result.add(cs);
            }
          });
    }

    return result;
  }

  @Nonnull
  @Override
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View view) {
    return Optional.ofNullable(moduleInfoMap.get(language.getVersion()).get(sig));
  }

  @Nonnull
  @Override
  public Set<ModuleSignature> getModules(View view) {
    return inputLocations.get(language.getVersion()).stream()
        .filter(e -> e instanceof ModuleInfoAnalysisInputLocation)
        .map(e -> ((ModuleInfoAnalysisInputLocation) e).getModules(view))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Nonnull
  public Language getLanguage() {
    return this.language;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PathBasedAnalysisInputLocation)) {
      return false;
    }
    return path.equals(((PathBasedAnalysisInputLocation) o).path);
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }


  @Nonnull
  @Override
  protected String fromPath(@Nonnull Path baseDirPath, Path packageNamePathAndClass) {
    // FIXME: [ms] implement specific handling of the versioned path
    return super.fromPath(baseDirPath, packageNamePathAndClass);
  }

  private static boolean isMultiReleaseJar(Path path) {
    try {
      FileInputStream inputStream = new FileInputStream(path.toFile());
      JarInputStream jarStream = new JarInputStream(inputStream);
      Manifest mf = jarStream.getManifest();

      if (mf == null) {
        return false;
      }

      Attributes attributes = mf.getMainAttributes();

      String value = attributes.getValue("Multi-Release");

      return Boolean.parseBoolean(value);
    } catch (IOException e) {
      throw new IllegalArgumentException("File not found.", e);
    }
  }
}
