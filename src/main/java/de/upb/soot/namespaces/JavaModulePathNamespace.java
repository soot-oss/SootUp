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

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *      href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathNamespace extends AbstractNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaModulePathNamespace.class);

  private final ModuleFinder moduleFinder;

  public JavaModulePathNamespace(String modulePath) {
    this(modulePath, getDefaultClassProvider());
  }

  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes in the given module path.
   *
   * @param modulePath
   *          The class path to search in The {@link IClassProvider} for generating {@link ClassSource}es for the files found
   *          on the class path
   */
  public JavaModulePathNamespace(String modulePath, IClassProvider classProvider) {
    super(classProvider);
    this.moduleFinder = new ModuleFinder(classProvider, modulePath);
  }

  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {
    Preconditions.checkArgument(factory instanceof ModuleSignatureFactory, "Factory must be a ModuleSignatureFactory");

    Set<ClassSource> found = new HashSet<>();
    Collection<String> availableModules = moduleFinder.discoverAllModules();
    for (String module : availableModules) {
      AbstractNamespace ns = moduleFinder.discoverModule(module);
      SignatureFactory signatureFactoryWrapper = factory;

      if (!(ns instanceof JrtFileSystemNamespace)) {
        /*
         * we need a wrapper to create correct signatures for the found classes, all other ignore modules by default, or have
         * no clue about modules.
         */
        signatureFactoryWrapper = new SignatureFactoryWrapper(factory, module);
      }

      found.addAll(ns.getClassSources(signatureFactoryWrapper));
    }

    return found;

  }

  @Override
  public Optional<ClassSource> getClassSource(ClassSignature signature) {

    String modulename = ((ModulePackageSignature) signature.packageSignature).moduleSignature.moduleName;
    // lookup the ns for the class provider from the cache and use him...
    AbstractNamespace ns = moduleFinder.discoverModule(modulename);

    if (ns == null) {
      try {
        throw new ClassResolvingException("No Namespace for class " + signature);
      } catch (ClassResolvingException e) {
        e.printStackTrace();
      }
    }

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
