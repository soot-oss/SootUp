package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.ImmediateVisitor;
import sootup.core.model.Body;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.core.util.printer.StmtPrinter;

/**
 * Local variable in {@link Body}. Use {@link LocalGenerator} to generate locals.
 *
 * <p>Prefer to use the factory methods in {@link Jimple}.
 *
 * @author Linghui Luo
 */
public class Local implements Immediate, LValue, Acceptor<ImmediateVisitor> {

  @NonNull private final String name;
  @NonNull private final Type type;

  /** Constructs a JimpleLocal of the given name and type. */
  public Local(@NonNull String name, @NonNull Type type) {
    this.name = name;
    if (type instanceof VoidType) {
      throw new RuntimeException("Type should not be VoidType");
    } else {
      this.type = type;
    }
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Local)) {
      return false;
    }
    return name.equals(((Local) o).getName());
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseLocal(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(name, type);
  }

  /** Returns the name of this object. */
  @NonNull
  public String getName() {
    return name;
  }

  /** Returns the type of this local. */
  @NonNull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.local(this);
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.empty();
  }

  /** returns a List that can contain: Locals, JFieldRefs, JArrayRefs */
  public List<AbstractDefinitionStmt> getDefs(Collection<Stmt> defs) {
    List<AbstractDefinitionStmt> localDefs = new ArrayList<>();
    for (Stmt stmt : defs) {
      if (stmt instanceof AbstractDefinitionStmt
          && ((AbstractDefinitionStmt) stmt).getLeftOp().equals(this)) {
        localDefs.add((AbstractDefinitionStmt) stmt);
      }
    }
    return localDefs;
  }

  /**
   * Get all definition-stmts which define the given local used by the given stmt.
   *
   * @param graph a stmt graph which contains the given stmts.
   * @param stmt a stmt which uses the given local.
   */
  public List<Stmt> getDefsForLocalUse(StmtGraph<?> graph, Stmt stmt) {
    if (stmt.getUses().noneMatch(v -> v == this)) {
      throw new RuntimeException(stmt + " doesn't use the local " + this);
    }
    List<Stmt> defStmts = new ArrayList<>();
    Set<Stmt> visited = new HashSet<>();

    Deque<Stmt> queue = new ArrayDeque<>();
    queue.add(stmt);
    while (!queue.isEmpty()) {
      Stmt s = queue.removeFirst();
      if (!visited.contains(s)) {
        visited.add(s);
        if (s instanceof AbstractDefinitionStmt && s.getDef().get().equivTo(this)) {
          defStmts.add(s);
        } else {
          if (graph.containsNode(s)) {
            queue.addAll(graph.predecessors(s));
          }
        }
      }
    }
    return defStmts;
  }

  public List<Stmt> getStmtsUsingOrDefiningthisLocal(Collection<Stmt> stmts, Stmt removedStmt) {
    List<Stmt> localOccurrences = new ArrayList<>();
    for (Stmt stmt : stmts) {
      if (stmt.equivTo(removedStmt)) continue;
      List<Value> stmtUsesAndDefs = stmt.getUsesAndDefs().collect(Collectors.toList());
      for (Value stmtUse : stmtUsesAndDefs) {
        if (stmtUse instanceof Local && stmtUse.equivTo(this)) {
          localOccurrences.add(stmt);
        }
      }
    }
    return localOccurrences;
  }

  @Override
  public <V extends ImmediateVisitor> V accept(@NonNull V v) {
    v.caseLocal(this);
    return v;
  }

  @NonNull
  public Local withName(@NonNull String name) {
    return new Local(name, type);
  }

  @NonNull
  public Local withType(@NonNull Type type) {
    return new Local(name, type);
  }
}
