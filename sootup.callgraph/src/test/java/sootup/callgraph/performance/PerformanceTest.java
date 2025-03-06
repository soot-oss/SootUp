package sootup.callgraph.performance;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.Collections;

public class PerformanceTest {
    @Nested
    class PolymorphicCallsPerformance {
        // type hierarchy
        String pathToBinary = "src/test/java/sootup/callgraph/performance";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);
        JavaView view = new JavaView(inputLocation);
        // entry method
        JavaClassType classType = view.getIdentifierFactory().getClassType("sootup.callgraph.performance.PolymorphicCalls");
        MethodSignature entryMethodSig = view.getIdentifierFactory().getMethodSignature(classType, "main", VoidType.getInstance(), Collections.singletonList(classType));

        @Test
        public void testPolymorphicCallsCHA() {
            CallGraph cgCHA1 = new ClassHierarchyAnalysisAlgorithm(view).initialize(Collections.singletonList(entryMethodSig));
        }

        @Test
        public void testPolymorphicCallsRTA() {
            CallGraph cgRTA1 = new RapidTypeAnalysisAlgorithm(view).initialize(Collections.singletonList(entryMethodSig));
        }
    }

    @Nested
    class InvokeFromClassItselfPerformance {
        // type hierarchy
        String pathToBinary2 = "src/test/java/sootup/callgraph/performance";
        AnalysisInputLocation inputLocation2 = new JavaClassPathAnalysisInputLocation(pathToBinary2);
        JavaView view2 = new JavaView(inputLocation2);
        //entry method
        JavaClassType classType2 = view2.getIdentifierFactory().getClassType("sootup.callgraph.performance.InvokeFromClassItself");
        MethodSignature entryMethodSig2 = view2.getIdentifierFactory().getMethodSignature(classType2, "main", VoidType.getInstance(), Collections.singletonList(classType2));

        @Test
        public void testInvokeFromClassItselfCHA() {
            CallGraph cgCHA2 = new ClassHierarchyAnalysisAlgorithm(view2).initialize(Collections.singletonList(entryMethodSig2));
        }

        @Test
        public void testInvokeFromClassItselfRTA() {
            CallGraph cgRTA2 = new RapidTypeAnalysisAlgorithm(view2).initialize(Collections.singletonList(entryMethodSig2));
        }

        @Test
        public void testInvokeFromClassItselfParallel() {
            CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view2);
            CallGraphAlgorithm rta = new RapidTypeAnalysisAlgorithm(view2);
            CallGraph cgParallelCHA = cha.initialize(Collections.singletonList(entryMethodSig2));
            CallGraph cgParallelRTA = rta.initialize(Collections.singletonList(entryMethodSig2));
            /*
            cgParallelCHA.callsFrom(entryMethodSig2).stream().
                    forEach(tgt -> System.out.println(entryMethodSig2 + " may call " + tgt));
            cgParallelRTA.callsFrom(entryMethodSig2).stream().
                    forEach(tgt -> System.out.println(entryMethodSig2 + " may call " + tgt));
             */
        }
    }

    @Nested
    class InvokeDefaultMethodPerformance {
        // type hierarchy
        String pathToBinary3 = "src/test/java/sootup/callgraph/performance";
        AnalysisInputLocation inputLocation3 = new JavaClassPathAnalysisInputLocation(pathToBinary3);
        JavaView view3 = new JavaView(inputLocation3);
        //entry method
        JavaClassType classType3 = view3.getIdentifierFactory().getClassType("sootup.callgraph.performance.InvokeDefaultMethodExample");
        MethodSignature entryMethodSig3 = view3.getIdentifierFactory().getMethodSignature(classType3, "main", VoidType.getInstance(), Collections.singletonList(classType3));

        @Test
        public void testInvokeDefaultMethodCHA() {
            CallGraph cgCHA3 = new ClassHierarchyAnalysisAlgorithm(view3).initialize(Collections.singletonList(entryMethodSig3));
        }

        @Test
        public void testInvokeDefaultMethodRTA() {
            CallGraph cgRTA3 = new RapidTypeAnalysisAlgorithm(view3).initialize(Collections.singletonList(entryMethodSig3));
        }
    }
}
