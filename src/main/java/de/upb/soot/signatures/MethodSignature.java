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

import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents the fully qualified signature of a methodRef. */
public class MethodSignature extends AbstractClassMemberSignature {

  protected MethodSignature(
      JavaClassType declaringClassSignature,
      String methodName,
      Iterable<Type> parameters,
      Type fqReturnType) {
    this(declaringClassSignature, new MethodSubSignature(methodName, parameters, fqReturnType));
  }

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a {@link
   * DefaultSignatureFactory}
   *
   * @param declaringClass the declaring class signature
   * @param subSignature the sub-signature
   */
  protected MethodSignature(
      final @Nonnull JavaClassType declaringClass, final @Nonnull MethodSubSignature subSignature) {
    super(declaringClass, subSignature);

    this._subSignature = subSignature;
  }

  private final @Nonnull MethodSubSignature _subSignature;

  @Override
  @Nonnull
  public MethodSubSignature getSubSignature() {
    return _subSignature;
  }

  /** The methodRef's parameters' signatures. */
  @Nonnull
  public List<Type> getParameterSignatures() {
    return this.getSubSignature().getParameterSignatures();
  }
}
