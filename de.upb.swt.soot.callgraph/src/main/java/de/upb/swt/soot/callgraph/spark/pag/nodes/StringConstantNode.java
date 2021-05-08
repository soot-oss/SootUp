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

import de.upb.swt.soot.callgraph.spark.builder.NodeConstants;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;

/** Represents an allocation site node the represents a constant string. */
public class StringConstantNode extends AllocationNode {
  public StringConstantNode(String sc) {
    super(JavaIdentifierFactory.getInstance().getClassType(NodeConstants.CLASS), sc, null);
  }

  public String getString() {
    return (String) getNewExpr();
  }

  public String toString() {
    return "StringConstantNode " + getNewExpr();
  }
}
