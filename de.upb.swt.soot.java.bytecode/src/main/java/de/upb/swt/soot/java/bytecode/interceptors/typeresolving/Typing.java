package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import java.util.Collection;
import java.util.HashMap;
import javax.annotation.Nonnull;

public class Typing {
  private HashMap<Local, Type> local2Type = new HashMap<>();

  public Typing(@Nonnull Collection<Local> locals) {
    for (Local local : locals) {
      local2Type.put(local, BottomType.getInstance());
    }
  }

  public Type getType(Local local) {
    return this.local2Type.get(local);
  }

  public void set(@Nonnull Local local, @Nonnull Type type) {
    this.local2Type.put(local, type);
  }

  public Collection<Local> getLocals() {
    return local2Type.keySet();
  }
}
