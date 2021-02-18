package de.upb.swt.soot.java.core.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaSootClass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * This class represents Java Annotations (JSR-175).
 *
 * @author Markus Schmidt
 */

// TODO:[ms] move to a better place?
public class AnnotationType extends JavaClassType {

  final Set<String> metaAnnotationNames =
      new HashSet<>(
          Arrays.asList(
              "@interface_Retention",
              "@interface_Documented",
              "@interface_Target",
              "@interface_Inherited",
              "@interface_Repeatable"));

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link IdentifierFactory}
   *
   * @param annotationName the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageName the corresponding package
   */
  public AnnotationType(@Nonnull String annotationName, @Nonnull PackageName packageName) {
    super(annotationName, packageName);
  }

  @Override
  public String toString() {
    return "interface_" + super.toString();
  }

  public boolean isMetaAnnotation() {
    return metaAnnotationNames.contains(getClassName());
  }

  // TODO: move to a better place
  static final Set<String> forbiddenMethodNames =
      new HashSet<>(
          Arrays.asList(
              "equals", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"));

  public static boolean validateAnnotation(@Nonnull JavaSootClass annotationClass) {

    for (SootMethod method : annotationClass.getMethods()) {

      // method has nonvoid return type
      if (method.getReturnType() instanceof VoidType) {
        return false;
      }

      // methode has no parameter
      if (method.getParameterCount() != 0) {
        return false;
      }

      // methodname is not from a method of java.lang.Object
      if (forbiddenMethodNames.contains(method.getName())) {
        return false;
      }

      // does not throw an exception
      if (!method.getExceptionSignatures().isEmpty()) {
        return false;
      }
    }

    return true;
  }
}
