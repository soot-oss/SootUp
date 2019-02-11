package de.upb.soot.namespaces;

import com.google.common.base.Preconditions;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
   *          The class path to search in The {@link IClassProvider} for generating {@link AbstractClassSource}es for the files found
   *          on the class path
   */
  public JavaModulePathNamespace(String modulePath, IClassProvider classProvider) {
    super(classProvider);
    this.moduleFinder = new ModuleFinder(classProvider, modulePath);
  }

  @Override
  public Collection<AbstractClassSource> getClassSources(SignatureFactory factory) {
    Preconditions.checkArgument(factory instanceof ModuleSignatureFactory, "Factory must be a ModuleSignatureFactory");

    Set<AbstractClassSource> found = new HashSet<>();
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
  public Optional<AbstractClassSource> getClassSource(JavaClassSignature signature) {

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

    final Optional<AbstractClassSource> classSource = ns.getClassSource(signature);
    return classSource;

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
    public JavaClassSignature getClassSignature(String className, String packageName) {
      return factory.getClassSignature(className, packageName);
    }

    @Override
    public JavaClassSignature getClassSignature(String fullyQualifiedClassName) {
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
    public MethodSignature getMethodSignature(String methodName, JavaClassSignature declaringClassSignature, String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    public JavaClassSignature fromPath(Path file) {
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
        return moduleSignatureFactory.getClassSignature(className, packageName, this.moduleName);
      }
      return factory.fromPath(file);
    }

    @Override
    public FieldSignature getFieldSignature(String fieldName, JavaClassSignature declaringClassSignature, String fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }
  }

}
