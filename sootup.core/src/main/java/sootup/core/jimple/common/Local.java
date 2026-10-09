package sootup.core.jimple.common;

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

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.ImmediateVisitor;
import sootup.core.model.Body;
import sootup.core.types.Type;

/**
 * Local variable in a {@link Body}. Use {@link LocalGenerator} or the factories in {@link Jimple}
 * to create locals. JVM slot provenance is available through {@link SlotLocal}.
 *
 * <p>Implementations must compare locals by name in {@link Object#equals(Object)} and {@link
 * Object#hashCode()}, and by name and type for Jimple equivalence. Extend {@link AbstractLocal} to
 * inherit this contract.
 */
public interface Local extends Immediate, LValue, Acceptor<ImmediateVisitor> {
  @NonNull String getName();

  /** Returns a copy with a new name, preserving all other state and capabilities. */
  @NonNull Local withName(@NonNull String name);

  /** Returns a copy with a new type, preserving all other state and capabilities. */
  @NonNull Local withType(@NonNull Type type);

  List<AbstractDefinitionStmt> getDefs(Collection<Stmt> defs);

  List<Stmt> getDefsForLocalUse(ControlFlowGraph<?> graph, Stmt stmt);

  List<Stmt> getStmtsUsingOrDefiningthisLocal(Collection<Stmt> stmts, Stmt removedStmt);
}
