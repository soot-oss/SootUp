package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
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

  // TODO: [ms] enhance Builder with Annotations

}
