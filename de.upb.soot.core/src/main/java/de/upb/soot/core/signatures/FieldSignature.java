package de.upb.soot.core.signatures;

import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.types.Type;
import javax.annotation.Nonnull;

/**
 * Represents the fully qualified signature of a field.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class FieldSignature extends AbstractClassMemberSignature {

  public FieldSignature(final JavaClassType declaringClass, final String name, final Type type) {
    this(declaringClass, new FieldSubSignature(name, type));
  }

  public FieldSignature(
      @Nonnull JavaClassType declaringClass, @Nonnull FieldSubSignature subSignature) {
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
