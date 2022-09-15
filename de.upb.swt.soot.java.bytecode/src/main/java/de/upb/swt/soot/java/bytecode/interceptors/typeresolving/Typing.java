package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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
