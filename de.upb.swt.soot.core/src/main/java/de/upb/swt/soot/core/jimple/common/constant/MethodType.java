package de.upb.swt.soot.core.jimple.common.constant;

import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MethodType implements Constant, Copyable {

  // FIXME: [AD] adapt this class
  private final Type returnType;
  private final List<Type> parameterTypes;
  private final Type type;

  public MethodType(List<Type> parameterTypes, Type returnType, Type type) {
    this.returnType = returnType;
    this.parameterTypes = Collections.unmodifiableList(parameterTypes);
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
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
    return new MethodType(parameterTypes, returnType, type);
  }

  @Nonnull
  public MethodType withReturnType(Type returnType) {
    return new MethodType(parameterTypes, returnType, type);
  }
}
