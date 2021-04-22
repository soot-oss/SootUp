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
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.frontend.AsmModuleSource;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Discovers all modules in a given module path. For automatic modules, names are generated.
 * Supports exploded modules, modular jars, and automatic modules as defined in the official
 * documentation:
 *
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 * @author Andreas Dann on 28.06.18
 */
public class ModuleFinder {

  // associate a module name with the input location, that represents the module
  @Nonnull
  private final Map<ModuleSignature, AnalysisInputLocation<JavaSootClass>> moduleInputLocation =
      new HashMap<>();

  Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();

  private int next = 0;

  @Nonnull private final List<Path> modulePathEntries;

  @Nonnull
  private final AsmJavaClassProvider classProvider =
      new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors());

  public boolean isFullyResolved() {
    return next == modulePathEntries.size();
  }

  /**
   * Helper Class to discover modules in a given module path.
   *
   * @param modulePath the module path
   */
  public ModuleFinder(@Nonnull String modulePath) {
    this.modulePathEntries =
        JavaClassPathAnalysisInputLocation.explode(modulePath).collect(Collectors.toList());
  }

  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    if (!isFullyResolved()) {
      discoverAllModules();
    }
    return Optional.ofNullable(moduleInfoMap.get(sig));
  }

  /**
   * Returns the input location that manages the module.
   *
   * @param moduleName the module name
   * @return the input location that resolves classes contained in the module
   */
  @Nullable
  public AnalysisInputLocation<JavaSootClass> discoverModule(@Nonnull ModuleSignature moduleName) {

    // discover all system's modules if they are not loaded already as they are always existing in
    // java
    if (moduleInputLocation.isEmpty()) {
      JrtFileSystemAnalysisInputLocation jrtFileSystemNamespace =
          new JrtFileSystemAnalysisInputLocation();
      jrtFileSystemNamespace
          .discoverModules()
          .forEach(m -> moduleInputLocation.put(m, jrtFileSystemNamespace));
    }

    // check if module is cached
    AnalysisInputLocation<JavaSootClass> inputLocationForModule =
        moduleInputLocation.get(moduleName);
    if (inputLocationForModule != null) {
      return inputLocationForModule;
    }

    // search iterative on the remaining entries of the modulePath for the module
    while (next < modulePathEntries.size()) {
      discoverModulesIn(modulePathEntries.get(next++));
      inputLocationForModule = moduleInputLocation.get(moduleName);
      if (inputLocationForModule != null) {
        return inputLocationForModule;
      }
    }
    return null;
  }

  /**
   * Discover all modules in the module path.
   *
   * @return the names of all modules found
   */
  @Nonnull
  public Collection<ModuleSignature> discoverAllModules() {

    while (next < modulePathEntries.size()) {
      discoverModulesIn(modulePathEntries.get(next++));
    }
    return Collections.unmodifiableCollection(moduleInputLocation.keySet());
  }

  // TODO: in general it makes sense to traverse the directories further and associate packages with
  // a module
  // this is, for instance, done in the JDK
  /**
   * Searches in a directory for module definitions currently only one level of hierarchy is
   * traversed.
   *
   * @param path the directory
   */
  private void discoverModulesIn(@Nonnull Path path) {
    BasicFileAttributes attrs;
    try {
      attrs = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      throw new ResolveException("Error while discovering modules", path, e);
    }

    if (PathUtils.isArchive(path)) {
      buildModuleForJar(path);
    } else if (attrs.isDirectory()) {

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
        for (Path entry : stream) {
          try {
            attrs = Files.readAttributes(entry, BasicFileAttributes.class);
          } catch (NoSuchFileException ignore) {
            continue;
          }

          if (attrs.isDirectory()) {
            Path moduleInfoFile =
                JavaModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(
                    classProvider.getHandledFileType());
            Path mi = entry.resolve(moduleInfoFile);
            if (Files.exists(mi)) {
              buildModuleForExplodedModule(entry);
            }
          } else if (PathUtils.isArchive(entry)) {
            buildModuleForJar(entry);
          }
        }
      } catch (Exception e) {
        throw new ResolveException("Error while discovering modules", path, e);
      }
    }
  }

  private void buildModuleForExplodedModule(@Nonnull Path dir) throws ResolveException {
    // create the input location for this module dir
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.createForClassContainer(dir);

    Path moduleInfoFile =
        dir.resolve(
            JavaModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(
                classProvider.getHandledFileType()));
    if (!Files.exists(moduleInfoFile) && !Files.isRegularFile(moduleInfoFile)) {
      return;
    }

    JavaModuleInfo moduleInfo = new AsmModuleSource(moduleInfoFile);
    moduleInfoMap.put(moduleInfo.getModuleSignature(), moduleInfo);
    moduleInputLocation.put(moduleInfo.getModuleSignature(), inputLocation);
  }

  /**
   * Creates a module definition and the namespace for either a modular jar or an automatic module.
   *
   * @param jar the jar file
   */
  private void buildModuleForJar(@Nonnull Path jar) {
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);
    Path mi;
    try (FileSystem zipFileSystem = FileSystems.newFileSystem(jar, null)) {
      final Path archiveRoot = zipFileSystem.getPath("/");
      mi =
          archiveRoot.resolve(
              JavaModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(
                  classProvider.getHandledFileType(), zipFileSystem));

      if (Files.exists(mi)) {
        JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
        moduleInfoMap.put(moduleInfo.getModuleSignature(), moduleInfo);
        moduleInputLocation.put(moduleInfo.getModuleSignature(), inputLocation);
      } else {
        // no module-info: its an automatic module i.e. create module name from the jar file
        ModuleSignature moduleSignature =
            JavaModuleIdentifierFactory.getModuleSignature(createModuleNameForAutomaticModule(jar));
        moduleInputLocation.put(moduleSignature, inputLocation);
        moduleInfoMap.put(
            moduleSignature, JavaModuleInfo.createAutomaticModuleInfo(moduleSignature));
      }

    } catch (IOException e) {
      throw new ResolveException("Error resolving module descriptor in a Jar", jar, e);
    }
  }

  /**
   * Creates a name for an automatic module based on the name of a jar file. The implementation is
   * consistent with parsing module names in the JDK 9.
   *
   * @param path to the jar file
   * @return the name of the automatic module
   */
  @Nonnull
  public static String createModuleNameForAutomaticModule(@Nonnull Path path) {
    // check if Automatic-Module-Name header exists in manifest file and use it if exists
    try {
      JarFile jar = new JarFile(path.toFile());

      String file = "META-INF/MANIFEST.MF";
      JarEntry entry = (JarEntry) jar.getEntry(file);
      if (entry != null) {
        Manifest manifest = new Manifest(jar.getInputStream(entry));
        Attributes attr = manifest.getMainAttributes();

        String amn = attr.getValue("Automatic-Module-Name");
        if (amn != null) {
          return amn;
        }
      }
    } catch (IOException ignored) {
    }

    String filename = path.getFileName().toString();

    int i = filename.lastIndexOf(File.separator);
    if (i != -1) {
      filename = filename.substring(i + 1);
    }

    // drop teh file extension .jar
    String moduleName = filename.substring(0, filename.length() - 4);

    // find first occurrence of -${NUMBER}. or -${NUMBER}$
    // according to the java 9 spec and current implementation, version numbers are ignored when
    // naming automatic modules
    Matcher matcher = Pattern.compile("-(\\d+(\\.|$))").matcher(moduleName);
    if (matcher.find()) {
      int start = matcher.start();
      moduleName = moduleName.substring(0, start);
    }
    moduleName = Pattern.compile("[^A-Za-z0-9]").matcher(moduleName).replaceAll(".");

    // remove all repeating dots
    moduleName = Pattern.compile("(\\.)(\\1)+").matcher(moduleName).replaceAll(".");

    // remove leading dots
    int len = moduleName.length();
    if (len > 0 && moduleName.charAt(0) == '.') {
      moduleName = Pattern.compile("^\\.").matcher(moduleName).replaceAll("");
    }

    // remove trailing dots
    len = moduleName.length();
    if (len > 0 && moduleName.charAt(len - 1) == '.') {
      moduleName = Pattern.compile("\\.$").matcher(moduleName).replaceAll("");
    }

    return moduleName;
  }
}
