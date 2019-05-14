package de.upb.soot.namespaces;

import com.google.common.base.Preconditions;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.ModuleIdentifierFactory;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.signatures.ModulePackageName;
import de.upb.soot.signatures.PackageName;
import de.upb.soot.types.ArrayType;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
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
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    Preconditions.checkArgument(
        identifierFactory instanceof ModuleIdentifierFactory,
        "Factory must be a ModuleSignatureFactory");

    Set<AbstractClassSource> found = new HashSet<>();
    Collection<String> availableModules = moduleFinder.discoverAllModules();
    for (String module : availableModules) {
      AbstractNamespace ns = moduleFinder.discoverModule(module);
      IdentifierFactory identifierFactoryWrapper = identifierFactory;

      if (!(ns instanceof JrtFileSystemNamespace)) {
        /*
         * we need a wrapper to create correct types for the found classes, all other ignore modules by default, or have
         * no clue about modules.
         */
        identifierFactoryWrapper = new IdentifierFactoryWrapper(identifierFactoryWrapper, module);
      }

      // FIXME: [JMP] `ns` may be `null`
      found.addAll(ns.getClassSources(identifierFactoryWrapper));
    }

    return found;
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {

    String modulename =
        ((ModulePackageName) signature.getPackageName()).getModuleSignature().getModuleName();
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

  private static class IdentifierFactoryWrapper implements IdentifierFactory {

    private final IdentifierFactory factory;
    private final String moduleName;

    private IdentifierFactoryWrapper(IdentifierFactory factory, String moduleName) {
      this.factory = factory;
      this.moduleName = moduleName;
    }

    @Override
    public @Nonnull JavaClassType getClassType(
        @Nonnull String className, @Nonnull String packageName) {
      return factory.getClassType(className, packageName);
    }

    @Override
    public @Nonnull JavaClassType getClassType(@Nonnull String fullyQualifiedClassName) {
      return factory.getClassType(fullyQualifiedClassName);
    }

    @Override
    public @Nonnull Type getType(@Nonnull String typeName) {
      return factory.getType(typeName);
    }

    @Override
    public @Nonnull Optional<PrimitiveType> getPrimitiveType(@Nonnull String typeName) {
      return factory.getPrimitiveType(typeName);
    }

    @Override
    public @Nonnull ArrayType getArrayType(@Nonnull Type baseType, int dim) {
      return factory.getArrayType(baseType, dim);
    }

    @Override
    public @Nonnull JavaClassType fromPath(@Nonnull Path file) {
      if (factory instanceof ModuleIdentifierFactory) {
        ModuleIdentifierFactory moduleSignatureFactory = (ModuleIdentifierFactory) factory;
        String fullyQualifiedName =
            FilenameUtils.removeExtension(file.toString()).replace('/', '.');
        String packageName = "";
        int index = fullyQualifiedName.lastIndexOf(".");
        String className = fullyQualifiedName;
        if (index > 0) {
          className = fullyQualifiedName.substring(index);
          packageName = fullyQualifiedName.substring(0, index);
        }
        return moduleSignatureFactory.getClassType(className, packageName, this.moduleName);
      }
      return factory.fromPath(file);
    }

    @Override
    public PackageName getPackageName(String packageName) {
      return factory.getPackageName(packageName);
    }

    @Override
    public MethodSignature getMethodSignature(
        String methodName,
        String fullyQualifiedNameDeclClass,
        String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(
          methodName, fullyQualifiedNameDeclClass, fqReturnType, parameters);
    }

    @Override
    public MethodSignature getMethodSignature(
        String methodName,
        JavaClassType declaringClassSignature,
        String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(
          methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    public MethodSignature getMethodSignature(
        String methodName,
        JavaClassType declaringClassSignature,
        Type fqReturnType,
        List<Type> parameters) {
      return factory.getMethodSignature(
          methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    @Nonnull
    public MethodSignature getMethodSignature(
        @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature) {
      return factory.getMethodSignature(declaringClass, subSignature);
    }

    @Override
    @Nonnull
    public MethodSignature getMethodSignature(
        @Nonnull JavaClassType declaringClassSignature, @Nonnull MethodSubSignature subSignature) {
      return factory.getMethodSignature(declaringClassSignature, subSignature);
    }

    @Override
    @Nonnull
    public MethodSignature parseMethodSignature(@Nonnull String methodSignature) {
      return factory.parseMethodSignature(methodSignature);
    }

    @Override
    @Nonnull
    public MethodSubSignature getMethodSubSignature(
        @Nonnull String name,
        @Nonnull Iterable<? extends Type> parameterSignatures,
        @Nonnull Type returnType) {
      return factory.getMethodSubSignature(name, parameterSignatures, returnType);
    }

    @Override
    @Nonnull
    public MethodSubSignature parseMethodSubSignature(@Nonnull String methodSubSignature) {
      return factory.parseMethodSubSignature(methodSubSignature);
    }

    @Override
    @Nonnull
    public FieldSignature parseFieldSignature(@Nonnull String fieldSignature) {
      return factory.parseFieldSignature(fieldSignature);
    }

    @Override
    public FieldSignature getFieldSignature(
        String fieldName, JavaClassType declaringClassSignature, String fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Override
    public FieldSignature getFieldSignature(
        String fieldName, JavaClassType declaringClassSignature, Type fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Override
    @Nonnull
    public FieldSignature getFieldSignature(
        @Nonnull JavaClassType declaringClassSignature, @Nonnull FieldSubSignature subSignature) {
      return factory.getFieldSignature(declaringClassSignature, subSignature);
    }

    @Override
    @Nonnull
    public FieldSubSignature getFieldSubSignature(@Nonnull String name, @Nonnull Type type) {
      return factory.getFieldSubSignature(name, type);
    }

    @Override
    @Nonnull
    public FieldSubSignature parseFieldSubSignature(@Nonnull String subSignature) {
      return factory.parseFieldSubSignature(subSignature);
    }
  }
}
