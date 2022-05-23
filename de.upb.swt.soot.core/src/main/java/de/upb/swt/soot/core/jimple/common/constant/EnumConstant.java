package de.upb.swt.soot.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Jonas Klauke
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

import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

public class EnumConstant implements Constant {
  private final String value;
  private final ClassType type;
  private final FieldSignature signature;

  public EnumConstant(@Nonnull String value, @Nonnull ClassType type) {
    this.value = value;
    this.type = type;
    this.signature = new FieldSignature(type, value, type);
  }

  @Override
  public boolean equals(Object c) {
    return (c instanceof EnumConstant
        && ((EnumConstant) c).value.equals(value)
        && ((EnumConstant) c).type.equals(type));
  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  public String getValue() {
    return value;
  }

  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    //    v.caseClassConstant(this);
  }

  @Override
  public String toString() {
    return signature.toString();
  }
}
