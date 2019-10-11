package de.upb.swt.soot.core.signatures;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClassMember;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class AbstractClassMemberSignature implements Signature {

  /** The signature of the declaring class. */
  @Nonnull private final JavaClassType declClassSignature;

  @Nonnull private final AbstractClassMemberSubSignature subSignature;
  @Nonnull private final ImmutableSet<Modifier> modifiers;

  public AbstractClassMemberSignature(
      @Nonnull JavaClassType klass, @Nonnull AbstractClassMemberSubSignature subSignature) {
    this(klass, subSignature, EnumSet.noneOf(Modifier.class));
  }

  public AbstractClassMemberSignature(
      @Nonnull JavaClassType klass,
      @Nonnull AbstractClassMemberSubSignature subSignature,
      EnumSet<Modifier> modifiers) {
    this.declClassSignature = klass;
    this.subSignature = subSignature;
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
  }

  @Nonnull
  public AbstractClassMemberSubSignature getSubSignature() {
    return subSignature;
  }

  /** The signature of the declaring class. */
  @Nonnull
  public JavaClassType getDeclClassType() {
    return declClassSignature;
  }

  public Type getType() {
    return subSignature.getType();
  }

  public String getName() {
    return subSignature.getName();
  }

  /**
   * Gets the modifiers of this class member in an immutable set.
   *
   * @see Modifier
   */
  @Nonnull
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractClassMemberSignature that = (AbstractClassMemberSignature) o;
    return Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(subSignature, that.subSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(declClassSignature, subSignature);
  }

  @Override
  public String toString() {
    return "<" + declClassSignature.toString() + ": " + getSubSignature() + '>';
  }
}
