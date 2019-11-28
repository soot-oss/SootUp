package de.upb.swt.soot.core.signatures;

import com.google.common.base.Objects;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.List;
import java.util.function.Supplier;
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
   * @param parameterTypes The signatures of the method parameters.
   * @param type The return type signature.
   */
  public MethodSubSignature(
      @Nonnull String name, @Nonnull Iterable<? extends Type> parameterTypes, @Nonnull Type type) {
    super(name, type);

    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }

  // endregion /Constructor/

  // region Properties

  @Nonnull private final List<Type> parameterTypes;

  /**
   * Gets the parameters in an immutable list.
   *
   * @return The value to get.
   */
  @Nonnull
  public List<Type> getParameterTypes() {
    return this.parameterTypes;
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

    return Objects.equal(getParameterTypes(), that.getParameterTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), getParameterTypes());
  }

  @Override
  public int compareTo(@Nonnull MethodSubSignature o) {
    return super.compareTo(o);
  }

  @Override
  @Nonnull
  public MethodSignature toFullSignature(@Nonnull ClassType declClassSignature) {
    return new MethodSignature(declClassSignature, this);
  }

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(
          () ->
              String.format(
                  "%s %s(%s)",
                  getType(),
                  getName(),
                  getParameterTypes().stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","))));

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  // endregion /Methods/
}
