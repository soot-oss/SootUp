package de.upb.soot.namespaces;

import com.google.common.base.Preconditions;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.types.ArrayType;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.ModuleTypeFactory;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import de.upb.soot.types.TypeFactory;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
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
  public @Nonnull Collection<ClassSource> getClassSources(
      @Nonnull SignatureFactory signatureFactory, TypeFactory typeFactory) {
    Preconditions.checkArgument(
        signatureFactory instanceof ModuleSignatureFactory,
        "Factory must be a ModuleSignatureFactory");

    Set<ClassSource> found = new HashSet<>();
    Collection<String> availableModules = moduleFinder.discoverAllModules();
    for (String module : availableModules) {
      AbstractNamespace ns = moduleFinder.discoverModule(module);
      TypeFactory typeFactoryWrapper = typeFactory;

      if (!(ns instanceof JrtFileSystemNamespace)) {
        /*
         * we need a wrapper to create correct types for the found classes, all other ignore modules by default, or have
         * no clue about modules.
         */
        typeFactoryWrapper = new TypeFactoryWrapper(typeFactory, module);
      }

      // FIXME: [JMP] `ns` may be `null`
      found.addAll(ns.getClassSources(signatureFactory, typeFactoryWrapper));
    }

    return found;
  }

  @Override
  public @Nonnull Optional<ClassSource> getClassSource(@Nonnull JavaClassType signature) {

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

  private static class TypeFactoryWrapper implements TypeFactory {

    private final TypeFactory factory;
    private final String moduleName;

    private TypeFactoryWrapper(TypeFactory factory, String moduleName) {
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
      if (factory instanceof ModuleTypeFactory) {
        ModuleTypeFactory moduleSignatureFactory = (ModuleTypeFactory) factory;
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
  }
}
