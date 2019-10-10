package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.EnumSet;
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
    super(declaringClass, subSignature, EnumSet.noneOf(Modifier.class));
  }

  public FieldSignature(
      @Nonnull JavaClassType declaringClass,
      @Nonnull FieldSubSignature subSignature,
      @Nonnull EnumSet<Modifier> modifiers) {
    super(declaringClass, subSignature, modifiers);
  }

  @Override
  @Nonnull
  public FieldSubSignature getSubSignature() {
    return (FieldSubSignature) super.getSubSignature();
  }
}
