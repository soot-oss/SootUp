package de.upb.soot.signatures;

import com.google.common.base.Objects;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/** Represents the fully qualified signature of a method. */
public class MethodSignature extends AbstractClassMemberSignature {

  /** The method's parameters' signatures. */
  public final List<TypeSignature> parameterSignatures;

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a {@link DefaultSignatureFactory}
   *
   * @param methodName
   *          the signature
   * @param declaringClass
   *          the declaring class signature
   */
  protected MethodSignature(final String methodName, final JavaClassSignature declaringClass, final TypeSignature returnType,
      final List<TypeSignature> parameters) {
    super(methodName, declaringClass, returnType);
    this.parameterSignatures = parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MethodSignature that = (MethodSignature) o;
    return Objects.equal(name, that.name) && Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(parameterSignatures, that.parameterSignatures)
        && Objects.equal(typeSignature, that.typeSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, declClassSignature, parameterSignatures);
  }

  /**
   * The simple name of the method; the method's name and its parameters.
   *
   * @return a String of the form "returnTypeName methodName(ParameterName(,)*)"
   */
  @Override
  public String getSubSignature() {
    StringBuilder sb = new StringBuilder();
    sb.append(typeSignature.toString());
    sb.append(' ');
    sb.append(name);
    sb.append('(');
    sb.append(StringUtils.join(parameterSignatures, ", "));
    sb.append(')');
    return sb.toString();
  }
}
