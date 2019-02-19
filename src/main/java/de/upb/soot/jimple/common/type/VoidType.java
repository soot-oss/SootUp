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
import de.upb.soot.signatures.VoidTypeSignature;

/** Represents the Java void type. */
@SuppressWarnings("serial")
public class VoidType extends Type {

  public static final VoidType INSTANCE = new VoidType();

  private VoidType() {
  }

  @Override
  public int hashCode() {
    return 0x3A8C1035;
  }

  @Override
  public boolean equals(Object t) {
    return this == t;
  }

  @Override
  public String toString() {
    return VoidTypeSignature.VOID_TYPE_SIGNATURE.toString();
  }

  @Override
  public boolean isAllowedInFinalCode() {
    return true;
  }

  @Override
  public TypeSignature getTypeSignature() {
    return VoidTypeSignature.VOID_TYPE_SIGNATURE;
  }

  @Override
  public void accept(IVisitor v) {
    ((ITypeVisitor) v).caseVoidType(this);
  }
}
