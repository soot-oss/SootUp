package sootup.java.bytecode.frontend.inputlocation;

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
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.conversion.AsmJavaClassProvider;
import sootup.java.bytecode.frontend.conversion.AsmModuleSource;
import sootup.java.core.*;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation implements ModuleInfoAnalysisInputLocation {

  // FIXME: handle closing the filesystem resource
  private static final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
  private final Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();
  boolean isResolved = false;

  @NonNull private final SourceType sourceType;

  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  public JrtFileSystemAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public JrtFileSystemAnalysisInputLocation(@NonNull SourceType sourceType) {
    this(sourceType, BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public JrtFileSystemAnalysisInputLocation(
      @NonNull SourceType sourceType, @NonNull List<BodyInterceptor> bodyInterceptors) {
    this.sourceType = sourceType;
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(
      @NonNull ClassType classType, @NonNull View view) {
    JavaClassType klassType = (JavaClassType) classType;

    ClassProvider classProvider = getClassProvider(view);
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
        return classProvider
            .createClassSource(this, foundClass, klassType)
            .map(src -> (JavaSootClassSource) src);
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
            return classProvider
                .createClassSource(this, foundfile, klassType)
                .map(src -> (JavaSootClassSource) src);
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
  @NonNull
  public Stream<JavaSootClassSource> getModulesClassSources(
      @NonNull ModuleSignature moduleSignature, @NonNull View view) {
    return getClassSourcesInternal(moduleSignature, view.getIdentifierFactory(), view);
  }

  @NonNull
  protected Stream<JavaSootClassSource> getClassSourcesInternal(
      @NonNull ModuleSignature moduleSignature,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull View view) {

    ClassProvider classProvider = getClassProvider(view);

    String moduleInfoFilename =
        JavaModuleIdentifierFactory.MODULE_INFO_FILE
            + classProvider.getHandledFileType().getExtensionWithDot();

    final Path archiveRoot = theFileSystem.getPath("modules", moduleSignature.getModuleName());
    try (Stream<Path> paths = Files.walk(archiveRoot)) {
      // collect into a list and then return a stream, so we do not leak the Stream returned by
      // Files.walk
      List<JavaSootClassSource> javaSootClassSources =
          paths
              .filter(
                  filePath -> {
                    if (!Files.isDirectory(filePath)) {
                      String pathStr = filePath.toString();
                      return pathStr.endsWith(
                              classProvider.getHandledFileType().getExtensionWithDot())
                          && !pathStr.endsWith(moduleInfoFilename);
                    }
                    return false;
                  })
              .<SootClassSource>flatMap(
                  p ->
                      StreamUtils.optionalToStream(
                          classProvider.createClassSource(this, p, fromPath(p, identifierFactory))))
              .map(src -> (JavaSootClassSource) src)
              .toList();
      return javaSootClassSources.stream();
    } catch (IOException e) {
      throw new ResolveException("Error loading module " + moduleSignature, archiveRoot, e);
    }
  }

  protected ClassProvider getClassProvider(@NonNull View view) {
    return new AsmJavaClassProvider(view);
  }

  @Override
  public @NonNull Stream<JavaSootClassSource> getClassSources(@NonNull View view) {

    Collection<ModuleSignature> moduleSignatures = discoverModules();
    return moduleSignatures.stream()
        .flatMap(sig -> getClassSourcesInternal(sig, view.getIdentifierFactory(), view));
  }

  /**
   * Discover and return all modules contained in the jrt filesystem.
   *
   * @return Collection of found module names.
   */
  @NonNull
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

  @NonNull
  private JavaClassType fromPath(
      @NonNull Path p, @NonNull final IdentifierFactory identifierFactory) {

    final Path moduleDir = p.subpath(1, 2);
    final Path filename = p.subpath(2, p.getNameCount());

    final String fullyQualifiedName =
        FilenameUtils.removeExtension(
            filename.toString().replace(filename.getFileSystem().getSeparator(), "."));

    JavaClassType sig = (JavaClassType) identifierFactory.getClassType(fullyQualifiedName);

    // TODO: move to Module version
    if (identifierFactory instanceof JavaModuleIdentifierFactory) {
      return ((JavaModuleIdentifierFactory) identifierFactory)
          .getClassType(sig.getClassName(), sig.getPackageName().getName(), moduleDir.toString());
    }

    // if we are using the normal signature factory, then trim the module from the path
    return sig;
  }

  @NonNull
  @Override
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View view) {
    if (!isResolved) {
      discoverModules();
    }
    return Optional.ofNullable(moduleInfoMap.get(sig));
  }

  @NonNull
  @Override
  public Set<ModuleSignature> getModules(View view) {
    if (!isResolved) {
      discoverModules();
    }
    return Collections.unmodifiableSet(moduleInfoMap.keySet());
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @Override
  @NonNull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
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
