package de.upb.swt.soot.core.analysis;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Kadiray Karakaya and others
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
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import java.util.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Analysis that implements the LocalUses interface. Uses for a Local defined at a given Stmt are
 * returned as a list of Stmt,Value Pairs each containing a Stmt that use the local and the Local
 * itself wrapped in a ValueBox.
 */
public class SimpleLocalUses implements LocalUses {

  final Collection<Local> locals;
  private final Map<Stmt, List<Pair<Stmt, Value>>> stmtToUses;

  public SimpleLocalUses(Body body, LocalDefs localDefs) {
    this(body.getLocals(), body.getStmts(), localDefs);
  }

  public SimpleLocalUses(Body.BodyBuilder bodyBuilder, LocalDefs localDefs) {
    this(bodyBuilder.getLocals(), bodyBuilder.getStmts(), localDefs);
  }

  /**
   * Construct the analysis from a method body and a LocalDefs interface. This supposes that a
   * LocalDefs analysis must have been computed prior.
   */
  public SimpleLocalUses(Collection<Local> locals, List<Stmt> stmts, LocalDefs localDefs) {
    this.locals = locals;
    this.stmtToUses = new HashMap<>(stmts.size() * 2 + 1, 0.7f);

    // Traverse Stmts and associate uses with definitions
    for (Stmt stmt : stmts) {
      for (Value v : stmt.getUses()) {
        if (v instanceof Local) {
          // Add this statement to the uses of the definition of the local
          Local l = (Local) v;

          List<Stmt> defs = localDefs.getDefsOfAt(l, stmt);
          if (defs != null) {
            Pair<Stmt, Value> newPair = new ImmutablePair<>(stmt, v);
            for (Stmt def : defs) {
              List<Pair<Stmt, Value>> lst = stmtToUses.get(def);
              if (lst == null) {
                stmtToUses.put(def, lst = new ArrayList<>());
              }
              lst.add(newPair);
            }
          }
        }
      }
    }
  }

  /**
   * Uses for a Local defined at a given Stmt are returned as a list of Stmt,Value pairs each
   * containing a Stmt that use the local.
   *
   * @param s a stmt that we want to query for the uses of the Local it (may) define.
   * @return a Stmt,Value pairs of the Stmt that use the Local.
   */
  @Override
  public List<Pair<Stmt, Value>> getUsesOf(Stmt s) {
    List<Pair<Stmt, Value>> l = stmtToUses.get(s);
    return (l == null) ? Collections.emptyList() : Collections.unmodifiableList(l);
  }

  /**
   * Gets all variables that are used in this body
   *
   * @return The list of variables used in this body
   */
  public Set<Local> getUsedVariables() {
    Set<Local> res = new HashSet<>();
    for (List<Pair<Stmt, Value>> values : stmtToUses.values()) {
      for (Pair<Stmt, Value> val : values) {
        res.add((Local) val.getValue());
      }
    }
    return res;
  }

  /**
   * Gets all variables that are not used in this body
   *
   * @return The list of variables declared, but not used in this body
   */
  public Set<Local> getUnusedVariables() {
    Set<Local> res = new HashSet<>(locals);
    res.retainAll(getUsedVariables());
    return res;
  }
}
