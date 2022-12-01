package sootup.java.core.types;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.VoidType;
import sootup.java.core.JavaAnnotationSootMethod;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/**
 * This class represents Java Annotations (JSR-175).
 *
 * @author Markus Schmidt, Bastian Haverkamp
 */
public class AnnotationType extends JavaClassType {

  public void setInherited(boolean inherited) {
    isInherited = inherited;
  }

  @Nullable private Boolean isInherited = null;
  @Nullable private Map<String, Object> defaultValues = null;

  /**
   * Returns default values of annotation parameters. Needs to be called at least once with a
   * JavaView (per annotationtype). A mapping of string -&gt; NullConstant means, that there is no
   * default value for this parameter method (it needs to be declared on annotation use!)
   *
   * @param viewOptional view to resolve annotation soot class of AnnotationType
   * @return default values of all parameters of this annotation
   */
  public Map<String, Object> getDefaultValues(@Nonnull Optional<JavaView> viewOptional) {
    if (defaultValues == null) {
      defaultValues = new HashMap<>();
      if (viewOptional.isPresent()) {
        JavaView jv = viewOptional.get();

        // meta annotations are not in the view
        if (this.isMetaAnnotation()) {
          return defaultValues;
        }

        if (!jv.getClass(this).isPresent()) {
          throw new RuntimeException("Class of annotation not in view");
        }

        JavaSootClass jsc = jv.getClass(this).get();

        for (JavaSootMethod jsm : jsc.getMethods()) {
          JavaAnnotationSootMethod jasm = (JavaAnnotationSootMethod) jsm;
          Object defaultVal = jasm.getDefaultValue();
          defaultValues.put(jasm.getName(), defaultVal);
        }
      } else {
        throw new IllegalArgumentException(
            "getDefaultMethods needs to be called at least once with a view for each annotation type.");
      }
    }

    return defaultValues;
  }

  /**
   * Returns whether this annotation has the meta annotation Inherited. Needs to be called at least
   * once with a JavaView for each annotation.
   *
   * @param viewOptional view to resolve annotation soot class of AnnotationType
   * @return whether annotation has @Inherited meta annotation
   */
  public boolean isInherited(Optional<JavaView> viewOptional) {
    if (isInherited == null) {
      if (!viewOptional.isPresent()) {
        throw new IllegalArgumentException(
            "JavaView needs to be supplied at least once for the annotationType");
      }
      JavaView jv = viewOptional.get();
      if (!jv.getClass(this).isPresent()) {
        throw new RuntimeException("Class of annotation not in view");
      }
      JavaSootClass jsc = jv.getClass(this).get();

      isInherited =
          StreamSupport.stream(jsc.getAnnotations(viewOptional).spliterator(), false)
              .anyMatch(
                  annotationUsage ->
                      annotationUsage.getAnnotation().getClassName().equals("Inherited"));
    }

    return isInherited;
  }

  final Set<String> metaAnnotationNames =
      new HashSet<>(Arrays.asList("Retention", "Documented", "Target", "Inherited", "Repeatable"));

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

  public AnnotationType(
      @Nonnull String annotationName, @Nonnull PackageName packageName, boolean isInherited) {
    super(annotationName, packageName);
    this.isInherited = isInherited;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AnnotationType that = (AnnotationType) o;

    return this.getFullyQualifiedName().equals(that.getFullyQualifiedName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isInherited, defaultValues);
  }
}
