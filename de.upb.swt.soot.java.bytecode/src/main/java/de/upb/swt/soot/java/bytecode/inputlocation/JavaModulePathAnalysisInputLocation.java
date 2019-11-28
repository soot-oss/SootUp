package de.upb.swt.soot.java.bytecode.inputlocation;

import com.google.common.base.Preconditions;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.ClassResolvingException;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;
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
 * An implementation of the {@link AnalysisInputLocation} interface for the Java modulepath. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathAnalysisInputLocation implements BytecodeAnalysisInputLocation {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaModulePathAnalysisInputLocation.class);

  @Nonnull private final String modulePath;

  /**
   * Creates a {@link JavaModulePathAnalysisInputLocation} which locates classes in the given module
   * path.
   *
   * @param modulePath The class path to search in The {@link ClassProvider} for generating {@link
   *     ClassSource}es for the files found on the class path
   */
  public JavaModulePathAnalysisInputLocation(@Nonnull String modulePath) {
    this.modulePath = modulePath;
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    Preconditions.checkArgument(
        identifierFactory instanceof ModuleIdentifierFactory,
        "Factory must be a ModuleSignatureFactory");

    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    ModuleFinder moduleFinder =
        new ModuleFinder(new AsmJavaClassProvider(bodyInterceptors), modulePath);
    Set<AbstractClassSource> found = new HashSet<>();
    Collection<String> availableModules = moduleFinder.discoverAllModules();
    for (String module : availableModules) {
      AnalysisInputLocation inputLocation = moduleFinder.discoverModule(module);
      IdentifierFactory identifierFactoryWrapper = identifierFactory;

      if (!(inputLocation instanceof JrtFileSystemAnalysisInputLocation)) {
        /*
         * we need a wrapper to create correct types for the found classes, all other ignore modules by default, or have
         * no clue about modules.
         */
        identifierFactoryWrapper = new IdentifierFactoryWrapper(identifierFactoryWrapper, module);
      }

      // FIXME: [JMP] `inputLocation` may be `null`
      found.addAll(inputLocation.getClassSources(identifierFactoryWrapper, classLoadingOptions));
    }

    return found;
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ClassType classType, @Nonnull ClassLoadingOptions classLoadingOptions) {
    JavaClassType klassType = (JavaClassType) classType;
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();

    String modulename =
        ((ModulePackageName) klassType.getPackageName()).getModuleSignature().getModuleName();
    // lookup the ns for the class provider from the cache and use him...
    AnalysisInputLocation inputLocation =
        new ModuleFinder(new AsmJavaClassProvider(bodyInterceptors), modulePath)
            .discoverModule(modulename);

    if (inputLocation == null) {
      try {
        throw new ClassResolvingException("No Namespace for class " + klassType);
      } catch (ClassResolvingException e) {
        e.printStackTrace();
        // FIXME: [JMP] Throwing exception and catching it immediately? This causes `inputLocation`
        // to remain
        // `null`.
      }
    }

    // FIXME: [JMP] `inputLocation` may be `null`
    return inputLocation.getClassSource(klassType);
  }

  private static class IdentifierFactoryWrapper implements IdentifierFactory {

    private final IdentifierFactory factory;
    private final String moduleName;

    private IdentifierFactoryWrapper(IdentifierFactory factory, String moduleName) {
      this.factory = factory;
      this.moduleName = moduleName;
    }

    @Override
    public @Nonnull ClassType getClassType(@Nonnull String className, @Nonnull String packageName) {
      return factory.getClassType(className, packageName);
    }

    @Override
    public @Nonnull ClassType getClassType(@Nonnull String fullyQualifiedClassName) {
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
    public @Nonnull ClassType fromPath(@Nonnull Path file) {
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
        ClassType declaringClassSignature,
        String fqReturnType,
        List<String> parameters) {
      return factory.getMethodSignature(
          methodName, declaringClassSignature, fqReturnType, parameters);
    }

    @Override
    public MethodSignature getMethodSignature(
        String methodName,
        ClassType declaringClassSignature,
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
        @Nonnull ClassType declaringClassSignature, @Nonnull MethodSubSignature subSignature) {
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
        String fieldName, ClassType declaringClassSignature, String fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Override
    public FieldSignature getFieldSignature(
        String fieldName, ClassType declaringClassSignature, Type fieldType) {
      return factory.getFieldSignature(fieldName, declaringClassSignature, fieldType);
    }

    @Override
    @Nonnull
    public FieldSignature getFieldSignature(
        @Nonnull ClassType declaringClassSignature, @Nonnull FieldSubSignature subSignature) {
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
