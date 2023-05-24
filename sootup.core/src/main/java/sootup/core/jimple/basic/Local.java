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
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.ImmediateVisitor;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/**
 * Local variable in {@link Body}. Use {@link LocalGenerator} to generate locals.
 *
 * <p>Prefer to use the factory methods in {@link Jimple}.
 *
 * @author Linghui Luo
 */
public class Local implements Immediate, Copyable, Acceptor<ImmediateVisitor> {

  @Nonnull private final String name;
  @Nonnull private final Type type;
  @Nonnull private final Position position; // position of the declaration

  /** Constructs a JimpleLocal of the given name and type. */
  public Local(@Nonnull String name, @Nonnull Type type) {
    this(name, type, NoPositionInformation.getInstance());
  }

  /** Constructs a JimpleLocal of the given name and type. */
  public Local(@Nonnull String name, @Nonnull Type type, @Nonnull Position position) {
    this.name = name;
    this.type = type;
    this.position = position;
  }

  /** Hint: the naming is just an indicator! */
  public boolean isFieldLocal() {
    return getName().charAt(0) != '$';
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
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseLocal(this, o);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(name, type);
  }

  /** Returns the name of this object. */
  @Nonnull
  public String getName() {
    return name;
  }

  /** Returns the type of this local. */
  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.local(this);
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    return Collections.emptyList();
  }

  @Nonnull
  public Position getPosition() {
    return position;
  }

  public List<AbstractDefinitionStmt<Local, Value>> getDefsOfLocal(List<Stmt> defs) {
    List<AbstractDefinitionStmt<Local, Value>> localDefs = new ArrayList<>();
    for (Stmt stmt : defs) {
      if (stmt instanceof AbstractDefinitionStmt
          && ((AbstractDefinitionStmt<Local, Value>) stmt).getLeftOp().equals(this)) {
        localDefs.add((AbstractDefinitionStmt<Local, Value>) stmt);
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
    if (!stmt.getUses().contains(this)) {
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
        if (s instanceof AbstractDefinitionStmt && s.getDefs().get(0).equivTo(this)) {
          defStmts.add(s);
        } else {
          queue.addAll(graph.predecessors(s));
        }
      }
    }
    return defStmts;
  }

  @Override
  public void accept(@Nonnull ImmediateVisitor v) {
    v.caseLocal(this);
  }

  @Nonnull
  public Local withName(@Nonnull String name) {
    return new Local(name, type);
  }

  @Nonnull
  public Local withType(@Nonnull Type type) {
    return new Local(name, type);
  }
}
