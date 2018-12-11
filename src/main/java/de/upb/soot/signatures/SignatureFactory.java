package de.upb.soot.signatures;

import java.nio.file.Path;
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

import java.util.List;

public interface SignatureFactory {
  PackageSignature getPackageSignature(String packageName);

  JavaClassSignature getClassSignature(String className, String packageName);

  JavaClassSignature getClassSignature(String fullyQualifiedClassName);

  TypeSignature getTypeSignature(String typeName);

  MethodSignature getMethodSignature(String methodName, String fullyQualifiedNameDeclClass, String fqReturnType,
      List<String> parameters);

  MethodSignature getMethodSignature(String methodName, JavaClassSignature declaringClassSignature, String fqReturnType,
      List<String> parameters);

  FieldSignature getFieldSignature(String fieldName, JavaClassSignature declaringClassSignature, String fieldType);

  JavaClassSignature fromPath(Path file);
}
