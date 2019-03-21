package de.upb.soot.signatures;

import de.upb.soot.core.SootClass;
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

public interface SignatureFactory {
  PackageSignature getPackageSignature(String packageName);

  JavaClassSignature getClassSignature(String className, String packageName);

  JavaClassSignature getClassSignature(String fullyQualifiedClassName);

  TypeSignature getTypeSignature(String typeName);

  @Nonnull
  Optional<PrimitiveTypeSignature> getPrimitiveTypeSignature(@Nonnull String typeName);

  ArrayTypeSignature getArrayTypeSignature(TypeSignature baseType, int dim);

  MethodSignature getMethodSignature(
      String methodName,
      String fullyQualifiedNameDeclClass,
      String fqReturnType,
      List<String> parameters);

  MethodSignature getMethodSignature(
      String methodName,
      JavaClassSignature declaringClassSignature,
      String fqReturnType,
      List<String> parameters);

  MethodSignature getMethodSignature(
      String methodName,
      JavaClassSignature declaringClassSignature,
      TypeSignature fqReturnType,
      List<TypeSignature> parameters);

  @Nonnull
  MethodSignature getMethodSignature(
      @Nonnull SootClass declaringClass, @Nonnull MethodSubSignature subSignature);

  @Nonnull
  MethodSignature getMethodSignature(
      @Nonnull JavaClassSignature declaringClassSignature,
      @Nonnull MethodSubSignature subSignature);

  @Nonnull
  MethodSignature parseMethodSignature(@Nonnull String methodSignature);

  @Nonnull
  MethodSubSignature getMethodSubSignature(
      @Nonnull String name,
      @Nonnull Iterable<? extends TypeSignature> parameterSignatures,
      @Nonnull TypeSignature returnTypeSignature);

  @Nonnull
  MethodSubSignature parseMethodSubSignature(@Nonnull String methodSubSignature);

  @Nonnull
  FieldSignature parseFieldSignature(@Nonnull String fieldSignature);

  FieldSignature getFieldSignature(
      String fieldName, JavaClassSignature declaringClassSignature, String fieldType);

  FieldSignature getFieldSignature(
      String fieldName, JavaClassSignature declaringClassSignature, TypeSignature fieldType);

  @Nonnull
  FieldSignature getFieldSignature(
      @Nonnull JavaClassSignature declaringClassSignature, @Nonnull FieldSubSignature subSignature);

  @Nonnull
  FieldSubSignature getFieldSubSignature(
      @Nonnull String name, @Nonnull TypeSignature typeSignature);

  @Nonnull
  FieldSubSignature parseFieldSubSignature(@Nonnull String subSignature);

  JavaClassSignature fromPath(Path file);
}
