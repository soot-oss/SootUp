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


package de.upb.soot.jimple.ref;

import java.util.Collections;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.type.RefType;
import de.upb.soot.jimple.type.Type;
import de.upb.soot.jimple.visitor.IRefVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

public class ThisRef implements IdentityRef
{
    RefType thisType;

    public ThisRef(RefType thisType)
    {
        this.thisType = thisType;
    }

    public boolean equivTo(Object o)
    {
        if (o instanceof ThisRef)
        {
            return thisType.equals(((ThisRef)o).thisType);
        }
        return false;
    }

    public int equivHashCode()
    {
        return thisType.hashCode();
    }
    
    @Override
    public String toString()
    {
        return "@this: "+thisType;
    }
    
    public void toString( UnitPrinter up ) {
        up.identityRef(this);
    }

    public final List<ValueBox> getUseBoxes()
    {
        return Collections.emptyList();
    }

  @Override
  public Type getType()
    {
        return thisType;
    }

    public void accept(IVisitor sw)
    {
        ((IRefVisitor) sw).caseThisRef(this);
    }
    
    @Override
    public Object clone()
    {
        return new ThisRef(thisType);
    }

}
