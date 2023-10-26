package sootup.java.bytecode.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Christian Brüggemann and others
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

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.AsmJavaClassProvider;
import sootup.java.bytecode.frontend.AsmModuleSource;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.JavaSootClass;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation implements ModuleInfoAnalysisInputLocation {

  private static final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
  Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();
  boolean isResolved = false;

  @Nonnull private final SourceType sourceType;

  public JrtFileSystemAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public JrtFileSystemAnalysisInputLocation(@Nonnull SourceType sourceType) {
    this.sourceType = sourceType;
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull View<?> view) {
    JavaClassType klassType = (JavaClassType) classType;

    ClassProvider<JavaSootClass> classProvider = new AsmJavaClassProvider(view);
    Path filepath =
        theFileSystem.getPath(
            klassType.getFullyQualifiedName().replace('.', '/')
                + classProvider.getHandledFileType().getExtensionWithDot());

    // parse as module
    if (klassType.getPackageName() instanceof ModulePackageName) {

      ModulePackageName modulePackageSignature = (ModulePackageName) klassType.getPackageName();

      final Path module =
          theFileSystem.getPath(
              "modules", modulePackageSignature.getModuleSignature().getModuleName());
      Path foundClass = module.resolve(filepath);
      if (Files.isRegularFile(foundClass)) {
        return classProvider.createClassSource(this, foundClass, klassType);
      } else {
        return Optional.empty();
      }
    }

    // module information does not exist in Signature -> search for class
    final Path moduleRoot = theFileSystem.getPath("modules");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          // check each module folder for the class
          Path foundfile = entry.resolve(filepath);
          if (Files.isRegularFile(foundfile)) {
            return classProvider.createClassSource(this, foundfile, klassType);
          }
        }
      }
    } catch (IOException e) {
      throw new ResolveException("Error loading a module", moduleRoot, e);
    }

    return Optional.empty();
  }

  /** Retreive CLassSources of a module specified by methodSignature */
  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View<?> view) {
    return getClassSourcesInternal(moduleSignature, view.getIdentifierFactory(), view)
        .collect(Collectors.toList());
  }

  @Nonnull
  protected Stream<AbstractClassSource<JavaSootClass>> getClassSourcesInternal(
      @Nonnull ModuleSignature moduleSignature,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull View<?> view) {

    ClassProvider<JavaSootClass> classProvider = new AsmJavaClassProvider(view);

    String moduleInfoFilename =
        JavaModuleIdentifierFactory.MODULE_INFO_FILE
            + classProvider.getHandledFileType().getExtensionWithDot();

    final Path archiveRoot = theFileSystem.getPath("modules", moduleSignature.getModuleName());
    try {

      return Files.walk(archiveRoot)
          .filter(
              filePath ->
                  !Files.isDirectory(filePath)
                      && filePath
                          .toString()
                          .endsWith(classProvider.getHandledFileType().getExtensionWithDot())
                      && !filePath.toString().endsWith(moduleInfoFilename))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      classProvider.createClassSource(
                          this,
                          p,
                          this.fromPath(
                              p.subpath(2, p.getNameCount()),
                              p.subpath(1, 2),
                              identifierFactory))));
    } catch (IOException e) {
      throw new ResolveException("Error loading module " + moduleSignature, archiveRoot, e);
    }
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull View<?> view) {

    Collection<ModuleSignature> moduleSignatures = discoverModules();
    return moduleSignatures.stream()
        .flatMap(sig -> getClassSourcesInternal(sig, view.getIdentifierFactory(), view))
        .collect(Collectors.toList());
  }

  /**
   * Discover and return all modules contained in the jrt filesystem.
   *
   * @return Collection of found module names.
   */
  @Nonnull
  public Collection<ModuleSignature> discoverModules() {
    if (!isResolved) {
      final Path moduleRoot = theFileSystem.getPath("modules");
      final String moduleInfoFilename = JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class";
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
        {
          for (Path entry : stream) {
            if (Files.isDirectory(entry)) {
              ModuleSignature moduleSignature =
                  JavaModuleIdentifierFactory.getModuleSignature(entry.subpath(1, 2).toString());
              Path moduleInfo = entry.resolve(moduleInfoFilename);
              if (Files.exists(moduleInfo)) {
                moduleInfoMap.put(moduleSignature, new AsmModuleSource(moduleInfo));
              } else {
                moduleInfoMap.put(
                    moduleSignature, JavaModuleInfo.createAutomaticModuleInfo(moduleSignature));
              }
            }
          }
        }
      } catch (IOException e) {
        throw new ResolveException("Error while discovering modules", moduleRoot, e);
      }
      isResolved = true;
    }
    return moduleInfoMap.keySet();
  }

  @Nonnull
  private JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    // else use the module system and create fully class signature
    // we do not have a base directory here, the moduleDir is actually not a directory
    JavaClassType sig = (JavaClassType) identifierFactory.fromPath(Paths.get(""), filename);

    if (identifierFactory instanceof JavaModuleIdentifierFactory) {
      return ((JavaModuleIdentifierFactory) identifierFactory)
          .getClassType(sig.getClassName(), sig.getPackageName().getName(), moduleDir.toString());
    }

    // if we are using the normal signature factory, then trim the module from the path
    return sig;
  }

  @Nonnull
  @Override
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View<?> view) {
    if (!isResolved) {
      discoverModules();
    }
    return Optional.ofNullable(moduleInfoMap.get(sig));
  }

  @Nonnull
  @Override
  public Set<ModuleSignature> getModules(View<?> view) {
    if (!isResolved) {
      discoverModules();
    }
    return Collections.unmodifiableSet(moduleInfoMap.keySet());
  }

  @Nonnull
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof JrtFileSystemAnalysisInputLocation;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
