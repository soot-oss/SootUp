package sootup.examples.callgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class CallgraphExampleTest {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation("src/test/resources/Callgraph/binary"));
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation()); // add rt.jar

    JavaView view = new JavaView(inputLocations);

    // Get a MethodSignature
    ClassType classTypeA = view.getIdentifierFactory().getClassType("A");
    ClassType classTypeB = view.getIdentifierFactory().getClassType("B");
    MethodSignature entryMethodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                classTypeB,
                view.getIdentifierFactory()
                    .getMethodSubSignature(
                        "calc", VoidType.getInstance(), Collections.singletonList(classTypeA)));

    // Create type hierarchy and CHA
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    System.out.println("Subclasses of A: ");
    typeHierarchy.subclassesOf(classTypeA).forEach(System.out::println);

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

    // Create CG by initializing CHA with entry method(s)
    CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

    System.out.println("Call Graph from B:");
    final Set<CallGraph.Call> calls = cg.callsFrom(entryMethodSignature);
    calls.forEach(System.out::println);
  }
}
