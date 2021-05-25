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
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.StreamUtils;
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
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation
    implements BytecodeAnalysisInputLocation, ModuleInfoAnalysisInputLocation {

  private static final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));
  Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();
  boolean isResolved = false;

  @Override
  public @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull ClassLoadingOptions classLoadingOptions) {
    JavaClassType klassType = (JavaClassType) classType;
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    ClassProvider<JavaSootClass> classProvider = new AsmJavaClassProvider(bodyInterceptors);
    Path filepath = klassType.toPath(classProvider.getHandledFileType(), theFileSystem);

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

  @Override
  public @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {

    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    ClassProvider<JavaSootClass> classProvider = new AsmJavaClassProvider(bodyInterceptors);

    final Path archiveRoot = theFileSystem.getPath("modules/");
    final FileType handledFileType = classProvider.getHandledFileType();
    try {

      discoverModules();
      //      moduleInfoMap.values().stream().forEach( moduleInfo -> moduleInfo.get);

      // FIXME WIP

      return Files.walk(archiveRoot)
          .filter(
              filePath ->
                  !filePath.endsWith(
                      JavaModuleIdentifierFactory.MODULE_INFO_CLASS.toString() + handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(
                          classProvider.createClassSource(
                              this,
                              p,
                              fromPath(
                                  p.subpath(2, p.getNameCount()),
                                  p.subpath(1, 2),
                                  identifierFactory)))))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new ResolveException("Error loading a module", archiveRoot, e);
    }
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
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
        {
          for (Path entry : stream) {
            if (Files.isDirectory(entry)) {
              ModuleSignature moduleSignature =
                  JavaModuleIdentifierFactory.getModuleSignature(entry.subpath(1, 2).toString());
              Path moduleInfo =
                  entry.resolve(JavaModuleIdentifierFactory.MODULE_INFO_CLASS + ".class");
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

  // TODO: originally, I could create a ModuleSingatre in any case, however, then
  // every signature factory needs a method signature created from path
  // however, I cannot think of a general way for java 9 modules anyway....
  // how to create the module name if we have a jar file..., or a multi jar, or the jrt file system
  // nevertheless, one general method Signature for all signatures seems reasonable
  private @Nonnull JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    // else use the module system and create fully class signature
    JavaClassType sig = (JavaClassType) identifierFactory.fromPath(filename);

    if (identifierFactory instanceof JavaModuleIdentifierFactory) {
      return ((JavaModuleIdentifierFactory) identifierFactory)
          .getClassType(
              sig.getClassName(), sig.getPackageName().getPackageName(), moduleDir.toString());
    }

    // if we are using the normal signature factory, than trim the module from the path
    return sig;
  }

  @Nonnull
  @Override
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    if (!isResolved) {
      discoverModules();
    }
    return Optional.ofNullable(moduleInfoMap.get(sig));
  }

  @Nonnull
  @Override
  public Set<ModuleSignature> getModules() {
    if (!isResolved) {
      discoverModules();
    }
    return Collections.unmodifiableSet(moduleInfoMap.keySet());
  }
}
