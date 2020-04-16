package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class JavaSootMethod extends SootMethod {
  @Nonnull protected static final String CONSTRUCTOR_NAME = "<init>";
  @Nonnull protected static final String STATIC_INITIALIZER_NAME = "<clinit>";
  @Nonnull private final Iterable<AnnotationType> annotations;

  public JavaSootMethod(
      @Nonnull MethodSource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      @Nonnull Iterable<AnnotationType> annotations) {
    super(source, methodSignature, modifiers, thrownExceptions);
    this.annotations = annotations;
  }

  /**
   * @return yes, if this function is a constructor. Please not that &lt;clinit&gt; methods are not
   *     treated as constructors in this methodRef.
   */
  public boolean isConstructor() {
    return this.getSignature().getName().equals(CONSTRUCTOR_NAME);
  }

  /** @return yes, if this function is a static initializer. */
  public boolean isStaticInitializer() {
    return this.getSignature().getName().equals(STATIC_INITIALIZER_NAME);
  }

  @Nonnull
  public Iterable<AnnotationType> getAnnotations() {
    return annotations;
  }

  @Nonnull
  public JavaSootMethod withOverridingMethodSource(
      Function<OverridingMethodSource, OverridingMethodSource> overrider) {
    return new JavaSootMethod(
        overrider.apply(new OverridingMethodSource(methodSource)),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations());
  }

  @Nonnull
  public JavaSootMethod withSource(MethodSource source) {
    return new JavaSootMethod(source, getSignature(), getModifiers(), exceptions, getAnnotations());
  }

  @Nonnull
  public JavaSootMethod withModifiers(Iterable<Modifier> modifiers) {
    return new JavaSootMethod(
        methodSource, getSignature(), modifiers, getExceptionSignatures(), getAnnotations());
  }

  @Nonnull
  public JavaSootMethod withThrownExceptions(Iterable<ClassType> thrownExceptions) {
    return new JavaSootMethod(
        methodSource, getSignature(), getModifiers(), thrownExceptions, getAnnotations());
  }

  @Nonnull
  public JavaSootMethod withAnnotations(Iterable<AnnotationType> annotations) {
    return new JavaSootMethod(
        methodSource, getSignature(), getModifiers(), getExceptionSignatures(), annotations);
  }

  @Nonnull
  public JavaSootMethod withBody(@Nonnull Body body) {
    return new JavaSootMethod(
        new OverridingMethodSource(methodSource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations());
  }

  /** @see OverridingMethodSource#withBodyStmts(Consumer) */
  @Nonnull
  public JavaSootMethod withBodyStmts(Consumer<List<Stmt>> stmtModifier) {
    return new JavaSootMethod(
        new OverridingMethodSource(methodSource).withBodyStmts(stmtModifier),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations());
  }

  @Nonnull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootMethodBuilder();
  }

  public interface AnnotationOrSignatureStep extends MethodSourceStep {
    BuildStep withAnnotation(Iterable<AnnotationType> annotations);
  }

  /**
   * Defines a {@link JavaSootField.JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootMethodBuilder extends SootMethodBuilder
      implements AnnotationOrSignatureStep {

    private Iterable<AnnotationType> annotations = null;

    @Nonnull
    public Iterable<AnnotationType> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @Nonnull
    public BuildStep withAnnotation(Iterable<AnnotationType> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @Nonnull
    public JavaSootMethod build() {
      return new JavaSootMethod(
          getSource(), getSignature(), getModifiers(), getThrownExceptions(), getAnnotations());
    }
  }
}
