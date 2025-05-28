package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2025 Ondrej Lhotak, Kadiray Karakaya, Palaniappan Muthuraman
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

import java.util.HashSet;
import java.util.Set;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.spark.node.Node;

public class Spark {
  public Spark(View view, CallGraph callGraph) {}

  public Set<Node> getPointsToSet(Local local) {
    return new HashSet<>();
  }

  public Type getType(Local local) {
    return PrimitiveType.IntType.getInstance();
  }

  public Set<Local> getAliases(Local local) {
    return new HashSet<>();
  }
}
