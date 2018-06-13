package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 * 
 * @author Manuel Benz created on 22.05.18
 */
public class JavaModulePathNamespace extends JavaClassPathNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaModulePathNamespace.class);

  private BiMap<ModuleSignature, AbstractNamespace> moduleNamespace;

  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes based on the provided {@link IClassProvider}.
   *
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es for the files found on the class path
   * @param modulePath
   *          The class path to search in
   */
  public JavaModulePathNamespace(IClassProvider classProvider, String modulePath) {
    super(classProvider, modulePath);

    // add the namespace for the jrt virtual file system
    cpEntries.add(new JrtFSNamespace(classProvider));

    // todo only discover on demand...

    moduleNamespace = discoverModules();

  }

  private BiMap<ModuleSignature, AbstractNamespace> discoverModules() {
    Map<ModuleSignature, AbstractNamespace> map = new HashMap<>();

    // TODO, implement module discovery...
    // only discover is module unknown

    // iterate through all namespaces, and look for module-info file
    Optional<ClassSource> classSource;
    for (AbstractNamespace ns : cpEntries) {
      classSource = ns.getClassSource(ModuleSignatureFactory.MODULE_INFO_CLASS);
      if (classSource.isPresent()) {

      } else {
        // we have an automatic module?
      }
    }
    return null;

  }

  // IDEA: toPath Method wird teil der signature factory, und alle rufen einfach nurnoch topath auf...

  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {
    Preconditions.checkState(factory instanceof ModuleSignatureFactory, "Factory must be a ModuleSignatureFactory");

    SignatureFactory signatureFactoryWrapper = factory;
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept

    // TODO: problem is the classprovider creates classSignatures without module information
    Set<ClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {

      if (!(ns instanceof JrtFSNamespace)) {
        String moduleName = moduleNamespace.inverse().get(ns).moduleName;
        signatureFactoryWrapper = new SignatureFactoryWrapper(factory, moduleName);
      }

      found.addAll(ns.getClassSources(signatureFactoryWrapper));
    }
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
