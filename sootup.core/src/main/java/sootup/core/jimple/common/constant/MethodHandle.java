package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2005-2020 Jennifer Lhotak, Andreas Dann, Linghui Luo and others
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

import javax.annotation.Nonnull;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;

public class MethodHandle implements Constant {

  private final Type type;

  public enum Kind {
    REF_GET_FIELD(1, "REF_GET_FIELD"),
    REF_GET_FIELD_STATIC(2, "REF_GET_FIELD_STATIC"),
    REF_PUT_FIELD(3, "REF_PUT_FIELD"),
    REF_PUT_FIELD_STATIC(4, "REF_PUT_FIELD_STATIC"),
    REF_INVOKE_VIRTUAL(5, "REF_INVOKE_VIRTUAL"),
    REF_INVOKE_STATIC(6, "REF_INVOKE_STATIC"),
    REF_INVOKE_SPECIAL(7, "REF_INVOKE_SPECIAL"),
    REF_INVOKE_CONSTRUCTOR(8, "REF_INVOKE_CONSTRUCTOR"),
    REF_INVOKE_INTERFACE(9, "REF_INVOKE_INTERFACE");

    private final int val;
    private final String valStr;

    Kind(int val, String valStr) {
      this.val = val;
      this.valStr = valStr;
    }

    @Override
    public String toString() {
      return valStr;
    }

    public int getValue() {
      return val;
    }

    public static Kind getKind(int kind) {
      for (Kind k : Kind.values()) {
        if (k.getValue() == kind) {
          return k;
        }
      }
      throw new RuntimeException("Error: No method handle kind for value '" + kind + "'.");
    }

    public static Kind getKind(String kind) {
      for (Kind k : Kind.values()) {
        if (k.toString().equals(kind)) {
          return k;
        }
      }
      throw new RuntimeException("Error: No method handle kind for value '" + kind + "'.");
    }
  }

  private final MethodSignature methodSignature;
  private final JFieldRef fieldRef;

  public int tag;

  public MethodHandle(MethodSignature methodSignature, int tag, Type type) {
    this.methodSignature = methodSignature;
    this.tag = tag;
    this.fieldRef = null;
    this.type = type;
  }

  public MethodHandle(JFieldRef ref, int tag, Type type) {
    this.fieldRef = ref;
    this.tag = tag;
    this.methodSignature = null;
    this.type = type;
  }

  public static boolean isMethodRef(int kind) {
    return kind == Kind.REF_INVOKE_VIRTUAL.getValue()
        || kind == Kind.REF_INVOKE_STATIC.getValue()
        || kind == Kind.REF_INVOKE_SPECIAL.getValue()
        || kind == Kind.REF_INVOKE_CONSTRUCTOR.getValue()
        || kind == Kind.REF_INVOKE_INTERFACE.getValue();
  }

  @Override
  // FIXME: [ms] serialize in a way it can be restored with the same parameters; adapt Jimple.g4 and
  // JimpleConverter.java
  public String toString() {
    return "handle: " + methodSignature;
  }

  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  public MethodSignature getMethodSignature() {
    return methodSignature;
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseMethodHandle(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((methodSignature == null) ? 0 : methodSignature.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MethodHandle other = (MethodHandle) obj;
    if (methodSignature == null) {
      return other.methodSignature == null;
    } else {
      return methodSignature.equals(other.methodSignature);
    }
  }
}
