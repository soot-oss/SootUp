package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import java.util.*;
import java.util.stream.Collectors;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

/* Flow -insensitive def-use analysis */
public class DexDefUseAnalysis {

  private final Body.BodyBuilder bodyBuilder;
  private final Map<Local, Set<Stmt>> localToUses = new HashMap<>();
  private final Map<Local, Set<Stmt>> localToDefs = new HashMap<>();
  private final Map<Local, Set<Stmt>> localToDefsWithAliases = new HashMap<>();

  protected Map<Local, Integer> localToNumber = new HashMap<>();
  protected BitSet[] localToDefsBits;
  protected BitSet[] localToUsesBits;
  protected List<Stmt> stmtList;

  public DexDefUseAnalysis(Body.BodyBuilder bodyBuilder) {
    this.bodyBuilder = bodyBuilder;

    initialize();
  }

  private void initialize() {
    int lastLocalNumber = 0;
    for (Local l : bodyBuilder.getLocals()) {
      localToNumber.put(l, lastLocalNumber++);
    }
    localToDefsBits = new BitSet[bodyBuilder.getLocals().size()];
    localToUsesBits = new BitSet[bodyBuilder.getLocals().size()];

    stmtList = new ArrayList<>(bodyBuilder.getStmts());

    for (int i = 0; i < stmtList.size(); i++) {
      Stmt stmt = stmtList.get(i);

      // Record the Abstract definitions stmt
      if (stmt instanceof AbstractDefinitionStmt) {
        Value val = ((AbstractDefinitionStmt) stmt).getLeftOp();
        addLocalToUseBits(i, val, localToDefsBits);
      }
      List<Value> collect = stmt.getUses().collect(Collectors.toList());
      // Record the uses
      for (Value val : collect) {
        addLocalToUseBits(i, val, localToUsesBits);
      }
    }
  }

  /* Returns the uses of statement for the given Local */
  public Set<Stmt> getUsesOf(Local l) {
    Set<Stmt> uses = localToUses.get(l);
    if (uses == null) {
      uses = new HashSet<>();
      BitSet bs = localToUsesBits[localToNumber.get(l)];
      if (bs != null) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
          uses.add(stmtList.get(i));
        }
      }
      localToUses.put(l, uses);
    }
    return uses;
  }

  /**
   * Collect definitions of l in body including the definitions of aliases of l. This analysis
   * exploits that the problem is flow-insensitive anyway.
   *
   * <p>In this context an alias is a local that propagates its value to l.
   *
   * @param l the local whose definitions are to collect
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
        if (localToNumber.get(curLocal) == null) {
          continue;
        }
        BitSet bsDefs = localToDefsBits[localToNumber.get(curLocal)];
        if (bsDefs != null) {
          for (int i = bsDefs.nextSetBit(0); i >= 0; i = bsDefs.nextSetBit(i + 1)) {
            Stmt stmt = stmtList.get(i);
            defs.add(stmt);

            AbstractDefinitionStmt defStmt = (AbstractDefinitionStmt) stmt;
            if (defStmt.getRightOp() instanceof Local
                && seenLocals.add((Local) defStmt.getRightOp())) {
              newLocals.add((Local) defStmt.getRightOp());
            }
          }
        }

        // Use of l?
        BitSet bsUses = localToUsesBits[localToNumber.get(curLocal)];
        if (bsUses != null) {
          for (int i = bsUses.nextSetBit(0); i >= 0; i = bsUses.nextSetBit(i + 1)) {
            Stmt stmt = stmtList.get(i);
            if (stmt instanceof JAssignStmt) {
              JAssignStmt assignUse = (JAssignStmt) stmt;
              if (assignUse.getRightOp() == curLocal
                  && assignUse.getLeftOp() instanceof Local
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

  /* Returns the definitions of the local in the given stmt graph*/
  public List<Stmt> getDefsOf(Local l) {
    Set<Stmt> defs = localToDefs.get(l);
    if (defs == null) {
      defs = new HashSet<>();
      if (localToNumber.get(l) == null) {
        return Collections.emptyList();
      }
      BitSet bs = localToDefsBits[localToNumber.get(l)];
      if (bs != null) {
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
          Stmt stmt = stmtList.get(i);
          if (stmt instanceof AbstractDefinitionStmt) {
            if (((AbstractDefinitionStmt) stmt).getLeftOp() == l) {
              defs.add(stmt);
            }
          }
        }
      }
      localToDefs.put(l, defs);
    }
    return new ArrayList<>(defs);
  }

  private void addLocalToUseBits(int i, Value val, BitSet[] localToUsesBits) {
    if (val instanceof Local) {
      if (localToNumber.get((Local) val) != null) {
        final int localIdx = localToNumber.get((Local) val);
        BitSet bs = localToUsesBits[localIdx];
        if (bs == null) {
          localToUsesBits[localIdx] = bs = new BitSet();
        }
        bs.set(i);
      }
    }
  }
}
