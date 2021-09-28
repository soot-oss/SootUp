package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.analysis.LocalDefs;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.*;

/**
 * Simplistic caching, flow-insensitive def/use analysis
 *
 * @author Steven Arzt
 *
 */
public class DexDefUseAnalysis implements LocalDefs {

  private final Body.BodyBuilder bodyBuilder;
  private final Map<Local, Set<Stmt>> localToUses = new HashMap<Local, Set<Stmt>>();
  private final Map<Local, Set<Stmt>> localToDefs = new HashMap<Local, Set<Stmt>>();
  private final Map<Local, Set<Stmt>> localToDefsWithAliases = new HashMap<Local, Set<Stmt>>();

  protected Map<Local, Integer> localToNumber = new HashMap<>();
  protected BitSet[] localToDefsBits;
  protected BitSet[] localToUsesBits;
  protected List<Stmt> unitList;

  public DexDefUseAnalysis(Body.BodyBuilder bodyBuilder) {
    this.bodyBuilder = bodyBuilder;

    initialize();
  }

  protected void initialize() {
    int lastLocalNumber = 0;
    for (Local l : bodyBuilder.getLocals()) {
      localToNumber.put(l, lastLocalNumber++);
    }

    localToDefsBits = new BitSet[bodyBuilder.getLocals().size()];
    localToUsesBits = new BitSet[bodyBuilder.getLocals().size()];

    unitList = new ArrayList<>(bodyBuilder.getStmts());
    for (int i = 0; i < unitList.size(); i++) {
      Stmt u = unitList.get(i);

      // Record the definitions
      if (u instanceof AbstractDefinitionStmt) {
        Value val = ((AbstractDefinitionStmt ) u).getLeftOp();
        if (val instanceof Local) {
          final int localIdx = localToNumber.get((Local) val);
          BitSet bs = localToDefsBits[localIdx];
          if (bs == null) {
            localToDefsBits[localIdx] = bs = new BitSet();
          }
          bs.set(i);
        }
      }

      // Record the uses
      for (Value value : u.getUses()) {
        if (value instanceof Local) {
          final int localIdx = localToNumber.get((Local) value);
          BitSet bs = localToUsesBits[localIdx];
          if (bs == null) {
            localToUsesBits[localIdx] = bs = new BitSet();
          }
          bs.set(i);
        }
      }
    }
  }

  public Set<Stmt> getUsesOf(Local l) {
    Set<Stmt> uses = localToUses.get(l);
    if (uses == null) {
      uses = new HashSet<>();
      BitSet bs = localToUsesBits[localToNumber.get(l)];
      if (bs != null) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
          uses.add(unitList.get(i));
        }
      }
      localToUses.put(l, uses);
    }
    return uses;
  }

  /**
   * Collect definitions of l in body including the definitions of aliases of l. This analysis exploits that the problem is
   * flow-insensitive anyway.
   *
   * In this context an alias is a local that propagates its value to l.
   *
   * @param l
   *          the local whose definitions are to collect
   */
  protected Set<Stmt> collectDefinitionsWithAliases(Local l) {
    Set<Stmt> defs = localToDefsWithAliases.get(l);
    if (defs == null) {
      defs = new HashSet<Stmt>();

      Set<Local> seenLocals = new HashSet<Local>();
      List<Local> newLocals = new ArrayList<Local>();
      newLocals.add(l);
      while (!newLocals.isEmpty()) {
        Local curLocal = newLocals.remove(0);

        // Definition of l?
        BitSet bsDefs = localToDefsBits[localToNumber.get(curLocal)];
        if (bsDefs != null) {
          for (int i = bsDefs.nextSetBit(0); i >= 0; i = bsDefs.nextSetBit(i + 1)) {
            Stmt u = unitList.get(i);
            defs.add(u);

            AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) u;
            if (defStmt.getRightOp() instanceof Local && seenLocals.add((Local) defStmt.getRightOp())) {
              newLocals.add((Local) defStmt.getRightOp());
            }
          }
        }

        // Use of l?
        BitSet bsUses = localToUsesBits[localToNumber.get(curLocal)];
        if (bsUses != null) {
          for (int i = bsUses.nextSetBit(0); i >= 0; i = bsUses.nextSetBit(i + 1)) {
            Stmt use = unitList.get(i);
            if (use instanceof JAssignStmt) {
              JAssignStmt assignUse = (JAssignStmt) use;
              if (assignUse.getRightOp() == curLocal && assignUse.getLeftOp() instanceof Local
                  && seenLocals.add((Local) assignUse.getLeftOp())) {
                newLocals.add((Local) assignUse.getLeftOp());
              }
            }
          }
        }
      }
      localToDefsWithAliases.put(l, defs);
    }

    return defs;
  }

  @Override
  public List<Stmt> getDefsOfAt(Local l, Stmt s) {
    return getDefsOf(l);
  }


  @Override
  public List<Stmt> getDefsOf(Local l) {
    Set<Stmt> defs = localToDefs.get(l);
    if (defs == null) {
      defs = new HashSet<>();
      BitSet bs = localToDefsBits[localToNumber.get(l)];
      if (bs != null) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
          Stmt u = unitList.get(i);
          if (u instanceof AbstractDefinitionStmt) {
            if (((AbstractDefinitionStmt) u).getLeftOp() == l) {
              defs.add(u);
            }
          }
        }
      }
      localToDefs.put(l, defs);
    }
    return new ArrayList<>(defs);
  }
}
