/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

package de.upb.soot.jimple.common.type;

import de.upb.soot.jimple.visitor.ITypeVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.TypeSignature;

/**
 * A class that models Java's sub types. AnySubTypes are parameterized by a super type called base.
 */
@SuppressWarnings("serial")
public class AnySubType extends RefLikeType {
  private RefType base;

  private AnySubType(RefType base) {
    this.base = base;
  }

  /**
   * Creates an AnySubType instance parameterized by a given super type.
   * 
   * @param base
   *          the super type
   * @return the AnySubType instance
   */
  public static AnySubType getInstance(RefType base) {
    if (base.getAnySubType() == null) {
      synchronized (base) {
        if (base.getAnySubType() == null) {
          base.setAnySubType(new AnySubType(base));
        }
      }
    }
    return base.getAnySubType();
  }

  @Override
  public String toString() {
    return "Any_subtype_of_" + base;
  }

  @Override
  public TypeSignature getTypeSignature() {
    // FIXME ... interesting
    return null;
  }

  @Override
  public Type getArrayElementType() {
    throw new RuntimeException("Attempt to get array base type of a non-array");
  }

  public RefType getBase() {
    return base;
  }

  public void setBase(RefType base) {
    this.base = base;
  }

  @Override
  public void accept(IVisitor v) {
    ((ITypeVisitor) v).caseAnySubType(this);
  }
}
