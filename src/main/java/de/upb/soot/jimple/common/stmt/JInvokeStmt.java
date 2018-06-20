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

import java.util.ArrayList;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.expr.InvokeExpr;
import de.upb.soot.jimple.stmt.InvokeStmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

public class JInvokeStmt extends AbstractStmt implements InvokeStmt
{
    final ValueBox invokeExprBox;

    public JInvokeStmt(Value c)
    {
        this(Jimple.v().newInvokeExprBox(c));
    }

    protected JInvokeStmt(ValueBox invokeExprBox)
    {
        this.invokeExprBox = invokeExprBox;
    }

 
    @Override
    public Object clone() 
    {
        return new JInvokeStmt(Jimple.cloneIfNecessary(getInvokeExpr()));
    }

    @Override
    public boolean containsInvokeExpr()
    {
        return true;
    }

    public String toString()
    {
        return invokeExprBox.getValue().toString();
    }
    
    @Override
    public void toString(UnitPrinter up) {
        invokeExprBox.toString(up);
    }
    
    @Override
    public void setInvokeExpr(Value invokeExpr)
    {
        invokeExprBox.setValue(invokeExpr);
    }

    @Override
    public InvokeExpr getInvokeExpr()
    {
        return (InvokeExpr) invokeExprBox.getValue();
    }

    @Override
    public ValueBox getInvokeExprBox()
    {
        return invokeExprBox;
    }

    @Override
    public List<ValueBox> getUseBoxes()
    {
        List<ValueBox> list = new ArrayList<ValueBox>();

        list.addAll(invokeExprBox.getValue().getUseBoxes());
        list.add(invokeExprBox);

        return list;
    }

    @Override
    public void accept(IVisitor sw)
    {
        ((IStmtVisitor) sw).caseInvokeStmt(this);
    }
   

    @Override
    public boolean fallsThrough() {return true;}
    @Override
    public boolean branches() {return false;}

}
