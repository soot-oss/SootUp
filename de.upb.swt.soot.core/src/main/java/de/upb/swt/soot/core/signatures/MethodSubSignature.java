package de.upb.swt.soot.core.signatures;

import com.google.common.base.Objects;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Iterator;
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

  @Nonnull private final List<Type> parameterTypes;

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

  /**
   * Gets the parameters in an immutable list.
   *
   * @return The value to get.
   */
  @Nonnull
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }

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

  private final Supplier<String> _cachedToString =
      Suppliers.memoize(
          () ->
              getType()
                  + " "
                  + getName()
                  + "("
                  + getParameterTypes().stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(","))
                  + ")");

  @Override
  @Nonnull
  public String toString() {
    return _cachedToString.get();
  }

  @Override
  public void toString(StmtPrinter printer) {
    printer.typeSignature(getType());
    printer.literal(" ");
    printer.literal(Jimple.escape(getName()));
    printer.literal("(");

    Iterator<Type> it = getParameterTypes().iterator();
    if (it.hasNext()) {
      printer.typeSignature(it.next());
      while (it.hasNext()) {
        printer.literal(",");
        printer.typeSignature(it.next());
      }
    }

    printer.literal(")");
  }
}
