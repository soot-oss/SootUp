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
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.Type;
import java.util.*;
import javax.annotation.Nonnull;

/** @author ZunWang* */
public class LocalPacker implements BodyInterceptor {

  @Override
  @Nonnull
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    Map<Local, Integer> localToColor = assignLocalsColor(builder);
    // map each color to a new color
    Map<Local, Local> localToNewLocal = new HashMap<>();
    List<Local> originalLocals = new ArrayList<>(builder.getLocals());

    Map<TypeColorPair, Local> typeColorToLocal = new HashMap<>();
    Set<String> usedLocalNames = new HashSet<>();
    Set<Local> newLocals = new HashSet<>();
    for (Local original : originalLocals) {
      Type type = original.getType();
      int color = localToColor.get(original);
      TypeColorPair pair = new TypeColorPair(type, color);

      Local newLocal;

      if (typeColorToLocal.containsKey(pair)) {
        newLocal = typeColorToLocal.get(pair);
      } else {
        newLocal = original;
        // If local's name contains "#", '#' and the part behind it should be deleted
        int signIndex = newLocal.getName().indexOf("#");
        if (signIndex != -1) {
          String newName = newLocal.getName().substring(0, signIndex);
          if (usedLocalNames.add(newName)) {
            newLocal = newLocal.withName(newName);
          } else {
            // TODO: think a better local name strategy
          }
        }
        typeColorToLocal.put(pair, newLocal);
      }
      newLocals.add(newLocal);
      localToNewLocal.put(original, newLocal);
    }
    builder.setLocals(newLocals);

    // TODO: newLocals instead of original Locals in for all stmts
  }

  /**
   * Assign each local from a Bodybuilder a color
   *
   * @param builder
   * @return a Map that maps local to a integer color
   */
  private Map<Local, Integer> assignLocalsColor(Body.BodyBuilder builder) {
    Map<Local, Integer> localToColor = new HashMap<>();

    // initial typeToColorCount, ColorCount is also the next free color for corresponding Type.
    Map<Type, Integer> typeToColorCount = new HashMap<>();
    for (Local local : builder.getLocals()) {
      Type type = local.getType();
      if (!typeToColorCount.containsKey(type)) {
        typeToColorCount.put(type, 0);
      }
    }
    // assign each parameter local a color (local from IdentityStmt)
    for (Stmt stmt : builder.getStmts()) {
      if (stmt instanceof JIdentityStmt) {
        if (((JIdentityStmt) stmt).getLeftOp() instanceof Local) {
          Local l = (Local) ((JIdentityStmt) stmt).getLeftOp();
          Type type = l.getType();
          int count = typeToColorCount.get(type);
          localToColor.put(l, count);
          count++;
          typeToColorCount.put(type, count);
        }
      } else {
        break;
      }
    }
    // Sort locals according to their number of interference-locals, local with more interferences <
    // local with less interferences
    Map<Local, Set<Local>> localInterferenceMap = buildLocalInterferenceMap(builder);
    List<Local> sortedLocals = new ArrayList<>(builder.getLocals());
    Collections.sort(
        sortedLocals,
        (o1, o2) -> {
          int num1 = localInterferenceMap.containsKey(o1) ? localInterferenceMap.get(o1).size() : 0;
          int num2 = localInterferenceMap.containsKey(o2) ? localInterferenceMap.get(o2).size() : 0;
          return num2 - num1;
        });

    // assign color
    for (Local local : sortedLocals) {
      if (!localToColor.containsKey(local)) {
        Type type = local.getType();
        int colorCount = typeToColorCount.get(type);
        // determine which colors are unavailable for this local
        Set<Local> interferences = new HashSet<>();
        BitSet unavailableColors = new BitSet(colorCount);
        if (localInterferenceMap.containsKey(local)) {
          interferences = localInterferenceMap.get(local);
          for (Local interference : interferences) {
            if (localToColor.containsKey(interference)) {
              unavailableColors.set(localToColor.get(interference));
            }
          }
        }
        int assignedColor = -1;
        for (int i = 0; i < colorCount; i++) {
          if (!unavailableColors.get(i)) {
            assignedColor = i;
            break;
          }
        }
        if (assignedColor < 0) {
          colorCount++;
          assignedColor = colorCount;
          typeToColorCount.put(type, colorCount);
        }
        localToColor.put(local, assignedColor);
      }
    }
    return localToColor;
  }

  /**
   * Find interference-local for each local from the given BodyBuilder. Two locals "l1" and "l2"
   * interfere each other, iff "l1" is alive before a successor of a stmt which defines "l2", vice
   * versa.
   *
   * @param builder a given BodyBuilder
   * @return a Map that maps local to a set of interference-locals
   */
  private Map<Local, Set<Local>> buildLocalInterferenceMap(Body.BodyBuilder builder) {
    // Maps local to its interfering locals
    Map<Local, Set<Local>> localToLocals = new HashMap<>();
    StmtGraph graph = builder.getStmtGraph();
    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(graph);

    for (Stmt stmt : builder.getStmts()) {
      if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {

        Local def = (Local) stmt.getDefs().get(0);

        Set<Local> aliveLocals = new HashSet<>();
        for (Stmt succ : graph.successors(stmt)) {
          aliveLocals.addAll(analyser.getLiveLocalsBeforeStmt(succ));
        }

        for (Local aliveLocal : aliveLocals) {
          if (aliveLocal.getType().equals(def.getType())) {
            // set interference for both locals: aliveLocal, def
            if (localToLocals.containsKey(def)) {
              localToLocals.get(def).add(aliveLocal);
            } else {
              Set<Local> locals = new HashSet<>();
              locals.add(aliveLocal);
              localToLocals.put(def, locals);
            }

            if (localToLocals.containsKey(aliveLocal)) {
              localToLocals.get(aliveLocal).add(def);
            } else {
              Set<Local> locals = new HashSet<>();
              locals.add(def);
              localToLocals.put(aliveLocal, locals);
            }
          }
        }
      }
    }
    return localToLocals;
  }

  private class TypeColorPair {
    public Type type;
    public int color;

    public TypeColorPair(Type type, int color) {
      this.type = type;
      this.color = color;
    }

    public boolean equals(Object other) {
      if (other instanceof TypeColorPair) {
        return ((TypeColorPair) other).type.equals(this.type)
            && ((TypeColorPair) other).color == this.color;
      } else {
        return false;
      }
    }
  }
}
