package de.upb.swt.soot.callgraph.spark.pag;

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

import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.views.View;
import org.jgrapht.graph.DefaultDirectedGraph;


public class PointerAssignmentGraph {

    // VariableNodes for local variables and static fields
    // Single VariableNode for all thrown exceptions
    // Depending on the options:
    // instance fields, method params and return values are represented with:
    // - variable, or
    // - field reference nodes
    // Array elements are always represented with field reference node
    // allocnodes created for alloc sites and string constants, including args to main method

    // Edges are created for all pointer-valued assignments including:
    // - casts
    // - throw catch
    // - pointers passed to and returned from methods (unless cfg is otf)
    // special edges for implicit flows:
    // - finalize methods
    // - java.lang.Thread start method to run method


    private final DefaultDirectedGraph<SparkVertex, SparkEdge> graph;
    private CallGraph callGraph;
    private View view;

    public PointerAssignmentGraph(View view, CallGraph callGraph) {
        this.view = view;
        this.callGraph = callGraph;
        this.graph = new DefaultDirectedGraph<>(null, null, false);
    }

    private void build(){
        for(AbstractClass<? extends AbstractClassSource> clazz: view.getClasses()){
            for (Method method : clazz.getMethods()) {
                SootMethod sootMethod = (SootMethod) method;
                if(sootMethod.isConcrete() || sootMethod.isNative()){
                    // TODO: native case
                    if(callGraph.containsMethod(sootMethod.getSignature())){
                        IntraproceduralPointerAssignmentGraph intraPAG = new IntraproceduralPointerAssignmentGraph(sootMethod);
                        addIntraproceduralPointerAssignmentGraph(intraPAG);
                    }
                }
            }
        }
    }

    private void addEdge(Node source, Node target){
        graph.addEdge(new SparkVertex(source), new SparkVertex(target));
    }

    private void addIntraproceduralPointerAssignmentGraph(IntraproceduralPointerAssignmentGraph intraPAG){
        DefaultDirectedGraph<SparkVertex, SparkEdge> intraGraph = intraPAG.getGraph();
        // handle intraGraph
    }
}
