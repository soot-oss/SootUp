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

import java.util.List;

import de.upb.soot.UnitPrinter;
import de.upb.soot.core.Unit;
import de.upb.soot.core.UnitBox;
import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.StmtSwitch;
import de.upb.soot.jimple.Switch;
import de.upb.soot.jimple.TableSwitchStmt;

public class JTableSwitchStmt extends AbstractSwitchStmt 
    implements TableSwitchStmt
{
    int lowIndex;
    int highIndex;
    
    // This method is necessary to deal with constructor-must-be-first-ism.
    private static UnitBox[] getTargetBoxesArray(List<? extends Unit> targets)
    {
        UnitBox[] targetBoxes = new UnitBox[targets.size()];
        for(int i = 0; i < targetBoxes.length; i++) {
          targetBoxes[i] = Jimple.v().newStmtBox(targets.get(i));
        }
        return targetBoxes;
    }
    
    @Override
    public Object clone() 
    {
        return new JTableSwitchStmt(Jimple.cloneIfNecessary(getKey()), 
        		lowIndex, highIndex, getTargets(), getDefaultTarget() );
    }
    
    public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends Unit> targets, Unit defaultTarget)
    {
        this(Jimple.v().newImmediateBox(key), lowIndex, highIndex, 
        		getTargetBoxesArray(targets),
        		Jimple.v().newStmtBox(defaultTarget)
             );
    }  
    
    public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends UnitBox> targets, UnitBox defaultTarget)
    {
        this(Jimple.v().newImmediateBox(key), lowIndex, highIndex, 
        		targets.toArray(new UnitBox[targets.size()]), defaultTarget
             );
    }

    protected JTableSwitchStmt(ValueBox keyBox, int lowIndex, int highIndex, 
    		UnitBox[] targetBoxes, UnitBox defaultTargetBox )
    {
    	super(keyBox, defaultTargetBox, targetBoxes);

        if(lowIndex > highIndex) {
          throw new RuntimeException("Error creating tableswitch: lowIndex(" 
                                     + lowIndex +  ") can't be greater than highIndex(" + highIndex + ").");
        }

        this.lowIndex = lowIndex;
        this.highIndex = highIndex;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        String endOfLine = " ";
        
        buffer.append(Jimple.TABLESWITCH + "(" +
            keyBox.getValue().toString() + ")" + endOfLine);
            
        buffer.append("{" + endOfLine);
        
        // In this for-loop, we cannot use "<=" since 'i' would wrap around.
        // The case for "i == highIndex" is handled separately after the loop.
        for (int i = lowIndex; i < highIndex; i++) {
          Unit target = getTarget(i - lowIndex);
          buffer.append("    " + Jimple.CASE + " " + i + ": " +
              Jimple.GOTO + " " + (target == this ? "self" : target) + ";" +
              endOfLine);
        }
        Unit target = getTarget(highIndex - lowIndex);
        buffer.append("    " + Jimple.CASE + " " + highIndex + ": " +
            Jimple.GOTO + " " + (target == this ? "self" : target) + ";" +
            endOfLine);

        target = getDefaultTarget();
        buffer.append("    " +  Jimple.DEFAULT + ": " + Jimple.GOTO + " " +
            (target == this ? "self" : target) + ";" + endOfLine);

        buffer.append("}");

        return buffer.toString();
    }
    
    @Override
    public void toString(UnitPrinter up)
    {
        up.literal(Jimple.TABLESWITCH);
        up.literal("(");
        keyBox.toString(up);
        up.literal(")");
        up.newline();
        up.literal("{");
        up.newline();
        // In this for-loop, we cannot use "<=" since 'i' would wrap around.
        // The case for "i == highIndex" is handled separately after the loop.
        for(int i = lowIndex; i < highIndex; i++) {
            printCaseTarget(up, i);
        }
        printCaseTarget(up, highIndex);

        up.literal("    ");
        up.literal(Jimple.DEFAULT);
        up.literal(": ");
        up.literal(Jimple.GOTO);
        up.literal(" ");
        defaultTargetBox.toString(up);
        up.literal(";");
        up.newline();
        up.literal("}");
    }


	private void printCaseTarget(UnitPrinter up, int targetIndex) {
		up.literal("    ");
		up.literal(Jimple.CASE);
		up.literal(" ");
		up.literal(Integer.toString(targetIndex));
		up.literal(": ");
		up.literal(Jimple.GOTO);
		up.literal(" ");
		targetBoxes[targetIndex-lowIndex].toString(up);
		up.literal(";");
		up.newline();
	}

    @Override
    public void setLowIndex(int lowIndex)
    {
        this.lowIndex = lowIndex;
    }

    @Override
    public void setHighIndex(int highIndex)
    {
        this.highIndex = highIndex;
    }

    @Override
    public int getLowIndex()
    {
        return lowIndex;
    }

    @Override
    public int getHighIndex()
    {
        return highIndex;
    }

    @Override
    public void apply(Switch sw)
    {
        ((StmtSwitch) sw).caseTableSwitchStmt(this);
    }    

}
