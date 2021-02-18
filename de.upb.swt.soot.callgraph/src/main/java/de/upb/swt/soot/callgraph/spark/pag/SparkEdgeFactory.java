package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Kadiray Karakaya
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

import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SparkEdgeFactory {

  public SparkEdge getEdge(Node source, Node target) {
    if (source == null || target == null) {
      throw new RuntimeException("Cannot get edge for null nodes");
    }

    if (source instanceof VariableNode) {
      if (target instanceof VariableNode) {
        return new SparkEdge(EdgeType.ASSIGNMENT_EDGE);
      } else if (target instanceof FieldReferenceNode) {
        return new SparkEdge(EdgeType.STORE_EDGE);
      } else if (target instanceof NewInstanceNode) {
        // TODO: NewInstanceEdge
        throw new NotImplementedException();
      } else {
        throw new RuntimeException("Invalid node type:" + target);
      }
    } else if (source instanceof FieldReferenceNode) {
      return new SparkEdge(EdgeType.LOAD_EDGE);
    } else if (source instanceof NewInstanceNode) {
      // TODO: assignInstanceEdge
      throw new NotImplementedException();
    } else {
      return new SparkEdge(EdgeType.ALLOCATION_EDGE);
    }
  }
}
