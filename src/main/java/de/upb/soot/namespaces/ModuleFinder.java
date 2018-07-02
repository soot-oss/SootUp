package de.upb.soot.namespaces;

import de.upb.soot.core.SootModule;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ModuleSignatureFactory;

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

/**
 * Discovers all modules in a given module path. For automatic modules corresponding names are generated. Handles exploded
 * modules, modular jars, and automatic modules as stated in the official documentation:
 * 
 * @see <a
 *      href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 *
 * @author Andreas Dann on 28.06.18
 */
class ModuleFinder {
  private IClassProvider classProvider;
  // associate a module name with the namespace, that represents the module
  private Map<String, AbstractNamespace> moduleNamespace = new HashMap<>();
  private int next = 0;

  private List<Path> modulePathEntries;

  private JrtFileSystemNamespace jrtFileSystemNamespace;

  public ModuleFinder(IClassProvider classProvider, String modulePath) {
    this.classProvider = classProvider;
    this.modulePathEntries = JavaClassPathNamespace.explode(modulePath).collect(Collectors.toList());
    // add the namespace for the jrt virtual file system
    // FIXME: check if it makes sense to make this static
    jrtFileSystemNamespace = new JrtFileSystemNamespace(classProvider);

    // discover all system's modules
    Collection<String> modules = jrtFileSystemNamespace.discoverModules();
    modules.forEach(m -> moduleNamespace.put(m, jrtFileSystemNamespace));

    // the rest of the modules are discovered on demand...
  }

  public AbstractNamespace discoverModule(String moduleName) {
    AbstractNamespace namespaceForModule = moduleNamespace.get(moduleName);
    if (namespaceForModule != null) {
      return namespaceForModule;
    }
    while (modulePathHasNextEntry()) {
      Path path = modulePathEntries.get(next);
      discoverModulesIn(path);
      next++;
      namespaceForModule = moduleNamespace.get(moduleName);
      if (namespaceForModule != null) {
        return namespaceForModule;
      }
    }
    return null;
  }

  private boolean modulePathHasNextEntry() {
    return this.next < this.modulePathEntries.size();
  }

  /**
   * Discover all modules in the modulepath.
   * 
   * @return the names of all modules found
   */
  public Collection<String> discoverAllModules() {

    while (modulePathHasNextEntry()) {
      Path path = modulePathEntries.get(next);
      discoverModulesIn(path);
      next++;
    }
    return Collections.unmodifiableCollection(moduleNamespace.keySet());

  }

  // TODO: in general it makes sense to traverse the directories further and associate packages with a module
  // this is, for instance, done in the JDK
  /**
   * Searches in a directory for module definitions currently only one level of hierarchy is traversed.
   *
   * @param path
   *          the directory
   * @return the found modules and their classes
   */
  private void discoverModulesIn(Path path) {
    Map<String, List<String>> mapModuleClasses = new HashMap<>();

    BasicFileAttributes attrs = null;
    try {
      attrs = Files.readAttributes(path, BasicFileAttributes.class);
    } catch (IOException e) {
      e.printStackTrace();
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
            Path moduleInfoFile = ModuleSignatureFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType());
            Path mi = entry.resolve(moduleInfoFile);
            if (Files.exists(mi)) {
              buildModuleForExplodedModule(entry);
            }
          } else if (PathUtils.isArchive(entry)) {
            buildModuleForJar(entry);
          }

        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }

  private void buildModuleForExplodedModule(Path dir) {
    // create the namespace for this module dir
    PathBasedNamespace namespace = PathBasedNamespace.createForClassContainer(this.classProvider, dir);

    Path moduleInfoFile = dir.resolve(ModuleSignatureFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType()));
    if (!Files.exists(moduleInfoFile) && !Files.isRegularFile(moduleInfoFile)) {
      return;
    }
    // get the module's name out of this module-info file
    Optional<ClassSource> moduleInfoClass = namespace.getClassSource(ModuleSignatureFactory.MODULE_INFO_CLASS);
    if (moduleInfoClass.isPresent()) {
      ClassSource moduleInfoSource = moduleInfoClass.get();
      // get the module name
      String moduleName = new SootModule(moduleInfoSource).getName();

      this.moduleNamespace.put(moduleName, namespace);

    }

  }

  /**
   * Creates a module definition and the namesapce for either a modular jar or an automatic module.
   *
   * @param jar
   *          the jar file
   */
  private void buildModuleForJar(Path jar) {
    PathBasedNamespace namespace = PathBasedNamespace.createForClassContainer(this.classProvider, jar);

    try (FileSystem zipFileSystem = FileSystems.newFileSystem(jar, null)) {
      final Path archiveRoot = zipFileSystem.getPath("/");
      Path mi = archiveRoot
          .resolve(ModuleSignatureFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType(), zipFileSystem));
      if (Files.exists(mi)) {

        // we have a modular jar
        // get the module name

        Optional<ClassSource> moduleInfoClass = namespace.getClassSource(ModuleSignatureFactory.MODULE_INFO_CLASS);
        if (moduleInfoClass.isPresent()) {
          ClassSource moduleInfoSource = moduleInfoClass.get();
          // get the module name
          String moduleName = new SootModule(moduleInfoSource).getName();

          this.moduleNamespace.put(moduleName, namespace);

        }

      } else {
        // no module-info treat as automatic module
        // create module name from the jar file
        String filename = jar.getFileName().toString();

        // make module base on the filename of the jar
        String moduleName = createModuleNameForAutomaticModule(filename);
        this.moduleNamespace.put(moduleName, namespace);

      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Creates a name for an automatic module based on the name of a jar file. The implementation is consistent with parsing
   * module names in the JDK 9{@link ModulePathFinder}.
   *
   * @param filename
   *          the name of the jar file
   * @return the name of the automatic module
   */
  private String createModuleNameForAutomaticModule(String filename) {
    int i = filename.lastIndexOf(File.separator);
    if (i != -1) {
      filename = filename.substring(i + 1);
    }

    // drop teh file extension .jar
    String moduleName = filename.substring(0, filename.length() - 4);

    // find first occurrence of -${NUMBER}. or -${NUMBER}$
    // according to the java 9 spec and current implementation, version numbers are ignored when naming automatic modules
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
