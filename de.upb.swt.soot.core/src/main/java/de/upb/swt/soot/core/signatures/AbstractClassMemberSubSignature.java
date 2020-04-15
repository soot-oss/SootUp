package de.upb.swt.soot.core.signatures;

import com.google.common.base.Objects;
import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines the base class for class member sub signatures.
 *
 * @see FieldSubSignature
 * @see MethodSubSignature
 * @author Jan Martin Persch
 */
public abstract class AbstractClassMemberSubSignature {

  /** Creates a new instance of the {@link AbstractClassMemberSubSignature} class. */
  protected AbstractClassMemberSubSignature(@Nonnull String name, @Nonnull Type type) {
    this.name = name;
    this.type = type;
  }

  @Nonnull private final String name;

  /**
   * Gets the name.
   *
   * @return The value to get.
   */
  @Nonnull
  public String getName() {
    return this.name;
  }

  @Nonnull private final Type type;

  /**
   * Gets the type.
   *
   * @return The value to get.
   */
  @Nonnull
  public Type getType() {
    return this.type;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractClassMemberSubSignature that = (AbstractClassMemberSubSignature) o;

    return Objects.equal(getName(), that.getName()) && Objects.equal(getType(), that.getType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), getType());
  }

  protected int compareTo(@Nonnull AbstractClassMemberSubSignature o) {
    int r = this.getName().compareTo(o.getName());

    if (r != 0) return r;

    return this.getType().toString().compareTo(o.getType().toString());
  }

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(
          () ->
              String.format(
                  "%s %s", Jimple.escape(getType().toString()), Jimple.escape(getName())));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  public abstract void toString(StmtPrinter printer);
}
