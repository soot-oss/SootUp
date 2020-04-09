package de.upb.swt.soot.java.core;

import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JavaSootClass extends SootClass {

  public boolean isJavaLibraryClass() {
    return this.classSignature.isBuiltInClass();
  }

  @Nonnull
  public JavaSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }

  private final Supplier<Iterable<AnnotationType>> lazyAnnotations =
      Suppliers.memoize(((JavaSootClassSource) classSource)::resolveAnnotations);

  @Nonnull
  public Iterable<AnnotationType> getAnnotations() {
    return lazyAnnotations.get();
  }

  @Override
  public JavaSootClassSource getClassSource() {
    return (JavaSootClassSource) super.getClassSource();
  }

  // Convenience withers that delegate to an OverridingClassSource

  /**
   * Creates a new JavaSootClass based on a new {@link OverridingJavaClassSource}. This is useful to
   * change selected parts of a {@link SootClass} without recreating a {@link JavaSootClassSource}
   * completely. {@link OverridingJavaClassSource} allows for replacing specific parts of a class,
   * such as fields and methods.
   */
  @Nonnull
  public JavaSootClass withOverridingClassSource(
      Function<OverridingJavaClassSource, OverridingJavaClassSource> overrider) {
    return new JavaSootClass(
        overrider.apply(new OverridingJavaClassSource(getClassSource())), sourceType);
  }

  @Nonnull
  public JavaSootClass withReplacedMethod(
      @Nonnull SootMethod toReplace, @Nonnull SootMethod replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedMethod(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public JavaSootClass withMethods(@Nonnull Collection<SootMethod> methods) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withMethods(methods), sourceType);
  }

  @Nonnull
  public JavaSootClass withReplacedField(
      @Nonnull SootField toReplace, @Nonnull SootField replacement) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withReplacedField(toReplace, replacement),
        sourceType);
  }

  @Nonnull
  public JavaSootClass withFields(@Nonnull Collection<SootField> fields) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withFields(fields), sourceType);
  }

  @Nonnull
  public JavaSootClass withModifiers(@Nonnull Set<Modifier> modifiers) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withModifiers(modifiers), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public JavaSootClass withSuperclass(@Nonnull Optional<ClassType> superclass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withSuperclass(superclass), sourceType);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public JavaSootClass withOuterClass(@Nonnull Optional<ClassType> outerClass) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withOuterClass(outerClass), sourceType);
  }

  @Nonnull
  public JavaSootClass withPosition(@Nullable Position position) {
    return new JavaSootClass(
        new OverridingJavaClassSource(getClassSource()).withPosition(position), sourceType);
  }
}
