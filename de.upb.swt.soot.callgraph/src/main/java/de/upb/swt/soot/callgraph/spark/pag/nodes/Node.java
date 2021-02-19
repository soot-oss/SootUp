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

import com.google.common.collect.Sets;
import de.upb.swt.soot.core.types.Type;
import java.util.Set;

public class Node {
  protected Type type;
  protected Node replacement;
  protected Set<Node> pointsToSet;

  public Node() {}

  public Node(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public Set<Node> getPointsToSet() {
    if (pointsToSet != null) {
      // TODO: replacement
      return pointsToSet;
    }
    // TODO: replacement
    return Sets.newHashSet();
  }

  public Set<Node> getOrCreatePointsToSet() {
    if (pointsToSet != null) {
      if (replacement != this) {
        throw new RuntimeException(
            "Node " + this + " has replacement " + replacement + " but has points-to set");
      }
      return pointsToSet;
    }
    Node rep = getReplacement();
    if (rep == this) {
      pointsToSet = Sets.newHashSet();
    }
    return rep.getOrCreatePointsToSet();
  }

  public Node getReplacement() {
    if (replacement != replacement.replacement) {
      replacement = replacement.getReplacement();
    }
    return replacement;
  }
}
