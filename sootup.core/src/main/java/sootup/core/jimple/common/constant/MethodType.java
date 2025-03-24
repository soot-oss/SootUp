package sootup.core.jimple.common.constant;

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

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class MethodType implements Constant {

  // FIXME: [AD] adapt this class
  private final Type type;
  private final MethodSubSignature methodSig;

  public MethodType(@NonNull MethodSubSignature methodSubSignature, @NonNull ClassType type) {
    this.methodSig = methodSubSignature;
    this.type = type;
  }

  @NonNull
  @Override
  public Type getType() {
    return type;
  }

  public List<Type> getParameterTypes() {
    return methodSig.getParameterTypes();
  }

  public Type getReturnType() {
    return methodSig.getType();
  }

  @Override
  public String toString() {
    return "methodtype: " + methodSig;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Objects.hashCode(type);
    result = 31 * result + Objects.hashCode(methodSig);
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
    return Objects.equals(methodSig, other.methodSig);
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull V v) {
    v.caseMethodType(this);
    return v;
  }
}
