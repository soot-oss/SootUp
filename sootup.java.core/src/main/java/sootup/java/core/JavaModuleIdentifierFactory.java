package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Christian Brüggemann and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.Type;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;

public class JavaModuleIdentifierFactory extends JavaIdentifierFactory {

  public static final String MODULE_INFO_FILE = "module-info";

  @NonNull
  private final Cache<ModuleSignature, JavaModuleIdentifierFactory> moduleIdentifierFactoryWrapper =
      CacheBuilder.newBuilder().weakValues().build();

  /**
   * Returns a view on this factory that resolves class names without an explicit module to {@code
   * module}. The wrappers are cached per factory instance, so they are released together with the
   * {@link sootup.java.core.views.JavaModuleView} this factory belongs to.
   */
  @NonNull
  public JavaModuleIdentifierFactory forModule(@NonNull String module) {
    return forModule(getModuleSignature(module));
  }

  /**
   * Returns a view on this factory that resolves class names without an explicit module to {@code
   * moduleSignature}.
   */
  @NonNull
  public JavaModuleIdentifierFactory forModule(@NonNull ModuleSignature moduleSignature) {
    return moduleIdentifierFactoryWrapper
        .asMap()
        .computeIfAbsent(moduleSignature, JavaModuleIdentifierFactoryWrapper::new);
  }

  @Override
  public boolean isMainSubSignature(@NonNull MethodSubSignature methodSubSignature) {
    if (methodSubSignature.getName().equals("main")) {
      final List<Type> parameterTypes = methodSubSignature.getParameterTypes();
      if (parameterTypes.size() == 1) {
        return parameterTypes.get(0).toString().equals("java.lang.String[]")
            || parameterTypes.get(0).toString().equals("java.base/java.lang.String[]");
      }
    }
    return false;
  }

  @Override
  public ModuleJavaClassType getClassType(final String className, final String packageName) {
    return getClassType(className, packageName, ModuleSignature.UNNAMED_MODULE.getModuleName());
  }

  @Override
  public ModuleJavaClassType getClassType(String fullyQualyfiedClassNameWithModule) {

    int moduleSplit = fullyQualyfiedClassNameWithModule.indexOf('/');
    String moduleName = null;
    if (moduleSplit >= 0) {
      moduleName = fullyQualyfiedClassNameWithModule.substring(0, moduleSplit);
      fullyQualyfiedClassNameWithModule =
          fullyQualyfiedClassNameWithModule.substring(moduleSplit + 1);
    }

    String className = ClassUtils.getShortClassName(fullyQualyfiedClassNameWithModule);
    String packageName = ClassUtils.getPackageName(fullyQualyfiedClassNameWithModule);

    if (className.equals(MODULE_INFO_FILE)) {
      throw new IllegalArgumentException("module-info is not allowed as classname.");
    }

    if (moduleName == null) {
      return getClassType(className, packageName);
    } else {
      return getClassType(className, packageName, moduleName);
    }
  }

  /**
   * Returns a unique ClassType. The method looks up a cache if it already contains a ClassType with
   * the given name/package/module. If the cache lookup fails a new ClassType is created. This lets
   * callers compare ClassTypes with {@code ==}.
   *
   * @param className the simple name of the class
   * @param packageName the declaring package
   * @param moduleName the declaring module
   * @return a ClassSignature for a Java 9 class
   * @throws NullPointerException if the given module name or package name is null. Use the empty
   *     string to denote the unnamed module or the default package.
   */
  public ModuleJavaClassType getClassType(
      final @NonNull String className,
      final @NonNull String packageName,
      final @NonNull String moduleName) {
    return ModuleJavaClassType.of(className, getPackageName(packageName, moduleName));
  }

  public ModuleJavaClassType getClassType(
      final @NonNull String className,
      final @NonNull String packageName,
      final @NonNull ModuleSignature moduleSignature) {
    return ModuleJavaClassType.of(className, getPackageName(packageName, moduleSignature));
  }

  /**
   * Returns a unique ModuleSignature. The method looks up a cache if it already contains a
   * signature with the given module name. If the cache lookup fails a new signature is created.
   * Returns a unique ModuleSignature. The method looks up a cache if it already contains a
   * signature with the given module name. If the cache lookup fails a new signature is created.
   *
   * @param moduleName the module name; Must not be null. Use the empty string for the unnamed
   *     module
   * @return a ModuleSignature
   * @throws NullPointerException if the given module name is null. Use the empty string to denote
   *     the unnamed module.
   */
  public static ModuleSignature getModuleSignature(@NonNull final String moduleName) {
    return ModuleSignature.of(moduleName);
  }

  @Override
  public ModulePackageName getPackageName(@NonNull final String packageName) {
    return getPackageName(packageName, ModuleSignature.UNNAMED_MODULE.getModuleName());
  }

  /**
   * Returns a unique PackageName. The methodRef looks up a cache if it already contains a signature
   * with the given package and module name. If the cache lookup fails a new signature is created.
   *
   * @param packageName the package name; must not be null use empty string for the default package
   * @param moduleName the module containing the package; must not be null use empty string for the
   *     unnamed module {@link ModuleSignature#UNNAMED_MODULE}
   * @return a ModulePackageName
   * @throws NullPointerException if the given module name or package name is null. Use the empty
   *     string to denote the unnamed module or the default package.
   */
  public ModulePackageName getPackageName(
      @NonNull final String packageName, @NonNull final String moduleName) {
    return ModulePackageName.of(packageName, getModuleSignature(moduleName));
  }

  public ModulePackageName getPackageName(
      @NonNull final String packageName, @NonNull final ModuleSignature moduleSignature) {
    return ModulePackageName.of(packageName, moduleSignature);
  }

  /** Wrapper which refers to a given ModuleSignature when building stuff */
  private static class JavaModuleIdentifierFactoryWrapper extends JavaModuleIdentifierFactory {

    @NonNull private final ModuleSignature moduleSignature;

    private JavaModuleIdentifierFactoryWrapper(@NonNull ModuleSignature moduleSignature) {
      this.moduleSignature = moduleSignature;
    }

    @Override
    public ModuleJavaClassType getClassType(String fullyQualifiedClassName) {
      int moduleSplitPos = fullyQualifiedClassName.indexOf('/');
      ModuleSignature moduleSig;
      if (moduleSplitPos >= 0) {
        String moduleName = fullyQualifiedClassName.substring(0, moduleSplitPos);
        fullyQualifiedClassName = fullyQualifiedClassName.substring(moduleSplitPos + 1);
        moduleSig = getModuleSignature(moduleName);
      } else {
        moduleSig = moduleSignature;
      }

      String className = ClassUtils.getShortClassName(fullyQualifiedClassName);
      String packageName = ClassUtils.getPackageName(fullyQualifiedClassName);
      return getClassType(className, packageName, moduleSig);
    }

    @Override
    public MethodSignature getMethodSignature(
        String fullyQualifiedNameDeclClass,
        String methodName,
        String fqReturnType,
        List<String> parameters) {
      return super.getMethodSignature(
          fullyQualifiedNameDeclClass, methodName, fqReturnType, parameters);
    }
  }
}
