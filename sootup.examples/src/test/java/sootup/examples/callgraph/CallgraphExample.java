package sootup.examples.callgraph;

import java.util.Collections;
import org.junit.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class CallgraphExample {

  @Test
  public void test() {
    // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
    // from the directory
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation("src/test/resources/Callgraph/binary");

    // Specify the language of the JavaProject. This is especially relevant for Multi-release jars,
    // where classes are loaded depending on the language level of the analysis
    JavaLanguage language = new JavaLanguage(8);

    // Create a new JavaProject and view based on the input location
    JavaProject project =
        JavaProject.builder(language)
            .addInputLocation(inputLocation)
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar")) // add rt.jar
            .build();

    JavaView view = project.createView();

    // Get a MethodSignature
    ClassType classTypeA = project.getIdentifierFactory().getClassType("A");
    ClassType classTypeB = project.getIdentifierFactory().getClassType("B");
    MethodSignature entryMethodSignature =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                classTypeB,
                JavaIdentifierFactory.getInstance()
                    .getMethodSubSignature(
                        "calc", VoidType.getInstance(), Collections.singletonList(classTypeA)));

    // Create type hierarchy and CHA
    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    System.out.println(typeHierarchy.subclassesOf(classTypeA));
    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);

    // Create CG by initializing CHA with entry method(s)
    CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

    cg.callsFrom(entryMethodSignature).forEach(System.out::println);
  }
}
