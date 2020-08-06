package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2020 Raja Vallée-Rai and others
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
 *
 */
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A BodyTransformer that removes all unused local variables from a given Body.
 *
 * @author Marcus Nachtigall
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  /**
   * Removes unused local variables from the List of Stmts of the given {@link Body}. Complexity is
   * linear with respect to the statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    Set<Local> locals = new HashSet<>();

    // Traverse statements copying all used uses and defs
    for (Stmt stmt : originalBody.getStmts()) {
      for (Value value : stmt.getUsesAndDefs()) {
        if (value instanceof Local) {
          Local local = (Local) value;
          locals.add(local);
        }
      }
    }

    return Body.builder(originalBody).setLocals(locals).build();
  }
}
