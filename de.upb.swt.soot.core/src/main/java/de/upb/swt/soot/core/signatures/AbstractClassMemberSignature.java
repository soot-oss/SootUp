package de.upb.swt.soot.core.signatures;

import com.google.common.base.Objects;
import de.upb.swt.soot.core.model.SootClassMember;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class AbstractClassMemberSignature implements Signature {

  /** The signature of the declaring class. */
  @Nonnull private final ClassType declClassSignature;

  @Nonnull private final AbstractClassMemberSubSignature subSignature;

  public AbstractClassMemberSignature(
      @Nonnull ClassType klass, @Nonnull AbstractClassMemberSubSignature subSignature) {
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
  public ClassType getDeclClassType() {
    return declClassSignature;
  }

  public Type getType() {
    return subSignature.getType();
  }

  public String getName() {
    return subSignature.getName();
  }
}
