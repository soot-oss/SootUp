package sootup.core;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Linghui luo, Christian Brüggemann and others
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

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/**
 * A factory used for creating language-specific objects representing entities of the language, for
 * instance {@link PackageName}, {@link MethodSignature} and others.
 */
public interface IdentifierFactory {

  /**
   * Gets the package name.
   *
   * @param packageName the package name
   * @return the package name
   */
  PackageName getPackageName(String packageName);

  /**
   * Gets the method signature.
   *
   * @param fullyQualifiedNameDeclClass the fully qualified name decl class
   * @param methodName the method name
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      String fullyQualifiedNameDeclClass,
      String methodName,
      String fqReturnType,
      List<String> parameters);

  /**
   * Gets the method signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @param methodName the method name
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      ClassType declaringClassSignature,
      String methodName,
      String fqReturnType,
      List<String> parameters);

  /**
   * Gets the method signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @param methodName the method name
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      ClassType declaringClassSignature,
      String methodName,
      Type fqReturnType,
      List<Type> parameters);

  /**
   * Gets the method signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @param subSignature the sub signature
   * @return the method signature
   */
  @NonNull MethodSignature getMethodSignature(
      @NonNull ClassType declaringClassSignature, @NonNull MethodSubSignature subSignature);

  /**
   * Parses the method signature.
   *
   * @param methodSignature the method signature
   * @return the method signature
   */
  @NonNull MethodSignature parseMethodSignature(@NonNull String methodSignature);

  /**
   * Gets the method sub signature.
   *
   * @param name the name
   * @param parameterSignatures the parameter signatures
   * @param returnType the return type
   * @return the method sub signature
   */
  @NonNull MethodSubSignature getMethodSubSignature(
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull Iterable<? extends Type> parameterSignatures);

  /**
   * Parses the method sub signature.
   *
   * @param methodSubSignature the method sub signature
   * @return the method sub signature
   */
  @NonNull MethodSubSignature parseMethodSubSignature(@NonNull String methodSubSignature);

  /**
   * Parses the field signature.
   *
   * @param fieldSignature the field signature
   * @return the field signature
   */
  @NonNull FieldSignature parseFieldSignature(@NonNull String fieldSignature);

  /**
   * Gets the field signature.
   *
   * @param fieldName the field name
   * @param declaringClassSignature the declaring class signature
   * @param fieldType the field type
   * @return the field signature
   */
  FieldSignature getFieldSignature(
      String fieldName, ClassType declaringClassSignature, String fieldType);

  /**
   * Gets the field signature.
   *
   * @param fieldName the field name
   * @param declaringClassSignature the declaring class signature
   * @param fieldType the field type
   * @return the field signature
   */
  FieldSignature getFieldSignature(
      String fieldName, ClassType declaringClassSignature, Type fieldType);

  /**
   * Gets the field signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @param subSignature the sub signature
   * @return the field signature
   */
  @NonNull FieldSignature getFieldSignature(
      @NonNull ClassType declaringClassSignature, @NonNull FieldSubSignature subSignature);

  /**
   * Gets the field sub signature.
   *
   * @param name the name
   * @param type the type
   * @return the field sub signature
   */
  @NonNull FieldSubSignature getFieldSubSignature(@NonNull String name, @NonNull Type type);

  /**
   * Parses the field sub signature.
   *
   * @param subSignature the sub signature
   * @return the field sub signature
   */
  @NonNull FieldSubSignature parseFieldSubSignature(@NonNull String subSignature);

  /**
   * Gets the class type.
   *
   * @param className the class name
   * @param packageName the package name
   * @return the class type
   */
  ClassType getClassType(String className, String packageName);

  /**
   * Gets the class type.
   *
   * @param fullyQualifiedClassName the fully qualified class name
   * @return the class type
   */
  ClassType getClassType(String fullyQualifiedClassName);

  /**
   * Gets the type.
   *
   * @param typeName the type name
   * @return the type
   */
  Type getType(String typeName);

  /**
   * Gets the primitive type.
   *
   * @param typeName the type name
   * @return the primitive type
   */
  @NonNull Optional<PrimitiveType> getPrimitiveType(@NonNull String typeName);

  @NonNull ClassType getBoxedType(@NonNull PrimitiveType primitiveType);

  /**
   * Gets the array type.
   *
   * @param baseType the base type
   * @param dim the dim
   * @return the array type
   */
  ArrayType getArrayType(Type baseType, int dim);

  /**
   * Gets the static initializer method signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @return the static initializer method signature
   */
  MethodSignature getStaticInitializerSignature(ClassType declaringClassSignature);

  boolean isStaticInitializerSubSignature(@NonNull MethodSubSignature methodSubSignature);

  boolean isConstructorSignature(@NonNull MethodSignature methodSignature);

  boolean isConstructorSubSignature(@NonNull MethodSubSignature methodSubSignature);

  boolean isMainSubSignature(@NonNull MethodSubSignature methodSubSignature);
}
