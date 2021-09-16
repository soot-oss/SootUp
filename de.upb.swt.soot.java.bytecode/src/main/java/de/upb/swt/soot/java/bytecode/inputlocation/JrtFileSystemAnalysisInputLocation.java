package de.upb.swt.soot.java.bytecode.inputlocation;
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

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.frontend.AsmModuleSource;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.ModuleInfoAnalysisInputLocation;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation implements ModuleInfoAnalysisInputLocation {

  private static final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
  Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();
  boolean isResolved = false;

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull View<?> view) {
    JavaClassType klassType = (JavaClassType) classType;

    ClassProvider<JavaSootClass> classProvider =
        new AsmJavaClassProvider(((View<JavaSootClass>) view).getBodyInterceptors(this));
    Path filepath =
        theFileSystem.getPath(
            klassType.getFullyQualifiedName().replace('.', '/')
                + "."
                + classProvider.getHandledFileType().getExtension());

    // parse as module
    if (klassType.getPackageName() instanceof ModulePackageName) {

      ModulePackageName modulePackageSignature = (ModulePackageName) klassType.getPackageName();

      final Path module =
          theFileSystem.getPath(
              "modules", modulePackageSignature.getModuleSignature().getModuleName());
      Path foundClass = module.resolve(filepath);
      if (Files.isRegularFile(foundClass)) {
        return Optional.of(classProvider.createClassSource(this, foundClass, klassType));
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
            return Optional.of(classProvider.createClassSource(this, foundfile, klassType));
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

    ClassProvider<JavaSootClass> classProvider =
        new AsmJavaClassProvider(((View<JavaSootClass>) view).getBodyInterceptors(this));

    String moduleInfoFilename =
        JavaModuleIdentifierFactory.MODULE_INFO_FILE
            + "."
            + classProvider.getHandledFileType().getExtension();

    final Path archiveRoot = theFileSystem.getPath("modules", moduleSignature.getModuleName());
    try {

      return Files.walk(archiveRoot)
          .filter(
              filePath ->
                  !Files.isDirectory(filePath)
                      && filePath
                          .toString()
                          .endsWith(classProvider.getHandledFileType().getExtension())
                      && !filePath.toString().endsWith(moduleInfoFilename))
          .flatMap(
              p -> {
                return StreamUtils.optionalToStream(
                    Optional.of(
                        classProvider.createClassSource(
                            this,
                            p,
                            this.fromPath(
                                p.subpath(2, p.getNameCount()),
                                p.subpath(1, 2),
                                identifierFactory))));
              });
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
          .getClassType(
              sig.getClassName(), sig.getPackageName().getPackageName(), moduleDir.toString());
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

  @Override
  public boolean equals(Object o) {
    return o instanceof JrtFileSystemAnalysisInputLocation;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
