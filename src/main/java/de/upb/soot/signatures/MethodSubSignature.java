package de.upb.soot.signatures;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import de.upb.soot.util.Utils;
import de.upb.soot.util.concurrent.Lazy;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Defines a method sub-signature, containing the method name, the parameter type signatures, and
 * the return type signature.
 *
 * @author Jan Martin Persch
 */
public class MethodSubSignature extends AbstractClassMemberSubSignature
    implements Comparable<MethodSubSignature> {
  // region Fields

  // endregion /Fields/

  // region Constructor

  /**
   * Creates a new instance of the {@link FieldSubSignature} class.
   *
   * @param name The method name.
   * @param parameterSignatures The signatures of the method parameters.
   * @param type The return type signature.
   */
  public MethodSubSignature(
      @Nonnull String name,
      @Nonnull Iterable<? extends Type> parameterSignatures,
      @Nonnull Type type) {
    super(name, type);

    this._parameterSignatures = ImmutableList.copyOf(parameterSignatures);
  }

  // endregion /Constructor/

  // region Properties

  @Nonnull private final List<Type> _parameterSignatures;

  /**
   * Gets the parameters in an immutable list.
   *
   * @return The value to get.
   */
  @Nonnull
  public List<Type> getParameterSignatures() {
    return this._parameterSignatures;
  }

  // endregion /Properties/

  // region Methods

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    if (!super.equals(o)) {
      return false;
    }

    MethodSubSignature that = (MethodSubSignature) o;

    return Objects.equal(getParameterSignatures(), that.getParameterSignatures());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), getParameterSignatures());
  }

  @Override
  public int compareTo(@Nonnull MethodSubSignature o) {
    return super.compareTo(o);
  }

  @Override
  @Nonnull
  public MethodSignature toFullSignature(@Nonnull JavaClassType declClassSignature) {
    return new MethodSignature(declClassSignature, this);
  }

  private final Lazy<String> _cachedToString =
      Utils.synchronizedLazy(
          () ->
              String.format(
                  "%s %s(%s)",
                  getSignature(),
                  getName(),
                  getParameterSignatures().stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","))));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  // endregion /Methods/
}
