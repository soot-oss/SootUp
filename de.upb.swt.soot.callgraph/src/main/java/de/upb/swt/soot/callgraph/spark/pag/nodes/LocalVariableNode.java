package de.upb.swt.soot.callgraph.spark.pag.nodes;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph that is specific to a
 * particular method invocation.
 */
public class LocalVariableNode extends VariableNode {
  private SootMethod method;

  public LocalVariableNode(PointerAssignmentGraph pag, Object variable, Type type, SootMethod method) {
    super(pag, variable, type);
    this.method = method;
  }

  public SootMethod getMethod() {
    return method;
  }

  @Override
  public String toString() {
    return "LocalVariableNode " + variable + " " + method;
  }
}
