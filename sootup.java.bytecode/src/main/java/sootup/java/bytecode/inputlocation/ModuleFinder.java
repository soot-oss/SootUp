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
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.util.PathUtils;
import sootup.java.bytecode.frontend.AsmModuleSource;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.JavaSootClass;
import sootup.java.core.signatures.ModuleSignature;

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

  @Nonnull private final Map<ModuleSignature, JavaModuleInfo> moduleInfoMap = new HashMap<>();

  private int next = 0;

  @Nonnull private final List<Path> modulePathEntries;
  private SourceType sourceType = null; // FIXME !

  public boolean hasMoreToResolve() {
    return next < modulePathEntries.size();
  }

  /**
   * Helper Class to discover modules in a given module path.
   *
   * @param modulePath the module path
   */
  public ModuleFinder(@Nonnull String modulePath, @Nonnull FileSystem fileSystem) {
    this.modulePathEntries =
        JavaClassPathAnalysisInputLocation.explode(modulePath, fileSystem)
            .collect(Collectors.toList());
    for (Path modulePathEntry : modulePathEntries) {
      if (!Files.exists(modulePathEntry)) {
        throw new IllegalArgumentException(
            "'"
                + modulePathEntry
                + "' from modulePath '"
                + modulePath
                + "' does not exist in the filesystem.");
      }
    }
  }

  public ModuleFinder(@Nonnull String modulePath) {
    this(modulePath, FileSystems.getDefault());
  }

  @Nonnull
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    if (hasMoreToResolve()) {
      getAllModules();
    }
    return Optional.ofNullable(moduleInfoMap.get(sig));
  }

  @Nonnull
  public Set<ModuleSignature> getModules() {
    if (hasMoreToResolve()) {
      getAllModules();
    }
    return Collections.unmodifiableSet(moduleInfoMap.keySet());
  }

  /**
   * Returns the input location that manages the module.
   *
   * @param moduleName the module name
   * @return the input location that resolves classes contained in the module
   */
  @Nullable
  public AnalysisInputLocation<JavaSootClass> getModule(@Nonnull ModuleSignature moduleName) {

    // check if module is cached
    AnalysisInputLocation<JavaSootClass> inputLocationForModule =
        moduleInputLocation.get(moduleName);
    if (inputLocationForModule != null) {
      return inputLocationForModule;
    }

    // search iterative on the remaining entries of the modulePath for the module
    while (hasMoreToResolve()) {
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
  public Collection<ModuleSignature> getAllModules() {

    while (hasMoreToResolve()) {
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
      Path mi = path.resolve(JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class");
      if (Files.exists(mi)) {
        buildModuleForExplodedModule(path);
      }

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
        for (Path entry : stream) {
          try {
            attrs = Files.readAttributes(entry, BasicFileAttributes.class);
          } catch (NoSuchFileException ignore) {
            continue;
          }

          if (attrs.isDirectory()) {
            mi = entry.resolve(JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class");
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
        PathBasedAnalysisInputLocation.create(dir, sourceType);

    Path moduleInfoFile = dir.resolve(JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class");
    if (!Files.exists(moduleInfoFile) && !Files.isRegularFile(moduleInfoFile)) {
      return;
    }

    JavaModuleInfo moduleInfo = new AsmModuleSource(moduleInfoFile);
    JavaModuleInfo oldValue = moduleInfoMap.put(moduleInfo.getModuleSignature(), moduleInfo);
    moduleInputLocation.put(moduleInfo.getModuleSignature(), inputLocation);
    if (oldValue != null) {
      throw new IllegalStateException(
          moduleInfo.getModuleSignature().toString() + " has multiple occurences.");
    }
  }

  /**
   * Creates a module definition and the namespace for either a modular jar or an automatic module.
   *
   * @param jar the jar file
   */
  private void buildModuleForJar(@Nonnull Path jar) {
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(jar, sourceType);
    Path mi;
    try (FileSystem zipFileSystem = FileSystems.newFileSystem(jar, (ClassLoader) null)) {
      final Path archiveRoot = zipFileSystem.getPath("/");
      mi = archiveRoot.resolve(JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class");

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

      final String file = "META-INF/MANIFEST.MF";
      JarEntry entry = (JarEntry) jar.getEntry(file);
      if (entry != null) {
        Manifest manifest = new Manifest(jar.getInputStream(entry));
        Attributes attr = manifest.getMainAttributes();

        String automaticModuleName = attr.getValue("Automatic-Module-Name");
        if (automaticModuleName != null) {
          return automaticModuleName;
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
    Matcher matcher = Patterns.VERSION.matcher(moduleName);
    if (matcher.find()) {
      int start = matcher.start();
      moduleName = moduleName.substring(0, start);
    }
    moduleName = Patterns.ALPHA_NUM.matcher(moduleName).replaceAll(".");

    // remove all repeating dots
    moduleName = Patterns.REPEATING_DOTS.matcher(moduleName).replaceAll(".");

    // remove leading dots
    int len = moduleName.length();
    if (len > 0 && moduleName.charAt(0) == '.') {
      moduleName = Patterns.LEADING_DOTS.matcher(moduleName).replaceAll("");
    }

    // remove trailing dots
    len = moduleName.length();
    if (len > 0 && moduleName.charAt(len - 1) == '.') {
      moduleName = Patterns.TRAILING_DOTS.matcher(moduleName).replaceAll("");
    }

    return moduleName;
  }

  @Override
  public int hashCode() {
    return modulePathEntries.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ModuleFinder)) {
      return false;
    }
    return modulePathEntries.equals(((ModuleFinder) o).modulePathEntries);
  }

  /** Lazy-initialized cache of compiled patterns. */
  private static class Patterns {
    static final Pattern VERSION = Pattern.compile("-(\\d+(\\.|$))");
    static final Pattern ALPHA_NUM = Pattern.compile("[^A-Za-z0-9]");
    static final Pattern REPEATING_DOTS = Pattern.compile("(\\.)(\\1)+");
    static final Pattern LEADING_DOTS = Pattern.compile("^\\.");
    static final Pattern TRAILING_DOTS = Pattern.compile("\\.$");
  }
}
