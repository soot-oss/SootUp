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
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.types.Type;

/** Node that represents a call to newInstance() */
public class NewInstanceNode extends Node {
  private final Value value;

  public NewInstanceNode(PointerAssignmentGraph pag, Type type, Value value) {
    super(pag, type);
    this.value = value;
  }

  public Value getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "NewInstanceNode{" + "value=" + value + '}';
  }
}
