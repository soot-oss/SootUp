package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2005-2023 Jennifer Lhotak, Andreas Dann, Linghui, Luo Jonas Klauke and others
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.signatures.SootClassMemberSubSignature;
import sootup.core.types.Type;

public class MethodHandle implements Constant {

  @NonNull private final Type type;

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

    public String getValueName() {
      return valStr;
    }

    public static Kind getKind(int kind) {
      for (Kind k : Kind.values()) {
        if (k.getValue() == kind) {
          return k;
        }
      }
      throw new RuntimeException("Error: No method handle kind for value '" + kind + "'.");
    }

    public static Kind getKind(String kindName) {
      for (Kind k : Kind.values()) {
        if (k.getValueName().equals(kindName)) {
          return k;
        }
      }
      throw new RuntimeException("Error: No method handle kind for value name '" + kindName + "'.");
    }
  }

  @NonNull
  private final SootClassMemberSignature<? extends SootClassMemberSubSignature> referenceSignature;

  @NonNull private final Kind kind;

  public MethodHandle(
      @NonNull SootClassMemberSignature<? extends SootClassMemberSubSignature> referenceSignature,
      int tag,
      @NonNull Type type) {
    this(referenceSignature, Kind.getKind(tag), type);
  }

  public MethodHandle(
      @NonNull SootClassMemberSignature<? extends SootClassMemberSubSignature> referenceSignature,
      MethodHandle.@NonNull Kind kind,
      @NonNull Type type) {
    this.kind = kind;
    this.type = type;
    this.referenceSignature = referenceSignature;
    if ((this.isMethodRef() && !(referenceSignature instanceof MethodSignature))
        || (this.isFieldRef() && !(referenceSignature instanceof FieldSignature))) {
      throw new IllegalArgumentException(
          "Kind:"
              + kind.valStr
              + " does not match with the given signature:"
              + referenceSignature.getClass());
    }
  }

  public static boolean isMethodRef(int tag) {
    return tag == Kind.REF_INVOKE_VIRTUAL.getValue()
        || tag == Kind.REF_INVOKE_STATIC.getValue()
        || tag == Kind.REF_INVOKE_SPECIAL.getValue()
        || tag == Kind.REF_INVOKE_CONSTRUCTOR.getValue()
        || tag == Kind.REF_INVOKE_INTERFACE.getValue();
  }

  public static boolean isFieldRef(int tag) {
    return tag == Kind.REF_GET_FIELD.getValue()
        || tag == Kind.REF_PUT_FIELD.getValue()
        || tag == Kind.REF_PUT_FIELD_STATIC.getValue()
        || tag == Kind.REF_GET_FIELD_STATIC.getValue();
  }

  public boolean isMethodRef() {
    return MethodHandle.isMethodRef(this.kind.getValue());
  }

  public boolean isFieldRef() {
    return MethodHandle.isFieldRef(this.kind.getValue());
  }

  @Override
  public String toString() {
    return "methodhandle: \"" + kind.valStr + "\" " + referenceSignature;
  }

  @NonNull
  @Override
  public Type getType() {
    return type;
  }

  public Kind getKind() {
    return kind;
  }

  public SootClassMemberSignature<? extends SootClassMemberSubSignature> getReferenceSignature() {
    return referenceSignature;
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull V v) {
    v.caseMethodHandle(this);
    return v;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + referenceSignature.hashCode();
    result = 31 * result + kind.hashCode();
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
    return referenceSignature.equals(other.referenceSignature);
  }
}
