package de.upb.soot.callgraph;

import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.typehierarchy.TypeHierarchy;
import de.upb.soot.views.View;

import java.util.List;

public class OnDemandClassHierarchyAlgorithm extends ClassHierarchyAlgorithm {

    public OnDemandClassHierarchyAlgorithm(View view, TypeHierarchy hierarchy) {
        super(view, hierarchy);
    }

    @Override
    public CallGraph initialize(List<MethodSignature> entryPoints) {
        return null;
    }
}
