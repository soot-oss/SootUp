package de.upb.soot.jimple.common.constant;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.types.Type;
import de.upb.soot.util.Copyable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MethodType implements Constant, Copyable {

  // FIXME: adapt this class
  private final Type returnType;
  private final List<Type> parameterTypes;

  private MethodType(List<Type> parameterTypes, Type returnType) {
    this.returnType = returnType;
    this.parameterTypes = Collections.unmodifiableList(parameterTypes);
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
  public void accept(Visitor v) {}

  @Nonnull
  public MethodType withParameterTypes(List<Type> parameterTypes) {
    return new MethodType(parameterTypes, returnType);
  }

  @Nonnull
  public MethodType withReturnType(Type returnType) {
    return new MethodType(parameterTypes, returnType);
  }
}
