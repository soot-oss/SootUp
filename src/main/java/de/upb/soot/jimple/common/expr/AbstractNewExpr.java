/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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

package de.upb.soot.jimple.common.expr;

import java.util.Collections;
import java.util.List;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

@SuppressWarnings("serial")
public abstract class AbstractNewExpr implements Expr {
  RefType type;

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof AbstractNewExpr) {
      AbstractNewExpr ae = (AbstractNewExpr) o;
      return type.equals(ae.type);
    }
    return false;
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return type.hashCode();
  }

  @Override
  public abstract Object clone();

  @Override
  public String toString() {
    return Jimple.NEW + " " + type.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.NEW);
    up.literal(" ");
    up.type(type);
  }

  public RefType getBaseType() {
    return type;
  }

  public void setBaseType(RefType type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseNewExpr(this);
  }
}
