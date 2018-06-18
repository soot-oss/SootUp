package de.upb.soot.jimple.internal;

import java.util.ArrayList;
import java.util.List;

import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;

public abstract class AbstractOpStmt extends AbstractStmt {

	final ValueBox opBox;
	
	protected AbstractOpStmt(ValueBox opBox)
    {
        this.opBox = opBox;
    }
	
	
    final public Value getOp()
    {
        return opBox.getValue();
    }
	
	
	final public void setOp(Value op)
    {
        opBox.setValue(op);
    }

	
	final public ValueBox getOpBox()
    {
        return opBox;
    }
	
	@Override
    final public List<ValueBox> getUseBoxes()
    {
        List<ValueBox> list = new ArrayList<ValueBox>();

        list.addAll(opBox.getValue().getUseBoxes());
        list.add(opBox);
    
        return list;
    }
}
