package sootup.java.codepropertygraph.cdg;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Test;
import sootup.core.IdentifierFactory;
import sootup.core.graph.BasicBlock;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;

public class MethodCdgTest {
  private static final Path MINIMAL_TEST_SUITE_DIR =
      Paths.get("..//shared-test-resources/miniTestSuite/java6/binary");

  private static final String CLASS_NAME = "IfElseStatement";

  Optional<? extends SootMethod> getMinimalTestSuiteMethod(String methodName) {
    PathBasedAnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation(MINIMAL_TEST_SUITE_DIR, null);
    JavaLanguage language = new JavaLanguage(8);
    JavaProject project = JavaProject.builder(language).addInputLocation(inputLocation).build();
    IdentifierFactory projectIdentifierFactory = project.getIdentifierFactory();
    View<JavaSootClass> view = project.createView();
    ClassType appClass = projectIdentifierFactory.getClassType(CLASS_NAME);
    MethodSignature methodSignature =
        projectIdentifierFactory.getMethodSignature(
            appClass, methodName, "int", Collections.singletonList("int"));
    return view.getMethod(methodSignature);
  }

  @org.junit.Before
  public void setUp() {}

  @Test
  public void testCdgForIfStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifStatement");
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    System.out.println(cdgGraph.toDotFormat());
  }

  @Test
  public void testCdgForIfElseStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifElseStatement");
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    System.out.println(cdgGraph.toDotFormat());
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod("ifElseIfStatement");
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    System.out.println(cdgGraph.toDotFormat());
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfStatement");
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);

    System.out.println(cdgGraph.toDotFormat());
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfInElseStatement");
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);

    System.out.println(cdgGraph.toDotFormat());
  }
}
