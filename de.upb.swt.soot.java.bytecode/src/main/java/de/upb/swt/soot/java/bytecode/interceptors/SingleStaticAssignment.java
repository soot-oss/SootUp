package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import com.sun.istack.internal.NotNull;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

/** A BodyTransformer that attemps to indentify the usage of the single static assignment */
public class SingleStaticAssignment implements BodyInterceptor {
  @Override
  public void interceptBody(@NotNull Body.BodyBuilder builder) {
    // TODO implement (Refer Soot Shimple):  BodyBuilder for SSA
    // Remove nops before building cfg
    // PhiNode insertion: If a variable is not defined along all paths of entry to a node
    StmtGraph stmtGraph = builder.getStmtGraph();
  }
}
