package de.upb.swt.soot.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2005-2020 Jennifer Lhotak, Andreas Dann, Christian Brüggemann and others
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

  public MethodType(
      @Nonnull List<Type> parameterTypes, @Nonnull Type returnType, @Nonnull Type type) {
    this.returnType = returnType;
    this.parameterTypes = Collections.unmodifiableList(parameterTypes);
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  public List<Type> getParameterTypes() {
    return Collections.emptyList();
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
  public void accept(@Nonnull Visitor v) {}

  @Nonnull
  public MethodType withParameterTypes(@Nonnull List<Type> parameterTypes) {
    return new MethodType(parameterTypes, returnType, type);
  }

  @Nonnull
  public MethodType withReturnType(@Nonnull Type returnType) {
    return new MethodType(parameterTypes, returnType, type);
  }
}
