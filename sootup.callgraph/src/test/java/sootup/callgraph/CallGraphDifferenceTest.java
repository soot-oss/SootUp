package sootup.callgraph;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CallGraphDifferenceTest {

    @Test
    public void testCGDiff() {
        String classPath = System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes" + File.separator + "callgraph" + File.separator + "CallGraphDifference";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath, null, Collections.emptyList());
        JavaView view = new JavaView(inputLocation);

        ClassHierarchyAnalysisAlgorithm chaAlgorithm = new ClassHierarchyAnalysisAlgorithm(view);
        JavaClassType chaClassType = view.getIdentifierFactory().getClassType("CG1");
        MethodSignature chaMethodSignature = view.getIdentifierFactory()
                .getMethodSignature(chaClassType, "main", "void", Collections.singletonList("java.lang.String[]"));
        CallGraph cg1 = chaAlgorithm.initialize(Collections.singletonList(chaMethodSignature));

        RapidTypeAnalysisAlgorithm rtaAlgorithm = new RapidTypeAnalysisAlgorithm(view);
        JavaClassType rtaClassType = view.getIdentifierFactory().getClassType("CG2");
        MethodSignature rtaMethodSignature = view.getIdentifierFactory()
                .getMethodSignature(rtaClassType, "main", "void", Collections.singletonList("java.lang.String[]"));
        CallGraph cg2 = rtaAlgorithm.initialize(Collections.singletonList(rtaMethodSignature));

        CallGraphDifference callGraphDifference = new CallGraphDifference(cg1, cg2);
        List<Pair<MethodSignature, MethodSignature>> addedEdges = callGraphDifference.addedEdges();
        addedEdges.forEach(System.out::println);
        System.out.println("-----");
        List<Pair<MethodSignature, MethodSignature>> removedEdges = callGraphDifference.removedEdges();
        removedEdges.forEach(System.out::println);
    }

}
