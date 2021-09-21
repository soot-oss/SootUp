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
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;

public class UntypedLongOrDoubleConstant extends UntypedConstant {

  /**
  * 
  */
  private static final long serialVersionUID = -3970057807907204253L;
  public final long value;

  private UntypedLongOrDoubleConstant(long value) {
    this.value = value;
  }

  public static UntypedLongOrDoubleConstant v(long value) {
    return new UntypedLongOrDoubleConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof UntypedLongOrDoubleConstant && ((UntypedLongOrDoubleConstant) c).value == this.value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  public DoubleConstant toDoubleConstant() {
    return DoubleConstant.getInstance(Double.longBitsToDouble(value));
  }

  public LongConstant toLongConstant() {
    return LongConstant.getInstance(value);
  }

  @Override
  public Immediate defineType(Type t) {
    if (t instanceof PrimitiveType.DoubleType) {
      return this.toDoubleConstant();
    } else if (t instanceof PrimitiveType.LongType) {
      return this.toLongConstant();
    } else {
      throw new RuntimeException("error: expected Double type or Long type. Got " + t);
    }
  }

  @Override
  public void accept(@Nonnull ConstantVisitor constantVisitor) {

  }
}
