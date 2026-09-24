package sootup.codepropertygraph.propertygraph.edges;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

/** Abstract base class for CFG (Control Flow Graph) edges in a property graph. */
public abstract class AbstCfgEdge extends PropertyGraphEdge {
  /** Creates a CFG edge between the given source and destination nodes. */
  public AbstCfgEdge(PropertyGraphNode source, PropertyGraphNode destination) {
    super(source, destination);
  }

  /** Returns the label identifying this CFG edge type. */
  public abstract String getLabel();
}
