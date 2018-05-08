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






package de.upb.soot.jimple;

import java.util.Collections;
import java.util.List;

import de.upb.soot.basic.AbstractStmt;
import de.upb.soot.basic.IfStmt;
import de.upb.soot.basic.Stmt;
import de.upb.soot.basic.Unit;
import de.upb.soot.basic.UnitBox;
import de.upb.soot.basic.Value;
import de.upb.soot.basic.ValueBox;

public class JIfStmt extends AbstractStmt implements IfStmt
{
    final ValueBox conditionBox;
    final UnitBox targetBox;

    final List<UnitBox> targetBoxes;

    protected JIfStmt(ValueBox conditionBox, UnitBox targetBox)
    {
        this.conditionBox = conditionBox;
        this.targetBox = targetBox;

        targetBoxes = Collections.singletonList(targetBox);
    }
    

    
    public Value getCondition()
    {
        return conditionBox.getValue();
    }
    
    public void setCondition(Value condition)
    {
        conditionBox.setValue(condition);
    }

    public ValueBox getConditionBox()
    {
        return conditionBox;
    }

    public Stmt getTarget()
    {
        return (Stmt) targetBox.getUnit();
    }

    public void setTarget(Unit target)
    {
        targetBox.setUnit(target);
    }

    public UnitBox getTargetBox()
    {
        return targetBox;
    }
    
    public boolean fallsThrough(){return true;}        
    public boolean branches(){return true;}

}
