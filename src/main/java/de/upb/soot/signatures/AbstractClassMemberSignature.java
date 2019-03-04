package de.upb.soot.signatures;

import com.google.common.base.Objects;
import de.upb.soot.core.SootClassMember;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 */
public abstract class AbstractClassMemberSignature implements ISignature {

  /** The signature of the declaring class. */
  public final JavaClassSignature declClassSignature;

  public final TypeSignature typeSignature;
  public final String name;

  public AbstractClassMemberSignature(
      String name, JavaClassSignature klass, TypeSignature returnType) {
    this.name = name;
    this.declClassSignature = klass;
    this.typeSignature = returnType;
  }

  public abstract String getSubSignature();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractClassMemberSignature that = (AbstractClassMemberSignature) o;
    return Objects.equal(name, that.name)
        && Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(typeSignature, that.typeSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, declClassSignature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    sb.append(declClassSignature.toString());
    sb.append(": ");
    sb.append(getSubSignature());
    sb.append('>');
    return sb.toString();
  }
}
