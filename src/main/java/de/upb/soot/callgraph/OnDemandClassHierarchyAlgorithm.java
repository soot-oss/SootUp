package de.upb.soot.callgraph;

import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.signatures.MethodSignature;

import java.util.List;
import java.util.stream.Stream;

public class OnDemandClassHierarchyAlgorithm extends AbstractCallGraphAlgorithm {

    @Override
    protected Stream<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
        return null;
    }

    @Override
    public CallGraph initialize(List<MethodSignature> entryPoints) {
        return null;
    }
}
