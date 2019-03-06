package de.upb.soot.signatures;

import com.google.common.base.Objects;
import de.upb.soot.core.SootClassMember;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 */
public abstract class AbstractClassMemberSignature implements ISignature {

  private final JavaClassSignature declClassSignature;

  private final TypeSignature typeSignature;
  private final String name;

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
    return "<" + declClassSignature.toString() + ": " + getSubSignature() + '>';
  }

  /** The signature of the declaring class. */
  public JavaClassSignature getDeclClassSignature() {
    return declClassSignature;
  }

  public TypeSignature getTypeSignature() {
    return typeSignature;
  }

  public String getName() {
    return name;
  }
}
