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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.FieldReferenceNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.core.model.Field;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Propagates points-to sets along pointer assignment graph using a relevant aliases.
 */
public class AliasPropagator implements Propagator{
    private final Set<VariableNode> varNodeWorkList = new TreeSet<>();
    private Set<VariableNode> aliasWorkList;
    private Set<FieldReferenceNode> fieldRefWorkList = new HashSet<>();
    private Set<FieldReferenceNode> outFieldRefWorkList = new HashSet<>();
    private PointerAssignmentGraph pag;
    private Multimap<Field, VariableNode> fieldToBase = HashMultimap.create();
    private Multimap<FieldReferenceNode, FieldReferenceNode> aliasEdges = HashMultimap.create();
    private Map<FieldReferenceNode, Set> loadSets;


    public AliasPropagator(PointerAssignmentGraph pag){
        this.pag = pag;
    }

    @Override
    public void propagate(){

    }

}
