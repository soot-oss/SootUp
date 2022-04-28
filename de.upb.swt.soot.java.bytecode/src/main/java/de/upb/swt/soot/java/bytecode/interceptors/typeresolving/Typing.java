package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;

public class Typing {
    private HashMap<Local, Type> local2Type = new HashMap<>();

    public Typing(@Nonnull Collection<Local> locals){
        for(Local local : locals ){
            local2Type.put(local, BottomType.getInstance());
        }
    }

    public Typing(@Nonnull Typing typing){
        this.local2Type = new HashMap<>(typing.getMap());
    }

    public HashMap<Local, Type> getMap(){
        return this.local2Type;
    }

    public void set(Local local, Type type){
        this.local2Type.put(local, type);
    }

    public Collection<Local> getLocals() {
        return local2Type.keySet();
    }

}
