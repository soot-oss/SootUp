package sootup.java.bytecode.interceptors.typeresolving;
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
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.types.Type;
import sootup.java.bytecode.interceptors.typeresolving.types.BottomType;

public class Typing {
  private HashMap<Local, Type> local2Type = new HashMap<>();
  private BitSet stmtsIDList;

  public Typing(@Nonnull Collection<Local> locals) {
    for (Local local : locals) {
      local2Type.put(local, BottomType.getInstance());
    }
    stmtsIDList = new BitSet();
  }

  public Typing(@Nonnull Typing typing, @Nonnull BitSet stmtsIDList) {
    this.local2Type = new HashMap<>(typing.local2Type);
    this.stmtsIDList = stmtsIDList;
  }

  public Type getType(Local local) {
    return local2Type.get(local);
  }

  public void set(@Nonnull Local local, @Nonnull Type type) {
    this.local2Type.put(local, type);
  }

  public Collection<Local> getLocals() {
    return local2Type.keySet();
  }

  public Map<Local, Type> getMap() {
    return this.local2Type;
  }

  public void setStmtsIDList(BitSet bitSet) {
    this.stmtsIDList = bitSet;
  }

  public BitSet getStmtsIDList() {
    return this.stmtsIDList;
  }

  /**
   * This method is used to compare two {@link Typing}s that have same locals' set, but with
   * different types.
   *
   * @return 0: same to the given <code>typing</code>. 1: more general to the given <code>typing
   *     </code>. -1: more specific to the given <code>typing</code>. -2: cannot be compared to the
   *     given <code>typing</code>. One local have two different types which have no ancestor
   *     relationship. 2: cannot be compared to the given <code>typing</code>. One local's type is
   *     more specific than the given typing, but another local's type is more general than the
   *     given typing.
   */
  public int compare(Typing typing, BytecodeHierarchy hierarchy, Collection<Local> localsToIgnore) {
    if (!typing.getLocals().equals(this.getLocals())) {
      throw new RuntimeException("The compared typings should have the same locals' set!");
    }
    int ret = 0;
    for (Local local : this.local2Type.keySet()) {
      if (!localsToIgnore.contains(local)) {
        Type ta = getType(local);
        Type tb = typing.getType(local);

        int cmp;
        if (ta.equals(tb)) {
          cmp = 0;
        } else if (hierarchy.isAncestor(ta, tb)) {
          cmp = 1;
        } else if (hierarchy.isAncestor(tb, ta)) {
          cmp = -1;
        } else {
          return -2;
        }

        if ((cmp == 1 && ret == -1) || (cmp == -1 && ret == 1)) {
          return 2;
        }
        if (ret == 0) {
          ret = cmp;
        }
      }
    }
    return ret;
  }
}
