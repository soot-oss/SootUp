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

package de.upb.soot.jimple.basic;

import de.upb.soot.Scene;
import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IJimpleValueVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.Collections;
import java.util.List;

public class Local implements Value, Numberable, Immediate {
  protected String name;
  Type type;

  /** Constructs a JimpleLocal of the given name and type. */
  public Local(String name, Type type) {
    setName(name);
    setType(type);
    List<Local> numberer = Scene.getInstance().getLocalNumberer();
    if (numberer != null) {
      numberer.add(this);
    }
  }

  /** Returns true if the given object is structurally equal to this one. */
  @Override
  public boolean equivTo(Object o) {
    return this.equals(o);
  }

  /**
   * Returns a hash code for this object, consistent with structural equality.
   */
  @Override
  public int equivHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /** Returns a clone of the current JimpleLocal. */
  @Override
  public Object clone() {
    // do not intern the name again
    Local local = new Local(null, type);
    local.name = name;
    return local;
  }

  /** Returns the name of this object. */
  public String getName() {
    return name;
  }

  /** Sets the name of this object as given. */
  public void setName(String name) {
    this.name = (name == null) ? null : name.intern();
  }

  /** Returns the type of this local. */
  @Override
  public Type getType() {
    return type;
  }

  /** Sets the type of this local. */
  public void setType(Type t) {
    this.type = t;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.local(this);
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IJimpleValueVisitor) sw).caseLocal(this);
  }

  @Override
  public final int getNumber() {
    return number;
  }

  @Override
  public final void setNumber(int number) {
    this.number = number;
  }

  private int number = 0;
}
