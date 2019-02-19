package de.upb.soot.jimple.common.constant;

import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.TypeSignature;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MethodType extends Constant {

  // FIXME: adapt this class

  private static final long serialVersionUID = 3523899677165980823L;
  protected TypeSignature returnType;
  protected List<TypeSignature> parameterTypes;

  private MethodType(List<TypeSignature> parameterTypes, TypeSignature returnType) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public static MethodType getInstance(List<TypeSignature> paramaterTypes, TypeSignature returnType) {
    return new MethodType(paramaterTypes, returnType);
  }

  public Type getType() {
    return RefType.getInstance("java.lang.invoke.MethodType");
  }

  public List<TypeSignature> getParameterTypes() {
    return parameterTypes == null ? Collections.emptyList() : parameterTypes;
  }

  public TypeSignature getReturnType() {
    return returnType;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Objects.hashCode(parameterTypes);
    result = 31 * result + Objects.hashCode(returnType);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    MethodType other = (MethodType) obj;
    return Objects.equals(returnType, other.returnType) && Objects.equals(parameterTypes, other.parameterTypes);
  }

  @Override
  public void accept(IVisitor v) {

  }

}
