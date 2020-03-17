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
  public static JavaSootFieldBuilder builder() {
    return new JavaSootFieldBuilder();
  }
  /**
   * Defines a {@link JavaSootField} builder to provide a fluent API.
   *
   * @author Jan Martin Persch
   * @author Markus Schmidt
   */
  public static class JavaSootFieldBuilder extends SootFieldBuilder
      implements Builder.ModifierStep, Builder.BuildStep, Builder<FieldSignature, SootField> {

    private Iterable<AnnotationType> annotations = null;
    private FieldSignature signature;
    private Iterable<Modifier> modifiers;

    @Nonnull
    protected FieldSignature getSignature() {
      return signature;
    }

    @Nonnull
    protected Iterable<Modifier> getModifiers() {
      return modifiers;
    }

    // FIXME: kein StepWiseBuilder mehr!!!

    @Nonnull
    public Builder.ModifierStep withSignature(@Nonnull FieldSignature signature) {
      this.signature = signature;
      return this;
    }

    @Nonnull
    public BuildStep withModifiers(@Nonnull Iterable<Modifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Nonnull
    public BuildStep withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {
      this.annotations = annotations;
      return this;
    }

    public Iterable<AnnotationType> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @Nonnull
    public JavaSootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getAnnotations());
    }
  }
}
