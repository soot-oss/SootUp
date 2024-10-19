package sootup.interceptors;
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
import com.google.common.collect.Lists;
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.Type;
import sootup.core.views.View;

/** @author Zun Wang * */
public class LocalPacker implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {
    MutableStmtGraph stmtGraph = builder.getStmtGraph();

    Map<Local, Integer> localToColor = assignLocalsColor(builder);
    // map each original local to a new local
    Map<Local, Local> localToNewLocal = new HashMap<>();
    // map each new local to corresponding list of original local
    Map<Local, List<Local>> newLocalToLocals = new HashMap<>();
    List<Local> originalLocals = new ArrayList<>(builder.getLocals());

    Map<TypeColorPair, Local> typeColorToLocal = new HashMap<>();
    int localIndex = 0;
    for (Local original : originalLocals) {
      Type type = original.getType();
      int color = localToColor.get(original);
      TypeColorPair pair = new TypeColorPair(type, color);

      Local newLocal;

      if (typeColorToLocal.containsKey(pair)) {
        newLocal = typeColorToLocal.get(pair);
      } else {
        String name = original.getName();
        int i = 0;
        for (; i < name.length(); i++) {
          if (Character.isDigit(name.charAt(i))) {
            break;
          }
        }
        String newName = name.substring(0, i) + '*' + localIndex;
        localIndex++;
        newLocal = original.withName(newName);

        typeColorToLocal.put(pair, newLocal);
      }
      localToNewLocal.put(original, newLocal);
      if (!newLocalToLocals.containsKey(newLocal)) {
        newLocalToLocals.put(newLocal, new ArrayList<>());
      }
      newLocalToLocals.get(newLocal).add(original);
    }

    // Correct a reasonable name for each new local and change them in BodyBuilder
    // store all new locals with reasonable name, if a local is not in newLoals, means that it
    // doesn't has reasonable name
    Set<Local> newLocals = new LinkedHashSet<>();
    for (Stmt stmt : Lists.newArrayList(stmtGraph)) {
      Stmt newStmt = stmt;
      for (Iterator<Value> iterator = stmt.getUses().iterator(); iterator.hasNext(); ) {
        Value use = iterator.next();
        if (use instanceof Local) {
          Local newLocal = localToNewLocal.get(use);
          if (newLocal == null) {
            continue;
          }
          // assign a reasonable name
          if (!newLocals.contains(newLocal)) {
            int starPos = newLocal.getName().indexOf('*');
            String reasonableName = newLocal.getName().substring(0, starPos) + newLocals.size();
            List<Local> oriLocals = newLocalToLocals.get(newLocal);
            newLocal = newLocal.withName(reasonableName);
            newLocals.add(newLocal);
            for (Local ori : oriLocals) {
              localToNewLocal.put(ori, newLocal);
            }
          }
          newStmt = newStmt.withNewUse(use, newLocal);
        }
      }
      Optional<LValue> defOpt = stmt.getDef();
      if (defOpt.isPresent() && defOpt.get() instanceof Local) {
        Local def = (Local) defOpt.get();
        Local newLocal = localToNewLocal.get(def);
        if (newLocal != null) {
          // assign a reasonable name
          if (!newLocals.contains(newLocal)) {
            int starPos = newLocal.getName().indexOf('*');
            String reasonableName = newLocal.getName().substring(0, starPos) + newLocals.size();
            List<Local> oriLocals = newLocalToLocals.get(newLocal);
            newLocal = newLocal.withName(reasonableName);
            newLocals.add(newLocal);
            for (Local ori : oriLocals) {
              localToNewLocal.put(ori, newLocal);
            }
          }
          newStmt = ((AbstractDefinitionStmt) newStmt).withNewDef(newLocal);
        }
      }
      if (!stmt.equals(newStmt)) {
        stmtGraph.replaceNode(stmt, newStmt);
      }
    }
    builder.setLocals(newLocals);
  }

  /**
   * Assign each local from a Bodybuilder an integer color
   *
   * @param builder an instance of BodyBuilder
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
      }
    }
    // Sort locals according to their number of interference-locals, local with more interferences <
    // local with less interferences
    Map<Local, Set<Local>> localInterferenceMap = buildLocalInterferenceMap(builder);
    List<Local> sortedLocals = new ArrayList<>(builder.getLocals());
    sortedLocals.sort(
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
        BitSet unavailableColors = new BitSet(colorCount);
        if (localInterferenceMap.containsKey(local)) {
          Set<Local> interferences = localInterferenceMap.get(local);
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
    StmtGraph<?> graph = builder.getStmtGraph();
    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(graph);

    // TODO: check if sorted Stmts are necessary
    for (Stmt stmt : builder.getStmts()) {
      if (stmt.getDef().isPresent() && stmt.getDef().get() instanceof Local) {

        Local def = (Local) stmt.getDef().get();

        Set<Local> aliveLocals = new HashSet<>();
        for (Stmt succ : graph.successors(stmt)) {
          aliveLocals.addAll(analyser.getLiveLocalsBeforeStmt(succ));
        }
        for (Stmt esucc : graph.exceptionalSuccessors(stmt).values()) {
          aliveLocals.addAll(analyser.getLiveLocalsBeforeStmt(esucc));
        }
        for (Local aliveLocal : aliveLocals) {
          if (aliveLocal != def && aliveLocal.getType().equals(def.getType())) {
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

  private static class TypeColorPair {
    private Type type;
    private int color;

    public TypeColorPair(Type type, int color) {
      this.setType(type);
      this.setColor(color);
    }

    public Type getType() {
      return type;
    }

    public void setType(Type type) {
      this.type = type;
    }

    public int getColor() {
      return color;
    }

    public void setColor(int color) {
      this.color = color;
    }

    public int hashCode() {
      return getType().hashCode() + 1013 * getColor();
    }

    public boolean equals(Object other) {
      if (other instanceof TypeColorPair) {
        return ((TypeColorPair) other).getType().equals(this.getType())
            && ((TypeColorPair) other).getColor() == this.getColor();
      } else {
        return false;
      }
    }
  }
}
