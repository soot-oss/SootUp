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

import java.util.Collections;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.core.Unit;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.StmtSwitch;
import de.upb.soot.jimple.IVisitor;
import de.upb.soot.jimple.UnitBox;
import de.upb.soot.jimple.stmt.GotoStmt;

public class JGotoStmt extends AbstractStmt implements GotoStmt
{
    final UnitBox targetBox;
    final List<UnitBox> targetBoxes;

    public JGotoStmt(Unit target)
    {
        this(Jimple.v().newStmtBox(target));
    }

    public JGotoStmt(UnitBox box)
    {
        targetBox = box;
        targetBoxes = Collections.singletonList(box);
    }

    @Override
    public Object clone() 
    {
        return new JGotoStmt(getTarget());
    }

    public String toString()
    {
        Unit t = getTarget();
        String target = "(branch)";
        if(!t.branches()) {
          target = t.toString();
        }
        return Jimple.GOTO + " [?= " + target + "]";
    }
    
    @Override
    public void toString(UnitPrinter up) {
        up.literal(Jimple.GOTO);
        up.literal(" ");
        targetBox.toString(up);
    }
    
    @Override
    public Unit getTarget()
    {
        return targetBox.getUnit();
    }

    @Override
    public void setTarget(Unit target)
    {
        targetBox.setUnit(target);
    }

    @Override
    public UnitBox getTargetBox()
    {
        return targetBox;
    }

    @Override
    public List<UnitBox> getUnitBoxes()
    {
        return targetBoxes;
    }

    @Override
    public void accept(IVisitor sw)
    {
        ((StmtSwitch) sw).caseGotoStmt(this);
    }    
    
    @Override
    public boolean fallsThrough(){return false;}        
    @Override
    public boolean branches() { return true;}
    
}



