package sootup.codepropertygraph.propertygraph.util;

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

import sootup.codepropertygraph.propertygraph.AstPropertyGraph;
import sootup.codepropertygraph.propertygraph.PropertyGraph;

public class PropertyGraphsMerger {
  public static PropertyGraph mergeGraphs(PropertyGraph graph1, PropertyGraph graph2) {
    PropertyGraph.Builder mergedGraphBuilder = new AstPropertyGraph.Builder();
    mergedGraphBuilder.setName("merged_graph");

    graph1.getNodes().forEach(mergedGraphBuilder::addNode);
    graph1.getEdges().forEach(mergedGraphBuilder::addEdge);
    graph2.getNodes().forEach(mergedGraphBuilder::addNode);
    graph2.getEdges().forEach(mergedGraphBuilder::addEdge);

    return mergedGraphBuilder.build();
  }
}
