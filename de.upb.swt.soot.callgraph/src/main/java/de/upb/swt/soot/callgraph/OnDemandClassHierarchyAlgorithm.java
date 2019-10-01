package de.upb.swt.soot.callgraph;

import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;
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
