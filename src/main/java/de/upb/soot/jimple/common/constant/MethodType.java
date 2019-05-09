package de.upb.soot.jimple.common.constant;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.types.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MethodType implements Constant {

  // FIXME: adapt this class

  private static final long serialVersionUID = 3523899677165980823L;
  protected Type returnType;
  protected List<Type> parameterTypes;

  private MethodType(List<Type> parameterTypes, Type returnType) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public static MethodType getInstance(List<Type> paramaterTypes, Type returnType) {
    return new MethodType(paramaterTypes, returnType);
  }

  @Override
  public Type getType() {
    return DefaultIdentifierFactory.getInstance().getClassType("java.lang.invoke.MethodType");
  }

  public List<Type> getParameterTypes() {
    return parameterTypes == null ? Collections.emptyList() : parameterTypes;
  }

  public Type getReturnType() {
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
    return Objects.equals(returnType, other.returnType)
        && Objects.equals(parameterTypes, other.parameterTypes);
  }

  @Override
  public void accept(IVisitor v) {}

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
