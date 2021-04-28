package de.upb.swt.soot.callgraph.spark.solver;

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
