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

package de.upb.soot.jimple.internal;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.StmtSwitch;
import de.upb.soot.jimple.IVisitor;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.stmt.IdentityStmt;
import de.upb.soot.jimple.type.Type;

public class JIdentityStmt extends AbstractDefinitionStmt implements
		IdentityStmt {
	public JIdentityStmt(Value local, Value identityValue) {
		this(Jimple.v().newLocalBox(local), Jimple.v().newIdentityRefBox(
				identityValue));
	}

	protected JIdentityStmt(ValueBox localBox, ValueBox identityValueBox) {
		super(localBox, identityValueBox);
	}

	@Override
  public Object clone() {
		return new JIdentityStmt(Jimple.cloneIfNecessary(getLeftOp()),
				Jimple.cloneIfNecessary(getRightOp()));
	}

	@Override
  public String toString() {
		return leftBox.getValue().toString() + " := "
				+ rightBox.getValue().toString();
	}

	@Override
  public void toString(UnitPrinter up) {
		leftBox.toString(up);
		up.literal(" := ");
		rightBox.toString(up);
	}

	public void setLeftOp(Value local) {
		leftBox.setValue(local);
	}

	public void setRightOp(Value identityRef) {
		rightBox.setValue(identityRef);
	}

	@Override
  public void accept(IVisitor sw) {
		((StmtSwitch) sw).caseIdentityStmt(this);
	}

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean equivTo(Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int equivHashCode() {
    // TODO Auto-generated method stub
    return 0;
  }

}
