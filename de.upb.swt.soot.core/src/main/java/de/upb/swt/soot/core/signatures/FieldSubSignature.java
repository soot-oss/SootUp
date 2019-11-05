package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/**
 * Defines a sub-signature of a field, containing the field name and the type signature.
 *
 * @author Jan Martin Persch
 * @author Jan Martin Persch
 */
public class FieldSubSignature extends AbstractClassMemberSubSignature
    implements Comparable<FieldSubSignature> {
  // region Fields

  // endregion /Fields/

  // region Constructor

  /**
   * Creates a new instance of the {@link FieldSubSignature} class.
   *
   * @param name The method name.
   * @param type The type signature.
   */
  public FieldSubSignature(@Nonnull String name, @Nonnull Type type) {
    super(name, type);
  }

  // endregion /Constructor/

  // region Properties

  // endregion /Properties/

  // region Methods

  @Override
  public int compareTo(@Nonnull FieldSubSignature o) {
    return super.compareTo(o);
  }

  @Override
  @Nonnull
  public FieldSignature toFullSignature(@Nonnull ClassType declClassSignature) {
    return new FieldSignature(declClassSignature, this);
  }

  // endregion /Methods/
}
