package de.upb.soot.signatures;

import com.google.common.base.Preconditions;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

/**
 * Factory to create valid signatures for Java classes in a modulepath.
 *
 * @author Andreas Dann
 */
public class ModuleSignatureFactory extends SignatureFactory {

  public static final ClassSignature MODULE_INFO_CLASS = new ClassSignature("module-info", PackageSignature.DEFAULT_PACKAGE);

  protected final Map<String, ModuleSignature> modules = new HashMap<>();

  protected ModuleSignatureFactory() {
    /*
     * Represents the unnamed module in Java's module system. Every type that is not defined in any known module but loaded
     * from the classpath is associated with this unnamed module, so as to ensure that every type is associated with a
     * module.
     *
     * <p>{@link ModuleSignature#UNNAMED_MODULE}
     */
    modules.put(ModuleSignature.UNNAMED_MODULE.moduleName, ModuleSignature.UNNAMED_MODULE);
  }

  /**
   * Returns a unique ModuleSignature. The method looks up a cache if it already contains a signature with the given module
   * name. If the cache lookup fails a new signature is created. Returns a unique ModuleSignature. The method looks up a
   * cache if it already contains a signature with the given module name. If the cache lookup fails a new signature is
   * created.
   *
   * @param moduleName
   *          the module name; Must not be null. Use the empty string for the unnamed module
   * @return a ModuleSignature
   * @throws NullPointerException
   *           if the given module name is null. Use the empty string to denote the unnamed module.
   */
  public ModuleSignature getModuleSignature(final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    ModuleSignature moduleSignature = modules.get(moduleName);
    if (moduleSignature == null) {
      moduleSignature = new ModuleSignature(moduleName);
      modules.put(moduleName, moduleSignature);
    }
    return moduleSignature;
  }

  @Override
  public ModulePackageSignature getPackageSignature(final String packageName) {
    return getPackageSignature(packageName, ModuleSignature.UNNAMED_MODULE.moduleName);
  }

  /**
   * Returns a unique PackageSignature. The method looks up a cache if it already contains a signature with the given package
   * and module name. If the cache lookup fails a new signature is created.
   *
   * @param packageName
   *          the package name; must not be null use empty string for the default package
   * @param moduleName
   *          the module containing the package; must not be null use empty string for the unnamed module
   *          {@link ModuleSignature#UNNAMED_MODULE}
   * @return a ModulePackageSignature
   * @throws NullPointerException
   *           if the given module name or package name is null. Use the empty string to denote the unnamed module or the
   *           default package.
   */
  public ModulePackageSignature getPackageSignature(final String packageName, final String moduleName) {
    Preconditions.checkNotNull(moduleName);
    Preconditions.checkNotNull(packageName);
    String fqId = moduleName + "." + packageName;
    ModulePackageSignature packageSignature = (ModulePackageSignature) packages.get(fqId);
    if (packageSignature == null) {
      ModuleSignature moduleSignature = getModuleSignature(moduleName);
      packageSignature = new ModulePackageSignature(packageName, moduleSignature);
      packages.put(fqId, packageSignature);
    }
    return packageSignature;
  }

  @Override
  public ClassSignature getClassSignature(final String className, final String packageName) {
    return getClassSignature(className, packageName, ModuleSignature.UNNAMED_MODULE.moduleName);
  }

  /**
   * Always creates a new ClassSignature. In opposite to PackageSignatures and ModuleSignatures, ClassSignatures are not
   * cached because the are unique per class, and thus reusing them does not make sense.
   *
   * @param className
   *          the simple name of the class
   * @param packageName
   *          the declaring package
   * @param moduleName
   *          the declaring module
   * @return a ClassSignature for a Java 9 class
   * @throws NullPointerException
   *           if the given module name or package name is null. Use the empty string to denote the unnamed module or the
   *           default package.
   */
  public ClassSignature getClassSignature(final String className, final String packageName, final String moduleName) {
    PackageSignature packageSignature = getPackageSignature(packageName, moduleName);
    return new ClassSignature(className, packageSignature);
  }

  // TODO: originally, I could create a ModuleSingatre in any case, however, then
  // every signature factory needs a method create from path
  // however, I cannot think of a general way for java 9 modules anyway....
  // how to create the module name if we have a jar file..., or a multi jar, or the jrt file system
  // nevertheless, one general method for all signatures seems reasonable
  // first part is the module, rest is package, than class
  public ClassSignature fromPath(final Path file, final Path parent) {

    String moduleName = parent.toString();

    String packageName = "";

    // check if the path contains a package
    int filePathLength = file.getNameCount();
    if (filePathLength > 1) {
      Path packageFileName = file.subpath(0, filePathLength - 1);

      // get the package
      packageName = packageFileName.toString().replace('/', '.');
    }
    // get the className
    String classname = FilenameUtils.removeExtension(file.getFileName().toString());
    return this.getClassSignature(classname, packageName, moduleName);

  }
}
