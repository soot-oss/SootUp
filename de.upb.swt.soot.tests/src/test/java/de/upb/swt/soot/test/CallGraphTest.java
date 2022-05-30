package de.upb.swt.soot.test;

import static junit.framework.TestCase.*;

import categories.SlowTest;
import de.upb.swt.soot.callgraph.AbstractCallGraphAlgorithm;
import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.RapidTypeAnalysisAlgorithm;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.typerhierachy.TypeHierarchy;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTest.class)
public class CallGraphTest {

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  private AbstractCallGraphAlgorithm algorithm;
  private String algorithmName;

  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    if (algorithmName.equals("RTA")) {
      return new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    } else {
      return new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    }
  }

  private JavaView createViewForClassPath(String classPath) {
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    return javaProject.createOnDemandView();
  }

  CallGraph loadCallGraph(String testDirectory, String className) {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath = "src/test/resources/callgraph/" + testDirectory;

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    JavaView view = createViewForClassPath(classPath);

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).get();
    Optional<SootMethod> m =
        (Optional<SootMethod>) sc.getMethod(mainMethodSignature.getSubSignature());
    assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    algorithm = createAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);
    return cg;
  }

  @Test
  public void testRTA() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph("Misc", "HelloWorld");

    ClassType clazzType = JavaIdentifierFactory.getInstance().getClassType("java.io.PrintStream");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "println", clazzType, "void", Collections.singletonList("java.lang.String"));

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }

  @Test
  public void testCHA() {
    algorithmName = "CHA";
    CallGraph cg = loadCallGraph("Misc", "HelloWorld");

    ClassType clazzType = JavaIdentifierFactory.getInstance().getClassType("java.io.PrintStream");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "println", clazzType, "void", Collections.singletonList("java.lang.String"));

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }
}
