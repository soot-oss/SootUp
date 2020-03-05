package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClassMember;
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



   /* Creates a {@link JavaSootField} builder.
   *
           * @return A {@link JavaSootField} builder.
   */
  @Nonnull
  public static JavaSootField.Builder.SignatureStep builder() {
    return new JavaSootFieldBuilder();
  }

  /**
   * Defines a stepwise builder for the {@link SootField} class.
   *
   * @see #builder()
   * @author Jan Martin Persch
   */
  public interface Builder extends SootClassMember.Builder<FieldSignature, JavaSootField> {

    interface ModifiersStep2 extends ModifiersStep {

    }

    interface AnnotationStep extends SootClassMember.Builder.ModifiersStep<JavaSootField.Builder> {

      @Nonnull
      AnnotationStep withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {

    }
  }


  /**
   * Defines a {@link JavaSootField} builder to provide a fluent API.
   *
   * @author Jan Martin Persch
   * @author Markus Schmidt
   */
  protected static class JavaSootFieldBuildStep extends SootFieldBuildStep implements SignatureStep, ModifiersStep, Builder{

    /** Creates a new instance of the {@link SootFieldBuildStep} class. */
    JavaSootFieldBuildStep() {
      super(JavaSootField.class);
    }

    private Iterable<AnnotationType> annotations;

    @Nonnull
    public Builder withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @Nonnull
    public JavaSootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getAnnotations());
    }

    public Iterable<AnnotationType> getAnnotations() {
      return annotations;
    }
  }
}
