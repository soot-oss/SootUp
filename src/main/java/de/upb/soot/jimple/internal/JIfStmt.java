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
import java.util.Collections;
import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.stmt.IfStmt;
import de.upb.soot.jimple.stmt.Stmt;
import de.upb.soot.jimple.stmt.Unit;
import de.upb.soot.jimple.stmt.UnitBox;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

public class JIfStmt extends AbstractStmt implements IfStmt
{
    final ValueBox conditionBox;
    final UnitBox targetBox;

    final List<UnitBox> targetBoxes;

    public JIfStmt(Value condition, Unit target)
    {
        this(condition, Jimple.v().newStmtBox(target));
    }

    public JIfStmt(Value condition, UnitBox target)
    {
        this(Jimple.v().newConditionExprBox(condition), target);
    }

    protected JIfStmt(ValueBox conditionBox, UnitBox targetBox)
    {
        this.conditionBox = conditionBox;
        this.targetBox = targetBox;

        targetBoxes = Collections.singletonList(targetBox);
    }
    
    @Override
    public Object clone()
    {
        return new JIfStmt(Jimple.cloneIfNecessary(getCondition()), getTarget());
    }
    
    public String toString()
    {
        Unit t = getTarget();
        String target = "(branch)";
        if(!t.branches()) {
          target = t.toString();
        }
        return Jimple.IF + " "  + getCondition().toString() + " " + Jimple.GOTO + " "  + target;
    }
    
    @Override
    public void toString(UnitPrinter up) {
        up.literal(Jimple.IF);
        up.literal(" ");
        conditionBox.toString(up);
        up.literal(" ");
        up.literal(Jimple.GOTO);
        up.literal(" ");
        targetBox.toString(up);
    }
    
    @Override
    public Value getCondition()
    {
        return conditionBox.getValue();
    }
    
    @Override
    public void setCondition(Value condition)
    {
        conditionBox.setValue(condition);
    }

    @Override
    public ValueBox getConditionBox()
    {
        return conditionBox;
    }

    @Override
    public Stmt getTarget()
    {
        return (Stmt) targetBox.getUnit();
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
    public List<ValueBox> getUseBoxes()
    {
        List<ValueBox> useBoxes = new ArrayList<ValueBox>();

        useBoxes.addAll(conditionBox.getValue().getUseBoxes());
        useBoxes.add(conditionBox);

        return useBoxes;
    }

    @Override
    public final List<UnitBox> getUnitBoxes()
    {
        return targetBoxes;
    }

    @Override
    public void accept(IVisitor sw)
    {
        ((IStmtVisitor) sw).caseIfStmt(this);
    }    


    @Override
    public boolean fallsThrough(){return true;}        
    @Override
    public boolean branches(){return true;}

}
