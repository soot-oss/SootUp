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

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Base class for {@link AnalysisInputLocation}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemAnalysisInputLocation implements BytecodeAnalysisInputLocation {

  private final FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));

  @Override
  public @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull ClassLoadingOptions classLoadingOptions) {
    JavaClassType klassType = (JavaClassType) classType;
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    if (klassType.getPackageName() instanceof ModulePackageName) {
      return this.getClassSourceInternalForModule(
          klassType, new AsmJavaClassProvider(bodyInterceptors));
    }
    return this.getClassSourceInternalForClassPath(
        klassType, new AsmJavaClassProvider(bodyInterceptors));
  }

  private @Nonnull Optional<AbstractClassSource<JavaSootClass>> getClassSourceInternalForClassPath(
      @Nonnull JavaClassType classSignature, @Nonnull ClassProvider<JavaSootClass> classProvider) {

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path moduleRoot = theFileSystem.getPath("modules");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          // check each module folder for the class
          Path foundfile = entry.resolve(filepath);
          if (Files.isRegularFile(foundfile)) {
            return Optional.of(classProvider.createClassSource(this, foundfile, classSignature));
          }
        }
      }
    } catch (IOException e) {
      throw new ResolveException("Error loading a module", moduleRoot, e);
    }

    return Optional.empty();
  }

  private @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>>
      getClassSourceInternalForModule(
          @Nonnull JavaClassType classSignature,
          @Nonnull ClassProvider<JavaSootClass> classProvider) {
    Preconditions.checkArgument(classSignature.getPackageName() instanceof ModulePackageName);

    ModulePackageName modulePackageSignature = (ModulePackageName) classSignature.getPackageName();

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path module =
        theFileSystem.getPath(
            "modules", modulePackageSignature.getModuleSignature().getModuleName());
    Path foundClass = module.resolve(filepath);

    if (Files.isRegularFile(foundClass)) {
      return Optional.of(classProvider.createClassSource(this, foundClass, classSignature));

    } else {
      return Optional.empty();
    }
  }

  // get the factory, which I should use the create the correspond class signatures
  @Override
  public @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();

    final Path archiveRoot = theFileSystem.getPath("modules");
    return walkDirectory(
        archiveRoot, identifierFactory, new AsmJavaClassProvider(bodyInterceptors));
  }

  protected @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassProvider<JavaSootClass> classProvider) {

    final FileType handledFileType = classProvider.getHandledFileType();
    try {
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(
                          classProvider.createClassSource(
                              this,
                              p,
                              this.fromPath(
                                  p.subpath(2, p.getNameCount()),
                                  p.subpath(1, 2),
                                  identifierFactory)))))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new ResolveException("Error loading a module", dirPath, e);
    }
  }

  /**
   * Discover and return all modules contained in the jrt filesystem.
   *
   * @return Collection of found module names.
   */
  public @Nonnull Collection<String> discoverModules() {
    final Path moduleRoot = theFileSystem.getPath("modules");
    List<String> foundModules = new ArrayList<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          if (Files.isDirectory(entry)) {
            foundModules.add(entry.subpath(1, 2).toString());
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return foundModules;
  }

  // TODO: originally, I could create a ModuleSingatre in any case, however, then
  // every signature factory needs a methodRef create from path
  // however, I cannot think of a general way for java 9 modules anyway....
  // how to create the module name if we have a jar file..., or a multi jar, or the jrt file system
  // nevertheless, one general methodRef for all signatures seems reasonable
  private @Nonnull JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    // else use the module system and create fully class signature
    JavaClassType sig = (JavaClassType) identifierFactory.fromPath(filename);

    if (identifierFactory instanceof JavaModuleIdentifierFactory) {
      // FIXME: adann clean this up!
      // String filename = FilenameUtils.removeExtension(file.toString()).replace('/', '.');
      // int index = filename.lastIndexOf('.');
      // Path parentDir = filename.subpath(0, 2);
      // Path packageFileName = parentDir.relativize(filename);
      // // get the package
      // String packagename = packageFileName.toString().replace('/', '.');
      // String classname = FilenameUtils.removeExtension(packageFileName.getFileName().toString());
      //

      return ((JavaModuleIdentifierFactory) identifierFactory)
          .getClassType(
              sig.getClassName(), sig.getPackageName().getPackageName(), moduleDir.toString());
    }

    // if we are using the normal signature factory, than trim the module from the path
    return sig;
  }
}
