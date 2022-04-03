package de.upb.swt.soot.test.callgraph.spark.propagater;

import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.builder.SparkOptions;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.jimple.parser.JimpleAnalysisInputLocation;
import de.upb.swt.soot.jimple.parser.JimpleProject;
import de.upb.swt.soot.jimple.parser.JimpleView;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PropagatorTest {

    PointerAssignmentGraph buildPAG(String className) {

        String resourceDir = "src/test/resources/spark/PointerBench/propagator-jimple/";
        JimpleAnalysisInputLocation location = new JimpleAnalysisInputLocation(Paths.get(resourceDir));
        JimpleView view = new JimpleProject(location).createOnDemandView();

        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        JavaClassType mainClassType = identifierFactory.getClassType(className);

        List<String> parameters = new ArrayList<>();
        parameters.add("java.lang.String[]");
        MethodSignature methodSignature =
                identifierFactory.getMethodSignature("main", mainClassType, "void", parameters);

        final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
        CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
        CallGraph callGraph = algorithm.initialize(Collections.singletonList(methodSignature));
        PointerAssignmentGraph pag = new PointerAssignmentGraph(view, callGraph, new SparkOptions());

        return pag;
    }
}
