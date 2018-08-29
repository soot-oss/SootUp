package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 1997 - 2018 Secure Software Engineering Department, University of Paderborn
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.base.Objects;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/** Represents the fully qualified signature of a method. */
public class MethodSignature {
  /** The method's signature. */
  public final String methodName;

  /** The signature of the declaring class. */
  public final ClassSignature declClassSignature;

  /** The method's parameters' signatures. */
  public final List<TypeSignature> parameterSignatures;

  /** The return type's signature. */
  public final TypeSignature returnTypeSignature;

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a {@link SignatureFactory}
   *
   * @param methodName
   *          the signature
   * @param declaringClass
   *          the declaring class signature
   */
  protected MethodSignature(final String methodName, final ClassSignature declaringClass, final TypeSignature returnType,
      final List<TypeSignature> parameters) {
    this.methodName = methodName;
    this.declClassSignature = declaringClass;
    this.parameterSignatures = parameters;
    this.returnTypeSignature = returnType;
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
    return Objects.equal(methodName, that.methodName) && Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(parameterSignatures, that.parameterSignatures)
        && Objects.equal(returnTypeSignature, that.returnTypeSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(methodName, declClassSignature, parameterSignatures, returnTypeSignature);
  }

  /**
   * The simple name of the method; the method's name and its parameters.
   *
   * @return a String of the form "returnTypeName methodName(ParameterName(,)*)"
   */
  public String getSimpleMethodSignature() {
    StringBuilder sb = new StringBuilder();
    sb.append(returnTypeSignature.toString());
    sb.append(' ');
    sb.append(methodName);
    sb.append('(');
    sb.append(StringUtils.join(parameterSignatures, ','));
    sb.append(')');
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    sb.append(declClassSignature.toString());
    sb.append(':');
    sb.append(getSimpleMethodSignature());
    sb.append('>');
    return sb.toString();
  }
}
