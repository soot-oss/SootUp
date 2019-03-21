package de.upb.soot.signatures;

import javax.annotation.Nonnull;

/**
 * Represents the fully qualified signature of a field.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class FieldSignature extends AbstractClassMemberSignature {

  public FieldSignature(
      final JavaClassSignature declaringClass, final String name, final TypeSignature type) {
    this(declaringClass, new FieldSubSignature(name, type));
  }

  public FieldSignature(
      @Nonnull JavaClassSignature declaringClass, @Nonnull FieldSubSignature subSignature) {
    super(declaringClass, subSignature);

    this._subSignature = subSignature;
  }

  @Nonnull private final FieldSubSignature _subSignature;

  @Override
  @Nonnull
  public FieldSubSignature getSubSignature() {
    return _subSignature;
  }
}
