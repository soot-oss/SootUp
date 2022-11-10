package de.upb.sse.sootup.examples.callgraph;

import de.upb.sse.sootup.callgraph.CallGraph;
import de.upb.sse.sootup.callgraph.CallGraphAlgorithm;
import de.upb.sse.sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import de.upb.sse.sootup.callgraph.RapidTypeAnalysisAlgorithm;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.typehierarchy.ViewTypeHierarchy;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.core.types.VoidType;
import de.upb.sse.sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.util.Collections;
import org.junit.Test;

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

    JavaView view = project.createFullView();

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
    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    cha = new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    // Create CG by initializing CHA with entry method(s)
    CallGraph cg = cha.initialize(Collections.singletonList(entryMethodSignature));

    cg.callsFrom(entryMethodSignature).forEach(System.out::println);
  }
}
