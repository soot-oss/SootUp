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
import com.sun.media.jfxmedia.events.BufferListener;
import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
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
  //Todo: type instead of group
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    /** local : Type */
    Map<Local, Type> localToGroup = new HashMap<>();
    /** Type : numOfColor */
    Map<Type, Integer> groupToColorCount = new HashMap<>();
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

    System.out.println(localToGroup);
    System.out.println(groupToColorCount);
    System.out.println(localToColor);

    // Assign each local which is IdentityStmt's def a color
    // Locals with same Type has always different color
    for (Stmt s : builder.getStmts()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getLeftOp() instanceof Local) {
        Local l = (Local) ((JIdentityStmt) s).getLeftOp();

        Type group = localToGroup.get(l);
        int count = groupToColorCount.get(group).intValue();

        localToColor.put(l, new Integer(count));

        count++;

        groupToColorCount.put(group, new Integer(count));
      }
    }
    System.out.println(localToGroup);
    System.out.println(groupToColorCount);
    System.out.println(localToColor);

    Map<Local, Set<Local>> localToInterferings = buildLocalInterferenceMap(builder);
    System.out.println(localToInterferings);

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

    //System.out.println(localToGroup);
    //System.out.println(groupToColorCount);
    //System.out.println(localToColor);
  }

  /**
   * Find interference-local for each local from the given BodyBuilder.
   * Two locals "l1" and "l2" interfere each other, iff "l1" is alive before a successor of a stmt which defines "l2", vice versa.
   * @param builder a given BodyBuilder
   * @return a Map that maps local to a set of interference-locals
   */
  private Map<Local, Set<Local>> buildLocalInterferenceMap(Body.BodyBuilder builder){
    //Maps local to its interfering locals
    Map<Local, Set<Local>> localToLocals = new HashMap<>();
    StmtGraph graph = builder.getStmtGraph();
    LocalLivenessAnalyser analyser = new LocalLivenessAnalyser(graph);

    for(Stmt stmt : builder.getStmts()){
      if(!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local){

        Local def = (Local) stmt.getDefs().get(0);

        Set<Local> aliveLocals = new HashSet<>();
        for(Stmt succ : graph.successors(stmt)){
          aliveLocals.addAll(analyser.getLiveLocalsBeforeStmt(succ));
        }

        for(Local aliveLocal : aliveLocals){
          if(aliveLocal.getType().equals(def.getType())){
             //set interference for both locals: aliveLocal, def
             if(localToLocals.containsKey(def)){
               localToLocals.get(def).add(aliveLocal);
             }else{
               Set<Local> locals = new HashSet<>();
               locals.add(aliveLocal);
               localToLocals.put(def, locals);
             }

            if(localToLocals.containsKey(aliveLocal)){
              localToLocals.get(aliveLocal).add(def);
            }else{
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



  public class ColorAssigner {

    private final Body.BodyBuilder builder;
    private final Map<Local, Type> localToGroup;
    private final Map<Type, Integer> groupToColorCount;
    private final Map<Local, Integer> localToColor;

    public ColorAssigner(Body.BodyBuilder builder, Map<Local, Type> localToGroup, Map<Type, Integer> groupToColorCount, Map<Local, Integer> localToColor){
        this.builder = builder;
        this.localToGroup = localToGroup;
        this.groupToColorCount = groupToColorCount;
        this.localToColor = localToColor;
    }

    public void assignColorsToLocals(){

      ExceptionalStmtGraph exceptionalStmtGraph = builder.getStmtGraph();
      LocalLivenessAnalyser livenessAnalyser = new LocalLivenessAnalyser(exceptionalStmtGraph);
    }
  }

}
