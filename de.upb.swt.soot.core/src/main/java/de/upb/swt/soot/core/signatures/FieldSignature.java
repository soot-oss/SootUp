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
public class FieldSignature extends SootClassMemberSignature {

  @Nonnull private final FieldSubSignature subSignature;

  public FieldSignature(final ClassType declaringClass, final String name, final Type type) {
    this(declaringClass, new FieldSubSignature(name, type));
  }

  public FieldSignature(
      @Nonnull ClassType declaringClass, @Nonnull FieldSubSignature subSignature) {
    super(declaringClass, subSignature);
    this.subSignature = subSignature;
  }

  @Override
  @Nonnull
  public FieldSubSignature getSubSignature() {
    return subSignature;
  }
}
