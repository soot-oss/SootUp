package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/**
 * Defines a sub-signature of a field, containing the field name and the type signature.
 *
 * @author Jan Martin Persch
 */
public class FieldSubSignature extends AbstractClassMemberSubSignature {

  /**
   * Creates a new instance of the {@link FieldSubSignature} class.
   *
   * @param name The method name.
   * @param type The type signature.
   */
  public FieldSubSignature(@Nonnull String name, @Nonnull Type type) {
    super(name, type);
  }

  @Override
  @Nonnull
  public FieldSignature toFullSignature(@Nonnull JavaClassType declClassSignature) {
    return new FieldSignature(declClassSignature, this);
  }
}
