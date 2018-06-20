package de.upb.soot.jimple.common.stmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.upb.soot.jimple.StmtBox;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;

public abstract class AbstractSwitchStmt extends AbstractStmt {

  final protected StmtBox defaultTargetBox;
    
  final protected ValueBox keyBox;

  final protected List<StmtBox> stmtBoxes;
    
  final protected StmtBox[] targetBoxes;
    
    protected AbstractSwitchStmt(ValueBox keyBox, StmtBox defaultTargetBox, StmtBox ... targetBoxes) {
    	this.keyBox = keyBox;
    	this.defaultTargetBox = defaultTargetBox;
    	this.targetBoxes = targetBoxes;
    	
        // Build up stmtBoxes
        List<StmtBox> list = new ArrayList<StmtBox>();
        stmtBoxes = Collections.unmodifiableList(list);
        
        Collections.addAll(list, targetBoxes);
        list.add(defaultTargetBox);
    }


  final public Stmt getDefaultTarget()
    {
        return defaultTargetBox.getStmt();
    }


  final public void setDefaultTarget(Stmt defaultTarget)
    {
        defaultTargetBox.setStmt(defaultTarget);
    }


    final public StmtBox getDefaultTargetBox()
    {
        return defaultTargetBox;
    }


    final public Value getKey()
    {
        return keyBox.getValue();
    }


    final public void setKey(Value key)
    {
        keyBox.setValue(key);
    }


    final public ValueBox getKeyBox()
    {
        return keyBox;
    }    
    

    @Override
    final public List<ValueBox> getUseBoxes()
    {
        List<ValueBox> list = new ArrayList<ValueBox>();

        list.addAll(keyBox.getValue().getUseBoxes());
        list.add(keyBox);

        return list;
    }
    
    final public int getTargetCount()
    {
        return targetBoxes.length;
    }
    

  final public Stmt getTarget(int index)
    {
        return targetBoxes[index].getStmt();
    }


    final public StmtBox getTargetBox(int index)
    {
        return targetBoxes[index];
    }


  final public void setTarget(int index, Stmt target)
    {
        targetBoxes[index].setStmt(target);
    }
    
  final public List<Stmt> getTargets()
    {
    List<Stmt> targets = new ArrayList<Stmt>();

        for (StmtBox element : targetBoxes) {
          targets.add(element.getStmt());
        }

        return targets;
    }
    
  final public void setTargets(List<? extends Stmt> targets)
    {
        for(int i = 0; i < targets.size(); i++) {
          targetBoxes[i].setStmt(targets.get(i));
        }
    }
    
  final public void setTargets(Stmt[] targets)
    {
        for(int i = 0; i < targets.length; i++) {
          targetBoxes[i].setStmt(targets[i]);
        }
    }

    @Override
    final public List<StmtBox> getUnitBoxes()
    {
        return stmtBoxes;
    }

    @Override
    public final boolean fallsThrough() 
    {
    	return false;
	}
    
    @Override
    public final boolean branches()
    {
    	return true;
	}
}
