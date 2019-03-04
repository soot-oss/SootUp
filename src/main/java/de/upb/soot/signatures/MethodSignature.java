package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
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

/** Represents the fully qualified signature of a methodRef. */
public class MethodSignature extends AbstractClassMemberSignature {

  /** The methodRef's parameters' signatures. */
  public final List<TypeSignature> parameterSignatures;

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a {@link
   * DefaultSignatureFactory}
   *
   * @param methodName the signature
   * @param declaringClass the declaring class signature
   */
  protected MethodSignature(
      final String methodName,
      final JavaClassSignature declaringClass,
      final TypeSignature returnType,
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
    return Objects.equal(name, that.name)
        && Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(parameterSignatures, that.parameterSignatures)
        && Objects.equal(typeSignature, that.typeSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, declClassSignature, parameterSignatures);
  }

  /**
   * The simple name of the methodRef; the methodRef's name and its parameters.
   *
   * @return a String of the form "returnTypeName methodName(ParameterName(,)*)"
   */
  @Override
  public String getSubSignature() {
    return typeSignature.toString()
        + ' '
        + name
        + '('
        + StringUtils.join(parameterSignatures, ", ")
        + ')';
  }
}
