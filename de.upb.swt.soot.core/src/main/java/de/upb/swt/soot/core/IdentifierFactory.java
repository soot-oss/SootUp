/*
 * @author Linghui Luo
 */
package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.signatures.MethodSubSignature;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Andreas Dann
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

/** Interface for identifierFactory which identifies signatures for object oriented languages. */
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
   * @param methodName the method name
   * @param fullyQualifiedNameDeclClass the fully qualified name decl class
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      String methodName,
      String fullyQualifiedNameDeclClass,
      String fqReturnType,
      List<String> parameters);

  /**
   * Gets the method signature.
   *
   * @param methodName the method name
   * @param declaringClassSignature the declaring class signature
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      String methodName,
      ClassType declaringClassSignature,
      String fqReturnType,
      List<String> parameters);

  /**
   * Gets the method signature.
   *
   * @param methodName the method name
   * @param declaringClassSignature the declaring class signature
   * @param fqReturnType the fq return type
   * @param parameters the parameters
   * @return the method signature
   */
  MethodSignature getMethodSignature(
      String methodName,
      ClassType declaringClassSignature,
      Type fqReturnType,
      List<Type> parameters);

  /**
   * Gets the method signature.
   *
   * @param declaringClass the declaring class
   * @param subSignature the sub signature
   * @return the method signature
   */
  @Nonnull
  MethodSignature getMethodSignature(
      @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature);

  /**
   * Gets the method signature.
   *
   * @param declaringClassSignature the declaring class signature
   * @param subSignature the sub signature
   * @return the method signature
   */
  @Nonnull
  MethodSignature getMethodSignature(
      @Nonnull ClassType declaringClassSignature, @Nonnull MethodSubSignature subSignature);

  /**
   * Parses the method signature.
   *
   * @param methodSignature the method signature
   * @return the method signature
   */
  @Nonnull
  MethodSignature parseMethodSignature(@Nonnull String methodSignature);

  /**
   * Gets the method sub signature.
   *
   * @param name the name
   * @param parameterSignatures the parameter signatures
   * @param returnType the return type
   * @return the method sub signature
   */
  @Nonnull
  MethodSubSignature getMethodSubSignature(
      @Nonnull String name,
      @Nonnull Iterable<? extends Type> parameterSignatures,
      @Nonnull Type returnType);

  /**
   * Parses the method sub signature.
   *
   * @param methodSubSignature the method sub signature
   * @return the method sub signature
   */
  @Nonnull
  MethodSubSignature parseMethodSubSignature(@Nonnull String methodSubSignature);

  /**
   * Parses the field signature.
   *
   * @param fieldSignature the field signature
   * @return the field signature
   */
  @Nonnull
  FieldSignature parseFieldSignature(@Nonnull String fieldSignature);

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
  @Nonnull
  FieldSignature getFieldSignature(
      @Nonnull ClassType declaringClassSignature, @Nonnull FieldSubSignature subSignature);

  /**
   * Gets the field sub signature.
   *
   * @param name the name
   * @param type the type
   * @return the field sub signature
   */
  @Nonnull
  FieldSubSignature getFieldSubSignature(@Nonnull String name, @Nonnull Type type);

  /**
   * Parses the field sub signature.
   *
   * @param subSignature the sub signature
   * @return the field sub signature
   */
  @Nonnull
  FieldSubSignature parseFieldSubSignature(@Nonnull String subSignature);

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
  @Nonnull
  Optional<PrimitiveType> getPrimitiveType(@Nonnull String typeName);

  /**
   * Gets the array type.
   *
   * @param baseType the base type
   * @param dim the dim
   * @return the array type
   */
  ArrayType getArrayType(Type baseType, int dim);

  /**
   * From path.
   *
   * @param file the file
   * @return the class type
   */
  ClassType fromPath(Path file);
}
