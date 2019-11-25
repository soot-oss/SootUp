package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassResolvingException;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.java.bytecode.frontend.modules.AsmModuleClassSource;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.types.ModuleDecoratorClassType;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private @Nonnull ClassProvider classProvider;
  // associate a module name with the input location, that represents the module
  private @Nonnull Map<String, AnalysisInputLocation> moduleInputLocation = new HashMap<>();
  private int next = 0;

  private @Nonnull List<Path> modulePathEntries;

  private @Nonnull JrtFileSystemAnalysisInputLocation jrtFileSystemNamespace;

  /**
   * Helper Class to discover modules in a given module path.
   *
   * @param classProvider the class provider for resolving found classes
   * @param modulePath the module path
   */
  public ModuleFinder(@Nonnull ClassProvider classProvider, @Nonnull String modulePath) {
    this.classProvider = classProvider;
    this.modulePathEntries =
        JavaClassPathAnalysisInputLocation.explode(modulePath).collect(Collectors.toList());
    // add the input location for the jrt virtual file system
    // FIXME: Set Jrt File input location by default?
    jrtFileSystemNamespace = new JrtFileSystemAnalysisInputLocation();

    // discover all system's modules
    Collection<String> modules = jrtFileSystemNamespace.discoverModules();
    modules.forEach(m -> moduleInputLocation.put(m, jrtFileSystemNamespace));

    // the rest of the modules are discovered on demand...
  }

  /**
   * Returns the input location that manages the module.
   *
   * @param moduleName the module name
   * @return the input location that resolves classes contained in the module
   */
  public @Nullable AnalysisInputLocation discoverModule(@Nonnull String moduleName) {
    AnalysisInputLocation inputLocationForModule = moduleInputLocation.get(moduleName);
    if (inputLocationForModule != null) {
      return inputLocationForModule;
    }
    while (modulePathHasNextEntry()) {
      Path path = modulePathEntries.get(next);
      discoverModulesIn(path);
      next++;
      inputLocationForModule = moduleInputLocation.get(moduleName);
      if (inputLocationForModule != null) {
        return inputLocationForModule;
      }
    }
    return null;
  }

  private boolean modulePathHasNextEntry() {
    return this.next < this.modulePathEntries.size();
  }

  /**
   * Discover all modules in the module path.
   *
   * @return the names of all modules found
   */
  public @Nonnull Collection<String> discoverAllModules() {

    while (modulePathHasNextEntry()) {
      Path path = modulePathEntries.get(next);
      discoverModulesIn(path);
      next++;
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
    BasicFileAttributes attrs = null;
    try {
      attrs = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: Exception handling.
    }

    if (PathUtils.isArchive(path)) {
      buildModuleForJar(path);
    } else if (attrs.isDirectory()) { // FIXME: [JMP] `attrs` may be `null`

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
        for (Path entry : stream) {
          try {
            attrs = Files.readAttributes(entry, BasicFileAttributes.class);
          } catch (NoSuchFileException ignore) {
            continue;
          }

          if (attrs.isDirectory()) {
            Path moduleInfoFile =
                ModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(
                    classProvider.getHandledFileType());
            Path mi = entry.resolve(moduleInfoFile);
            if (Files.exists(mi)) {
              buildModuleForExplodedModule(entry);
            }
          } else if (PathUtils.isArchive(entry)) {
            buildModuleForJar(entry);
          }
        }
      } catch (IOException | ClassResolvingException e) {
        e.printStackTrace();
      }
    }
  }

  private void buildModuleForExplodedModule(@Nonnull Path dir) throws ClassResolvingException {
    // create the input location for this module dir
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.createForClassContainer(dir);

    Path moduleInfoFile =
        dir.resolve(
            ModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType()));
    if (!Files.exists(moduleInfoFile) && !Files.isRegularFile(moduleInfoFile)) {
      return;
    }
    // get the module's name out of this module-info file
    Optional<? extends AbstractClassSource> moduleInfoClassSource =
        inputLocation.getClassSource(ModuleIdentifierFactory.MODULE_INFO_CLASS);
    if (moduleInfoClassSource.isPresent()) {
      AbstractClassSource moduleInfoSource = moduleInfoClassSource.get();
      // get the module name
      String moduleName = this.getModuleName(moduleInfoSource);
      this.moduleInputLocation.put(moduleName, inputLocation);
    }
  }

  /**
   * Creates a module definition and the namesapce for either a modular jar or an automatic module.
   *
   * @param jar the jar file
   */
  private void buildModuleForJar(@Nonnull Path jar) {
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);
    Optional<? extends AbstractClassSource> moduleInfoFile = Optional.empty();
    try (FileSystem zipFileSystem = FileSystems.newFileSystem(jar, null)) {
      final Path archiveRoot = zipFileSystem.getPath("/");
      Path mi =
          archiveRoot.resolve(
              ModuleIdentifierFactory.MODULE_INFO_CLASS.toPath(
                  classProvider.getHandledFileType(), zipFileSystem));
      if (Files.exists(mi)) {

        // we have a modular jar
        // get the module name
        // create proper moduleInfoSignature
        moduleInfoFile = inputLocation.getClassSource(ModuleIdentifierFactory.MODULE_INFO_CLASS);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (moduleInfoFile.isPresent()) {
      AbstractClassSource moduleInfoSource = moduleInfoFile.get();
      // get the module name
      String moduleName = null;
      try {
        moduleName = getModuleName(moduleInfoSource);
      } catch (ClassResolvingException classResolvingException) {
        classResolvingException.printStackTrace();
      }

      this.moduleInputLocation.put(moduleName, inputLocation);
    } else {
      // no module-info treat as automatic module
      // create module name from the jar file
      String filename = jar.getFileName().toString();

      // make module base on the filename of the jar
      String moduleName = createModuleNameForAutomaticModule(filename);
      this.moduleInputLocation.put(moduleName, inputLocation);
    }
  }

  // FIXME: quickly parse the module name
  private @Nonnull String parseModuleInfoClassFile(@Nonnull AbstractClassSource moduleInfo) {
    if (moduleInfo instanceof AsmModuleClassSource) {
      return ((AsmModuleClassSource) moduleInfo).getModuleName();
    }
    return "";
  }

  private @Nonnull String getModuleName(@Nonnull AbstractClassSource moduleInfoSource)
      throws ClassResolvingException {
    // FIXME: somehow in need the module name from the source code ...
    // AbstractClass moduleInfoClass = this.classProvider.reify(moduleInfoSource);
    AbstractClassSource moduleInfoClass = moduleInfoSource;
    if (!(moduleInfoClass instanceof AsmModuleClassSource)) {
      throw new ClassResolvingException(
          "Class is named module-info but does not reify to SootModuleInfo");
    }
    // FIXME: here is no view or anything to resolve the content...??? Why do I need a view anyway?

    String moduleName = parseModuleInfoClassFile(moduleInfoClass);
    createProperModuleSignature(moduleInfoSource, moduleName);

    return moduleName;
  }

  private void createProperModuleSignature(
      AbstractClassSource moduleInfoSource, String moduleName) {
    // create proper moduleInfoSignature
    // add the module name, which was unknown before
    // moduleInfoSource.setClassSignature();
    ModuleSignature moduleSignature = ModuleIdentifierFactory.getModuleSignature(moduleName);
    JavaClassType sig =
        new ModuleDecoratorClassType(ModuleIdentifierFactory.MODULE_INFO_CLASS, moduleSignature);
    moduleInfoSource.setClassSignature(sig);
  }

  /**
   * Creates a name for an automatic module based on the name of a jar file. The implementation is
   * consistent with parsing module names in the JDK 9.
   *
   * @param filename the name of the jar file
   * @return the name of the automatic module
   */
  private @Nonnull String createModuleNameForAutomaticModule(@Nonnull String filename) {
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
