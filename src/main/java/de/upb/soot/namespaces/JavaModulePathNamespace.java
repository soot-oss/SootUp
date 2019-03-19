package de.upb.soot.namespaces;

import com.google.common.base.Preconditions;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.ArrayTypeSignature;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.PackageSignature;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathNamespace extends AbstractNamespace {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaModulePathNamespace.class);

  private final ModuleFinder moduleFinder;

  public JavaModulePathNamespace(@Nonnull String modulePath) {
    this(modulePath, getDefaultClassProvider());
  }

  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes in the given module path.
   *
   * @param modulePath The class path to search in The {@link IClassProvider} for generating {@link
   *     ClassSource}es for the files found on the class path
   */
  public JavaModulePathNamespace(
      @Nonnull String modulePath, @Nonnull IClassProvider classProvider) {
    super(classProvider);
    this.moduleFinder = new ModuleFinder(classProvider, modulePath);
  }

  @Override
  public @Nonnull Collection<ClassSource> getClassSources(@Nonnull SignatureFactory factory) {
    Preconditions.checkArgument(
        factory instanceof ModuleSignatureFactory, "Factory must be a ModuleSignatureFactory");

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

      // FIXME: [JMP] `ns` may be `null`
      found.addAll(ns.getClassSources(signatureFactoryWrapper));
    }

    return found;
  }

  @Override
  public @Nonnull Optional<ClassSource> getClassSource(@Nonnull JavaClassSignature signature) {

    String modulename =
        ((ModulePackageSignature) signature.getPackageSignature())
            .getModuleSignature()
            .getModuleName();
    // lookup the ns for the class provider from the cache and use him...
    AbstractNamespace ns = moduleFinder.discoverModule(modulename);

    if (ns == null) {
      try {
        throw new ClassResolvingException("No Namespace for class " + signature);
      } catch (ClassResolvingException e) {
        e.printStackTrace();
        // FIXME: [JMP] Throwing exception and catching it immediately? This causes `ns` to remain
        // `null`.
      }
    }

    // FIXME: [JMP] `ns` may be `null`
    return ns.getClassSource(signature);
  }

  private class SignatureFactoryWrapper implements SignatureFactory {

    private final String moduleName;
    private final SignatureFactory factory;

    private SignatureFactoryWrapper(@Nonnull SignatureFactory factory, @Nonnull String moduleName) {
      this.factory = factory;
      this.moduleName = moduleName;
    }

    @Override
    public @Nonnull PackageSignature getPackageSignature(@Nonnull String packageName) {
      return factory.getPackageSignature(packageName);
    }

    @Override
    public @Nonnull JavaClassSignature getClassSignature(
        @Nonnull String className, @Nonnull String packageName) {
      return factory.getClassSignature(className, packageName);
    }

    @Override
    public @Nonnull JavaClassSignature getClassSignature(@Nonnull String fullyQualifiedClassName) {
      return factory.getClassSignature(fullyQualifiedClassName);
    }

    @Override
    public @Nonnull TypeSignature getTypeSignature(@Nonnull String typeName) {
      return factory.getTypeSignature(typeName);
    }

    @Override
    public @Nonnull Optional<PrimitiveTypeSignature> getPrimitiveTypeSignature(
        @Nonnull String typeName) {
      return factory.getPrimitiveTypeSignature(typeName);
    }

    @Override
    public @Nonnull ArrayTypeSignature getArrayTypeSignature(
        @Nonnull TypeSignature baseType, int dim) {
      return factory.getArrayTypeSignature(baseType, dim);
    }

    @Override
    public @Nonnull MethodSignature getMethodSignature(
        @Nonnull String methodName,
        @Nonnull String fullyQualifiedNameDeclClass,
        @Nonnull String fqReturnType,
        @Nonnull List<String> parameters) {
      return factory.getMethodSignature(
          methodName, fullyQualifiedNameDeclClass, fqReturnType, parameters);
    }

    @Override
    public MethodSignature getMethodSignature(
        @Nonnull String methodName,
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull String fqReturnType,
        @Nonnull List<String> parameters) {
      return factory.getMethodSignature(
          methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    public @Nonnull MethodSignature getMethodSignature(
        @Nonnull String methodName,
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull TypeSignature fqReturnType,
        @Nonnull List<TypeSignature> parameters) {
      return factory.getMethodSignature(
          methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Nonnull
    @Override
    public MethodSignature getMethodSignature(
        @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature) {
      return this.factory.getMethodSignature(declaringClass, subSignature);
    }

    @Nonnull
    @Override
    public MethodSignature getMethodSignature(
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull MethodSubSignature subSignature) {
      return this.factory.getMethodSignature(declaringClassSignature, subSignature);
    }

    @Override
    public @Nonnull MethodSignature parseMethodSignature(@Nonnull String methodSignature) {
      return factory.parseMethodSignature(methodSignature);
    }

    @Nonnull
    @Override
    public MethodSubSignature getMethodSubSignature(
        @Nonnull String name,
        @Nonnull Iterable<? extends TypeSignature> parameterSignatures,
        @Nonnull TypeSignature returnTypeSignature) {
      return this.factory.getMethodSubSignature(name, parameterSignatures, returnTypeSignature);
    }

    @Nonnull
    @Override
    public MethodSubSignature parseMethodSubSignature(@Nonnull String methodSubSignature) {
      return this.factory.parseMethodSubSignature(methodSubSignature);
    }

    @Override
    public @Nonnull FieldSignature parseFieldSignature(@Nonnull String fieldSignature) {
      return factory.parseFieldSignature(fieldSignature);
    }

    @Override
    public @Nonnull JavaClassSignature fromPath(@Nonnull Path file) {
      if (factory instanceof ModuleSignatureFactory) {
        ModuleSignatureFactory moduleSignatureFactory = (ModuleSignatureFactory) factory;
        String fullyQualifiedName =
            FilenameUtils.removeExtension(file.toString()).replace('/', '.');
        String packageName = "";
        int index = fullyQualifiedName.lastIndexOf(".");
        String className = fullyQualifiedName;
        if (index > 0) {
          className = fullyQualifiedName.substring(index);
          packageName = fullyQualifiedName.substring(0, index);
        }
        return moduleSignatureFactory.getClassSignature(className, packageName, this.moduleName);
      }
      return factory.fromPath(file);
    }

    @Override
    public @Nonnull FieldSignature getFieldSignature(
        @Nonnull String fieldName,
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull String fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Override
    public @Nonnull FieldSignature getFieldSignature(
        @Nonnull String fieldName,
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull TypeSignature fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Nonnull
    @Override
    public FieldSignature getFieldSignature(
        @Nonnull JavaClassSignature declaringClassSignature,
        @Nonnull FieldSubSignature subSignature) {
      return this.factory.getFieldSignature(declaringClassSignature, subSignature);
    }

    @Nonnull
    @Override
    public FieldSubSignature getFieldSubSignature(
        @Nonnull String name, @Nonnull TypeSignature typeSignature) {
      return this.factory.getFieldSubSignature(name, typeSignature);
    }

    @Nonnull
    @Override
    public FieldSubSignature parseFieldSubSignature(@Nonnull String subSignature) {
      return this.factory.parseFieldSubSignature(subSignature);
    }
  }
}
