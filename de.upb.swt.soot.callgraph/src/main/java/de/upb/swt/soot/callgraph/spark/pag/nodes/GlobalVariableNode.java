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

import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph that is not associated
 * with any particular method invocation.
 */
public class GlobalVariableNode extends VariableNode {
  public GlobalVariableNode(Object variable, Type type) {
    super(variable, type);
  }

  public String toString() {
    return "GlobalVarNode " + variable;
  }

  public ClassType getDeclaringClassType() {
    if (variable instanceof SootField) {
      return ((SootField) variable).getDeclaringClassType();
    }

    return null;
  }
}
