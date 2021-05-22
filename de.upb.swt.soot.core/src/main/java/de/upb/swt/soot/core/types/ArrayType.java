package de.upb.swt.soot.core.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Andreas Dann, Markus Schmidt and others
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
import de.upb.swt.soot.core.jimple.visitor.TypeVisitor;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import javax.annotation.Nonnull;

/**
 * Represents the type of an array, e.g. <code>int[]</code>, <code>Object[]</code> or <code>
 * String[][]</code>
 */
public class ArrayType extends ReferenceType {

  private final Type baseType;

  private final int dimension;

  public ArrayType(Type baseType, int dimension) {
    this.baseType = baseType;
    this.dimension = dimension;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(baseType);
    for (int i = 0; i < dimension; i++) {
      sb.append("[]");
    }
    return sb.toString();
  }

  public void toString(StmtPrinter printer) {
    printer.typeSignature(baseType);
    for (int i = 0; i < dimension; i++) {
      printer.literal("[]");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArrayType that = (ArrayType) o;
    return dimension == that.dimension && Objects.equal(baseType, that.baseType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(baseType, dimension);
  }

  public Type getBaseType() {
    return baseType;
  }

  public int getDimension() {
    return dimension;
  }

  @Override
  public void accept(@Nonnull TypeVisitor v) {
    v.caseArrayType(this);
  }
}
