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
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.common.ref.CaughtExceptionRef;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IRefVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

public class JCaughtExceptionRef implements CaughtExceptionRef
{
    public JCaughtExceptionRef()
    {
    }
    
    @Override
    public boolean equivTo(Object c)
    {
        return c instanceof CaughtExceptionRef;
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    @Override
    public int equivHashCode() 
    {
        return 1729;
    }

    @Override
    public Object clone() 
    {
        return new JCaughtExceptionRef();
    }

    @Override
    public String toString()
    {
        return "@caughtexception";
    }
    
    public void toString(StmtPrinter up)
    {
        up.identityRef(this);
    }

    @Override
    public final List<ValueBox> getUseBoxes()
    {
        return Collections.emptyList();
    }

    @Override
    public Type getType()
    {
        return RefType.v("java.lang.Throwable");
    }

    @Override
    public void accept(IVisitor sw)
    {
        ((IRefVisitor) sw).caseCaughtExceptionRef(this);
    }
}
