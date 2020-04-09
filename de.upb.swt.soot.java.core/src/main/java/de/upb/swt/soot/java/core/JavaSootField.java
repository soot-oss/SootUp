package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import java.util.Collections;
import javax.annotation.Nonnull;

public class JavaSootField extends SootField {

  @Nonnull private final Iterable<AnnotationType> annotations;

  /**
   * Constructs a Soot field with the given name, type and modifiers.
   *
   * @param signature
   * @param modifiers
   * @param annotations
   */
  public JavaSootField(
      @Nonnull FieldSignature signature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<AnnotationType> annotations) {
    super(signature, modifiers);
    this.annotations = annotations;
  }

  @Nonnull
  public Iterable<AnnotationType> getAnnotations() {
    return annotations;
  }

  @Nonnull
  public JavaSootField withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {
    return new JavaSootField(getSignature(), getModifiers(), annotations);
  }

  @Nonnull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootFieldBuilder();
  }

  public interface AnnotationOrSignatureStep extends SignatureStep {
    BuildStep withAnnotation(Iterable<AnnotationType> annotations);
  }

  /**
   * Defines a {@link JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootFieldBuilder extends SootFieldBuilder
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
    public JavaSootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getAnnotations());
    }
  }
}
