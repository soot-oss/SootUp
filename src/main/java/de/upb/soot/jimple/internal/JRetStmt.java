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
import de.upb.soot.jimple.StmtSwitch;
import de.upb.soot.jimple.IVisitor;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.stmt.RetStmt;

public class JRetStmt extends AbstractStmt implements RetStmt
{
    final ValueBox stmtAddressBox;
    //List useBoxes;

    public JRetStmt(Value stmtAddress)
    {
        this(Jimple.v().newLocalBox(stmtAddress));
    }

    protected JRetStmt(ValueBox stmtAddressBox)
    {
            this.stmtAddressBox = stmtAddressBox;

    }

    @Override
    public Object clone() 
    {
        return new JRetStmt(Jimple.cloneIfNecessary(getStmtAddress()));
    }

    public String toString()
    {
        return Jimple.RET + " "  + stmtAddressBox.getValue().toString();
    }
    
    @Override
    public void toString(UnitPrinter up) {
        up.literal(Jimple.RET);
        up.literal(" ");
        stmtAddressBox.toString(up);
    }

    @Override
    public Value getStmtAddress()
    {
        return stmtAddressBox.getValue();
    }

    @Override
    public ValueBox getStmtAddressBox()
    {
        return stmtAddressBox;
    }

    @Override
    public void setStmtAddress(Value stmtAddress)
    {
        stmtAddressBox.setValue(stmtAddress);
    }

    @Override
    public List<ValueBox> getUseBoxes()
    {
        List<ValueBox> useBoxes = new ArrayList<ValueBox>();

        useBoxes.addAll(stmtAddressBox.getValue().getUseBoxes());
        useBoxes.add(stmtAddressBox);

        return useBoxes;
    }

    @Override
    public void accept(IVisitor sw)
    {
        ((StmtSwitch) sw).caseRetStmt(this);
    }    

    @Override
    public boolean fallsThrough(){return true;}        
    @Override
    public boolean branches(){return false;}


}
