package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/**
 * Represents the fully qualified signature of a field.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class FieldSignature extends AbstractClassMemberSignature {

  public FieldSignature(final ClassType declaringClass, final String name, final Type type) {
    this(declaringClass, new FieldSubSignature(name, type));
  }

  public FieldSignature(
      @Nonnull ClassType declaringClass, @Nonnull FieldSubSignature subSignature) {
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
