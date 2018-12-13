package de.upb.soot.jimple;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Local;

public class IgnoreLocalNameComparator extends JimpleComparator {

    public boolean caseLocal(Local obj, Object o) {
        if( !(o instanceof Local)){
            return false;
        }
        return obj.getType().equals( ((Local) o).getType());

    }
}
