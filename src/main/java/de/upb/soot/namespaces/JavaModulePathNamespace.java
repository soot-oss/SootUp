package de.upb.soot.namespaces;

import com.google.common.base.Preconditions;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 * 
 * @author Andreas Dann created on 28.05.18
 */
public class JavaModulePathNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaModulePathNamespace.class);
  private List<Path> modulePathEntries;
  // associate a module name with the namespace, that represents the module
  private Map<String, AbstractNamespace> moduleNamespace = new HashMap<>();
  private int next = 0;

  private JrtFileSystemNamespace jrtFileSystemNamespace;

  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes based on the provided {@link IClassProvider}.
   *
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es for the files found on the class path
   * @param modulePath
   *          The class path to search in
   */
  public JavaModulePathNamespace(IClassProvider classProvider, String modulePath) {
    super(classProvider);
    this.modulePathEntries = PathUtils.explode(modulePath).collect(Collectors.toList());

    // add the namespace for the jrt virtual file system
    jrtFileSystemNamespace = new JrtFileSystemNamespace(classProvider);
    // discover all system's modules
    Collection<String> modules = jrtFileSystemNamespace.discoverModules();
    modules.forEach(m -> moduleNamespace.put(m, jrtFileSystemNamespace));

    // the rest of the modules are discovered on demand...

  }

  private INamespace discoverModule(String moduleName) {
    INamespace namespaceForModule = moduleNamespace.get(moduleName);
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

  private void discoverAllModules() {

    while (modulePathHasNextEntry()) {
      Path path = modulePathEntries.get(next);
      discoverModulesIn(path);
      next++;
    }

  }

  // TODO: in general it make sense to traverse the directories further and associate packages with a module
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

    if (attrs.isRegularFile() && PathUtils.hasExtension(path, FileType.JAR, FileType.ZIP)) {
      buildModuleForJar(path);
    }

    else if (attrs.isDirectory()) {

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
          } else if (attrs.isRegularFile() && PathUtils.hasExtension(path, FileType.JAR, FileType.ZIP)) {
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

    // get the module name
    String moduleName = "";

    Path moduleInfoFile = dir.resolve(ModuleSignatureFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType()));

    // TODO: get the module's name out of this module-info file (by parsing it?)

    this.moduleNamespace.put(moduleName, namespace);

  }

  /**
   * Creates a module definition and the namesapce for either a modular jar or an automatic module
   *
   * @param jar
   *          the jar file
   */
  private void buildModuleForJar(Path jar) {
    PathBasedNamespace namespace = PathBasedNamespace.createForClassContainer(this.classProvider, jar);

    try (FileSystem zipFileSystem = FileSystems.newFileSystem(jar, null)) {
      final Path archiveRoot = zipFileSystem.getPath("/");
      Path mi = archiveRoot.resolve(ModuleSignatureFactory.MODULE_INFO_CLASS.toPath(classProvider.getHandledFileType()));
      if (Files.exists(mi)) {

        // we have a modular jar
        // get the module name
        String moduleName = "";

        // TODO: get the module's name out of this module-info file (by parsing it?)

        this.moduleNamespace.put(moduleName, namespace);

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
   * Creates a name for an automatic module based on the name of a jar file this is based on the jdk parsing of module name
   * in the JDK 9{@link ModulePathFinder} at least the patterns are the same
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

  // TODO: Do we want class sources for all entries, or all classes under a certian module?
  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {
    Preconditions.checkState(factory instanceof ModuleSignatureFactory, "Factory must be a ModuleSignatureFactory");

    // TODO: problem is the classprovider creates classSignatures without module information
    Set<ClassSource> found = new HashSet<>();
    for (Map.Entry<String, AbstractNamespace> entry : moduleNamespace.entrySet()) {
      AbstractNamespace ns = entry.getValue();
      if (ns instanceof JrtFileSystemNamespace) {
        continue;
      }
      String moduleName = entry.getKey();

      SignatureFactory signatureFactoryWrapper = new SignatureFactoryWrapper(factory, moduleName);
      found.addAll(ns.getClassSources(signatureFactoryWrapper));

    }

    // add the end add the system libraries
    found.addAll(jrtFileSystemNamespace.getClassSources(factory));

    return found;

  }

  @Override
  public Optional<ClassSource> getClassSource(ClassSignature signature) {

    // here, we don't have any problems wrt. the classSignature, since the classprovider use the given signature
    // which is a module signature

    // take the ns for the class provider from the cache and use him...
    AbstractNamespace ns = moduleNamespace.get(((ModulePackageSignature) signature.packageSignature).moduleSignature);

    final Optional<ClassSource> classSource = ns.getClassSource(signature);
    if (classSource.isPresent()) {
      return classSource;
    }

    return Optional.empty();
  }

  private class SignatureFactoryWrapper implements SignatureFactory {

    private final String moduleName;
    private final SignatureFactory factory;

    private SignatureFactoryWrapper(SignatureFactory factory, String moduleName) {
      this.factory = factory;
      this.moduleName = moduleName;
    }

    @Override
    public PackageSignature getPackageSignature(String packageName) {
      return factory.getPackageSignature(packageName);
    }

    @Override
    public ClassSignature getClassSignature(String className, String packageName) {
      return factory.getClassSignature(className, packageName);
    }

    @Override
    public ClassSignature getClassSignature(String fullyQualifiedClassName) {
      return factory.getClassSignature(fullyQualifiedClassName);
    }

    @Override
    public TypeSignature getTypeSignature(String typeName) {
      return factory.getTypeSignature(typeName);
    }

    @Override
    public MethodSignature getMethodSignature(String methodName, String fullyQualifiedNameDeclClass, String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(methodName, fullyQualifiedNameDeclClass, fqReturnType, parameters);
    }

    @Override
    public MethodSignature getMethodSignature(String methodName, ClassSignature declaringClassSignature, String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    public ClassSignature fromPath(Path file) {
      if (factory instanceof ModuleSignatureFactory) {
        ModuleSignatureFactory moduleSignatureFactory = (ModuleSignatureFactory) factory;
        String fullyQualifiedName = FilenameUtils.removeExtension(file.toString()).replace('/', '.');
        String packageName = "";
        int index = fullyQualifiedName.lastIndexOf(".");
        String className = fullyQualifiedName;
        if (index > 0) {
          className = fullyQualifiedName.substring(index, fullyQualifiedName.length());
          packageName = fullyQualifiedName.substring(0, index);
        }
        ClassSignature signature = moduleSignatureFactory.getClassSignature(className, packageName, this.moduleName);
        return signature;
      }
      return factory.fromPath(file);
    }
  }

}
