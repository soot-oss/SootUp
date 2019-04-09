package de.upb.soot.signatures;

import com.google.common.base.Objects;
import de.upb.soot.util.Utils;
import de.upb.soot.util.concurrent.Lazy;
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
  // region Fields

  // endregion /Fields/

  // region Constructor

  /** Creates a new instance of the {@link AbstractClassMemberSubSignature} class. */
  protected AbstractClassMemberSubSignature(@Nonnull String name, @Nonnull Type type) {
    this._name = name;
    this._type = type;
  }

  // endregion /Constructor/

  // region Properties

  @Nonnull private final String _name;

  /**
   * Gets the name.
   *
   * @return The value to get.
   */
  @Nonnull
  public String getName() {
    return this._name;
  }

  @Nonnull private final Type _type;

  /**
   * Gets the type.
   *
   * @return The value to get.
   */
  @Nonnull
  public Type getSignature() {
    return this._type;
  }

  // endregion /Properties/

  // region Methods

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractClassMemberSubSignature that = (AbstractClassMemberSubSignature) o;

    return Objects.equal(getName(), that.getName())
        && Objects.equal(getSignature(), that.getSignature());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), getSignature());
  }

  protected int compareTo(@Nonnull AbstractClassMemberSubSignature o) {
    int r = this.getName().compareTo(o.getName());

    if (r != 0) return r;

    return this.getSignature().toString().compareTo(o.getSignature().toString());
  }

  @Nonnull
  public abstract AbstractClassMemberSignature toFullSignature(
      @Nonnull JavaClassType declClassSignature);

  private final Lazy<String> _cachedToString =
      Utils.synchronizedLazy(() -> String.format("%s %s", getSignature(), getName()));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  // endregion /Methods/
}
