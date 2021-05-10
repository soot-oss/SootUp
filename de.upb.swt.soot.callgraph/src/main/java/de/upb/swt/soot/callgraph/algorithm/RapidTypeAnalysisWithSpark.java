package de.upb.swt.soot.callgraph.algorithm;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RapidTypeAnalysisWithSpark extends AbstractCallGraphAlgorithm {

    @Nonnull private Set<ClassType> instantiatedClasses = new HashSet<>();
    @Nonnull private CallGraph chaGraph;
    @Nonnull private PointerAssignmentGraph pag;

    public RapidTypeAnalysisWithSpark(@Nonnull View view, @Nonnull TypeHierarchy typeHierarchy, CallGraph chaGraph, PointerAssignmentGraph pag) {
        super(view, typeHierarchy);
        this.chaGraph = chaGraph;
        this.pag = pag;
    }

    @Nonnull
    @Override
    public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
        ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
        chaGraph = cha.initialize(entryPoints);
        return constructCompleteCallGraph(view, entryPoints);
    }

    @Override
    @Nonnull
    protected Set<MethodSignature> resolveCall(SootMethod method, AbstractInvokeExpr invokeExpr) {
        MethodSignature targetMethodSignature = invokeExpr.getMethodSignature();
        Set<MethodSignature> result = Sets.newHashSet(targetMethodSignature);

        if (!chaGraph.containsMethod(method.getSignature())) {
            return result;
        }
        collectInstantiatedClassesInMethod(method);

        SootMethod targetMethod =
                view.getClass(targetMethodSignature.getDeclClassType())
                        .flatMap(clazz -> clazz.getMethod(targetMethodSignature))
                        .orElseGet(() -> findMethodInHierarchy(view, targetMethodSignature));

        if (Modifier.isStatic(targetMethod.getModifiers())
                || (invokeExpr instanceof JSpecialInvokeExpr)) {
            return result;
        } else {
            Set<MethodSignature> implAndOverrides =
                    MethodDispatchResolver.resolveAbstractDispatchInClasses(
                            view, targetMethodSignature, instantiatedClasses);
            result.addAll(implAndOverrides);
            return result;
        }
    }

    private void collectInstantiatedClassesInMethod(SootMethod method) {
        Set<ClassType> instantiated = new HashSet<>();
        List<AllocationNode> allocationNodes = pag.getAllocationNodes(method);
        for(AllocationNode node: allocationNodes){
            JNewExpr newExpr = (JNewExpr) node.getNewExpr();
            if(newExpr.getType() instanceof ClassType){
                instantiated.add((ClassType) newExpr.getType());
            }
        }
        instantiatedClasses.addAll(instantiated);

        // add also found classes' super classes
        instantiated.stream()
                .map(s -> (SootClass) view.getClass(s).get())
                .map(s -> s.getSuperclass())
                .filter(s -> s.isPresent())
                .map(s -> s.get())
                .forEach(instantiatedClasses::add);
    }

}
