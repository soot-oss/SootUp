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
import javax.annotation.Nonnull;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class JavaSootMethod extends SootMethod implements HasAnnotation {
  @Nonnull private final Iterable<AnnotationUsage> annotations;

  public JavaSootMethod(
      @Nonnull BodySource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<MethodModifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      @Nonnull Iterable<AnnotationUsage> annotations,
      @Nonnull Position position) {
    super(source, methodSignature, modifiers, thrownExceptions, position);
    this.annotations = annotations;
  }

  @Nonnull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  @Nonnull
  @Override
  public JavaSootMethod withOverridingMethodSource(
      @Nonnull Function<OverridingBodySource, OverridingBodySource> overrider) {
    return new JavaSootMethod(
        overrider.apply(new OverridingBodySource(bodySource)),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  @Nonnull
  @Override
  public JavaSootMethod withSource(@Nonnull BodySource source) {
    return new JavaSootMethod(
        source, getSignature(), getModifiers(), exceptions, getAnnotations(), getPosition());
  }

  @Nonnull
  @Override
  public JavaSootMethod withModifiers(@Nonnull Iterable<MethodModifier> modifiers) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        modifiers,
        getExceptionSignatures(),
        getAnnotations(),
        getPosition());
  }

  @Nonnull
  @Override
  public JavaSootMethod withThrownExceptions(@Nonnull Iterable<ClassType> thrownExceptions) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        thrownExceptions,
        getAnnotations(),
        getPosition());
  }

  @Nonnull
  public JavaSootMethod withAnnotations(@Nonnull Iterable<AnnotationUsage> annotations) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        getExceptionSignatures(),
        annotations,
        getPosition());
  }

  @Nonnull
  @Override
  public JavaSootMethod withBody(@Nonnull Body body) {
    return new JavaSootMethod(
        new OverridingBodySource(bodySource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  @Nonnull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootMethodBuilder();
  }

  public interface AnnotationOrSignatureStep extends MethodSourceStep {
    BuildStep withAnnotation(@Nonnull Iterable<AnnotationUsage> annotations);
  }

  /**
   * Defines a {@link JavaSootField.JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootMethodBuilder extends SootMethodBuilder
      implements AnnotationOrSignatureStep {

    private Iterable<AnnotationUsage> annotations = null;

    @Nonnull
    public Iterable<AnnotationUsage> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @Nonnull
    public BuildStep withAnnotation(@Nonnull Iterable<AnnotationUsage> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @Nonnull
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
