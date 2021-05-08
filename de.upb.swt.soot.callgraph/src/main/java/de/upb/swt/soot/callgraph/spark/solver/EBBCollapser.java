package de.upb.swt.soot.callgraph.spark.solver;

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
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.FieldReferenceNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.types.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Collapses nodes that are members of simple trees (EBBs) in the pointer assignment graph.
 */
public class EBBCollapser implements Collapser {
    private PointerAssignmentGraph pag;
    private int numCollapsed = 0;

    public EBBCollapser(PointerAssignmentGraph pag) {
        this.pag = pag;
    }

    public void collapse() {
        collapseAlloc();
        collapseLoad();
        collapseSimple();
    }

    private void collapseAlloc() {
        // TODO: handle ofcg
        Map<AllocationNode, Set<VariableNode>> allocationEdges = pag.getAllocationEdges();
        for (Map.Entry<AllocationNode, Set<VariableNode>> entry : allocationEdges.entrySet()) {
            VariableNode firstSucc = null;
            for (VariableNode succ : entry.getValue()) {
                if (pag.allocInvLookup(succ).size() > 1
                        || pag.loadInvLookup(succ).size() > 0
                        || pag.simpleInvLookup(succ).size() > 0) {
                    // TODO: ofcg
                    continue;
                }
                if (firstSucc == null) {
                    firstSucc = succ;
                } else {
                    if (firstSucc.getType().equals(succ.getType())) {
                        firstSucc.mergeWith(succ);
                        numCollapsed++;
                    }
                }
            }
        }
    }

    private void collapseSimple() {
        boolean change;
        final TypeHierarchy typeHierarchy = pag.getTypeHierarchy();
        do {
            change = false;
            Map<VariableNode, Set<VariableNode>> simpleEdges = pag.getSimpleEdges();
            for (Map.Entry<VariableNode, Set<VariableNode>> entry : simpleEdges.entrySet()) {
                final VariableNode n = entry.getKey();
                Type nType = n.getType();

                for (VariableNode succ : entry.getValue()) {
                    Type sType = succ.getType();
                    if (!typeHierarchy.canCast(nType, sType)
                            || pag.allocInvLookup(succ).size() > 0
                            || pag.loadInvLookup(succ).size() > 0
                            || pag.simpleInvLookup(succ).size() > 1) {
                        // TODO: ocfg
                        continue;
                    }
                    n.mergeWith(succ);
                    change=true;
                    numCollapsed++;
                }
            }
        } while (change);
    }

    private void collapseLoad(){
        final TypeHierarchy typeHierarchy = pag.getTypeHierarchy();
        Map<FieldReferenceNode, Set<VariableNode>> loadEdges = pag.getLoadEdges();
        for (Map.Entry<FieldReferenceNode, Set<VariableNode>> entry : loadEdges.entrySet()) {
            final FieldReferenceNode n = entry.getKey();
            Type nType = n.getType();
            Node firstSucc = null;
            Map<Type, VariableNode> typeToSucc = new HashMap<>();
            for(VariableNode succ: entry.getValue()){
                Type sType = succ.getType();
                if(pag.allocInvLookup(succ).size()>0 || pag.loadInvLookup(succ).size()>1 || pag.simpleInvLookup(succ).size()>0){
                    // TODO: ofcg
                    continue;
                }
                if(typeHierarchy.canCast(nType, sType)){
                    if(firstSucc==null){
                        firstSucc = succ;
                    } else {
                        firstSucc.mergeWith(succ);
                        numCollapsed++;
                    }
                } else {
                    VariableNode rep = typeToSucc.get(succ.getType());
                    if(rep==null){
                        typeToSucc.put(succ.getType(), succ);
                    } else {
                        rep.mergeWith(succ);
                        numCollapsed++;
                    }
                }
            }
        }
    }

}
