/* Soot - a J*va Optimization Framework
 * Copyright (C) 2005 - Jennifer Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.constant;

import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.jimple.common.ref.FieldRef;
import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.Type;
import org.objectweb.asm.Opcodes;

public class MethodHandle implements Constant {

  public enum Kind {
    REF_GET_FIELD(Opcodes.H_GETFIELD, "REF_GET_FIELD"),
    REF_GET_FIELD_STATIC(Opcodes.H_GETSTATIC, "REF_GET_FIELD_STATIC"),
    REF_PUT_FIELD(Opcodes.H_PUTFIELD, "REF_PUT_FIELD"),
    REF_PUT_FIELD_STATIC(Opcodes.H_PUTSTATIC, "REF_PUT_FIELD_STATIC"),
    REF_INVOKE_VIRTUAL(Opcodes.H_INVOKEVIRTUAL, "REF_INVOKE_VIRTUAL"),
    REF_INVOKE_STATIC(Opcodes.H_INVOKESTATIC, "REF_INVOKE_STATIC"),
    REF_INVOKE_SPECIAL(Opcodes.H_INVOKESPECIAL, "REF_INVOKE_SPECIAL"),
    REF_INVOKE_CONSTRUCTOR(Opcodes.H_NEWINVOKESPECIAL, "REF_INVOKE_CONSTRUCTOR"),
    REF_INVOKE_INTERFACE(Opcodes.H_INVOKEINTERFACE, "REF_INVOKE_INTERFACE");

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
      throw new RuntimeException("Error: No methodRef handle kind for value '" + kind + "'.");
    }

    public static Kind getKind(String kind) {
      for (Kind k : Kind.values()) {
        if (k.toString().equals(kind)) {
          return k;
        }
      }
      throw new RuntimeException("Error: No methodRef handle kind for value '" + kind + "'.");
    }
  }

  /** */
  private static final long serialVersionUID = 76297846662243365L;

  private final MethodSignature methodRef;
  private final FieldRef fieldRef;

  public int tag;

  private MethodHandle(MethodSignature ref, int tag) {
    this.methodRef = ref;
    this.tag = tag;
    this.fieldRef = null;
  }

  private MethodHandle(FieldRef ref, int tag) {
    this.fieldRef = ref;
    this.tag = tag;
    this.methodRef = null;
  }

  public static MethodHandle getInstance(MethodSignature ref, int tag) {
    return new MethodHandle(ref, tag);
  }

  public static MethodHandle getInstance(FieldRef ref, int tag) {
    return new MethodHandle(ref, tag);
  }

  public static boolean isMethodRef(int kind) {
    return kind == Kind.REF_INVOKE_VIRTUAL.getValue()
        || kind == Kind.REF_INVOKE_STATIC.getValue()
        || kind == Kind.REF_INVOKE_SPECIAL.getValue()
        || kind == Kind.REF_INVOKE_CONSTRUCTOR.getValue()
        || kind == Kind.REF_INVOKE_INTERFACE.getValue();
  }

  @Override
  public String toString() {
    return "handle: " + methodRef;
  }

  @Override
  public Type getType() {
    return DefaultIdentifierFactory.getInstance().getType("java.lang.invoke.MethodHandle");
  }

  public MethodSignature getMethodRef() {
    return methodRef;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseMethodHandle(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((methodRef == null) ? 0 : methodRef.hashCode());
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
    if (methodRef == null) {
      return other.methodRef == null;
    } else {
      return methodRef.equals(other.methodRef);
    }
  }

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
