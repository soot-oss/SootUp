package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt, Linghui Luo
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

import java.util.Collections;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class JavaSootMethod extends SootMethod implements HasAnnotation {
  @NonNull private final Iterable<AnnotationUsage> annotations;

  public JavaSootMethod(
      @NonNull BodySource source,
      @NonNull MethodSignature methodSignature,
      @NonNull Iterable<MethodModifier> modifiers,
      @NonNull Iterable<ClassType> thrownExceptions,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Position position) {
    super(source, methodSignature, modifiers, thrownExceptions, position);
    this.annotations = annotations;
  }

  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  @NonNull
  @Override
  public JavaSootMethod withOverridingMethodSource(
      @NonNull Function<OverridingBodySource, OverridingBodySource> overrider) {
    return new JavaSootMethod(
        overrider.apply(new OverridingBodySource(bodySource)),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withSource(@NonNull BodySource source) {
    return new JavaSootMethod(
        source, getSignature(), getModifiers(), exceptions, getAnnotations(), getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withModifiers(@NonNull Iterable<MethodModifier> modifiers) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        modifiers,
        getExceptionSignatures(),
        getAnnotations(),
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withThrownExceptions(@NonNull Iterable<ClassType> thrownExceptions) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        thrownExceptions,
        getAnnotations(),
        getPosition());
  }

  @NonNull
  public JavaSootMethod withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        getExceptionSignatures(),
        annotations,
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withBody(@NonNull Body body) {
    return new JavaSootMethod(
        new OverridingBodySource(bodySource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  @NonNull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootMethodBuilder();
  }

  public interface AnnotationOrSignatureStep extends MethodSourceStep {
    BuildStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations);
  }

  /**
   * Defines a {@link JavaSootField.JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootMethodBuilder extends SootMethodBuilder
      implements AnnotationOrSignatureStep {

    private Iterable<AnnotationUsage> annotations = null;

    @NonNull
    public Iterable<AnnotationUsage> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @NonNull
    public BuildStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @NonNull
    public JavaSootMethod build() {
      return new JavaSootMethod(
          getSource(),
          getSignature(),
          getModifiers(),
          getThrownExceptions(),
          getAnnotations(),
          getPosition());
    }
  }
}
