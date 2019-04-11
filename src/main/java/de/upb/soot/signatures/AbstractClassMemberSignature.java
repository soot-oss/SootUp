package de.upb.soot.signatures;

import com.google.common.base.Objects;
import de.upb.soot.core.SootClassMember;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import javax.annotation.Nonnull;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class AbstractClassMemberSignature implements ISignature {

  /** The signature of the declaring class. */
  @Nonnull private final JavaClassType declClassSignature;

  @Nonnull private final AbstractClassMemberSubSignature subSignature;

  public AbstractClassMemberSignature(
      @Nonnull JavaClassType klass, @Nonnull AbstractClassMemberSubSignature subSignature) {
    this.declClassSignature = klass;
    this.subSignature = subSignature;
  }

  @Nonnull
  public AbstractClassMemberSubSignature getSubSignature() {
    return subSignature;
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

  /** The signature of the declaring class. */
  @Nonnull
  public JavaClassType getDeclClassSignature() {
    return declClassSignature;
  }

  public Type getSignature() {
    return subSignature.getSignature();
  }

  public String getName() {
    return subSignature.getName();
  }

  // FIXME: [JMP] Implement quotation
  @Nonnull
  public String toQuotedString() {
    return this.toString();
  }
}
