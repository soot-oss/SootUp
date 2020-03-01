package de.upb.swt.soot.core.signatures;

import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.function.Supplier;
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

  // TODO: [ms] i dont think this method is necessary -> use identifierfactory for it
  @Override
  @Nonnull
  public FieldSignature toFullSignature(@Nonnull ClassType declClassSignature) {
    return new FieldSignature(declClassSignature, this);
  }

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(() -> String.format("%s %s", getType(), getName()));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  @Override
  public void toString(StmtPrinter printer) {
    printer.typeSignature(getType());
    printer.literal(" ");
    printer.literal(getName());
  }

  // endregion /Methods/
}
