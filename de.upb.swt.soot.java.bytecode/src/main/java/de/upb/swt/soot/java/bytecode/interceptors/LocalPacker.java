package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Zun Wang
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
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.Type;
import java.util.*;
import javax.annotation.Nonnull;

public class LocalPacker implements BodyInterceptor {

  @Override
  @Nonnull
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    /** local : Type */
    Map<Local, Object> localToGroup = new HashMap<>();
    /** Type : numOfColor */
    Map<Object, Integer> groupToColorCount = new HashMap<>();
    /** Local : ColorNum */
    // local with same type have different color.
    Map<Local, Integer> localToColor = new HashMap<>();
    /** Local, newLocal */
    Map<Local, Local> localToNewLocal = new HashMap<>();

    for (Local l : builder.getLocals()) {
      Type g = l.getType();

      localToGroup.put(l, g);

      if (!groupToColorCount.containsKey(g)) {
        groupToColorCount.put(g, 0);
      }
    }

    for (Stmt s : builder.getStmts()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getLeftOp() instanceof Local) {
        Local l = (Local) ((JIdentityStmt) s).getLeftOp();

        Object group = localToGroup.get(l);
        int count = groupToColorCount.get(group).intValue();

        localToColor.put(l, new Integer(count));

        count++;

        groupToColorCount.put(group, new Integer(count));
      }
    }

    List<Local> originalLocals = new ArrayList<>(builder.getLocals());
    Map<GroupIntPair, Local> groupIntToLocal = new HashMap<>();
    Set<String> usedLocalNames = new HashSet<>();

    for (Local original : originalLocals) {
      Object group = localToGroup.get(original);
      int color = localToColor.get(original);
      GroupIntPair pair = new GroupIntPair(group, color);

      Local newLocal;

      if (groupIntToLocal.containsKey(pair)) {
        newLocal = groupIntToLocal.get(pair);
      } else {
        newLocal = original.withType((Type) group);

        // If we have a split local, let's find a better name for it
        int signIndex = newLocal.getName().indexOf("#");
        if (signIndex != -1) {
          String newName = newLocal.getName().substring(0, signIndex);
          if (usedLocalNames.add(newName)) {
            newLocal = newLocal.withName(newName);
          }
        }
        groupIntToLocal.put(pair, newLocal);
      }
      localToNewLocal.put(original, newLocal);
    }

    System.out.println(localToGroup);
    System.out.println(groupToColorCount);
    System.out.println(localToColor);
  }

  public class GroupIntPair {
    public Object group;
    public int x;

    public GroupIntPair(Object group, int x) {
      this.group = group;
      this.x = x;
    }

    public boolean equals(Object other) {
      if (other instanceof GroupIntPair) {
        return ((GroupIntPair) other).group.equals(this.group)
            && ((GroupIntPair) other).x == this.x;
      } else {
        return false;
      }
    }

    public int hashCode() {
      return group.hashCode() + 1013 * x;
    }

    @Override
    public String toString() {
      return this.group + ": " + this.x;
    }
  }
}
