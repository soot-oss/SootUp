package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler.typing;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

public class UntypedIntOrFloatConstant extends UntypedConstant {

  /** */
  private static final long serialVersionUID = 4413439694269487822L;

  public final int value;

  private UntypedIntOrFloatConstant(int value) {
    this.value = value;
  }

  public static UntypedIntOrFloatConstant v(int value) {
    return new UntypedIntOrFloatConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof UntypedIntOrFloatConstant
        && ((UntypedIntOrFloatConstant) c).value == this.value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  public FloatConstant toFloatConstant() {
    return FloatConstant.getInstance(Float.intBitsToFloat(value));
  }

  public IntConstant toIntConstant() {
    return IntConstant.getInstance(value);
  }

  @Override
  public Immediate defineType(Type t) {
    if (t instanceof PrimitiveType.FloatType) {
      return this.toFloatConstant();
    } else if (t instanceof PrimitiveType.IntType
        || t instanceof PrimitiveType.CharType
        || t instanceof PrimitiveType.BooleanType
        || t instanceof PrimitiveType.ByteType
        || t instanceof PrimitiveType.ShortType) {
      return this.toIntConstant();
    } else {
      if (value == 0 && t instanceof ReferenceType) {
        return NullConstant.getInstance();
      }
      // if the value is only used in a if to compare against another integer, then use default type
      // of integer
      if (t == null) {
        return this.toIntConstant();
      }
      throw new RuntimeException("error: expected Float type or Int-like type. Got " + t);
    }
  }

  @Override
  public void accept(@Nonnull ConstantVisitor constantVisitor) {}
}
