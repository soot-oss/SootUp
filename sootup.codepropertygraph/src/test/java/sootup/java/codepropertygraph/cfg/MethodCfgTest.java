package sootup.java.codepropertygraph.cfg;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;

public class MethodCfgTest {
  private static final Path MINIMAL_TEST_SUITE_DIR =
      Paths.get("..//shared-test-resources/miniTestSuite/java6/binary");

  private static final String CLASS_NAME = "IfElseStatement";
  private String methodName;

  @org.junit.Before
  public void setUp() {}

  @Test
  public void testCfgForIfStatement() {
    methodName = "ifStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

  @Test
  public void testCfgForIfElseStatement() {
    methodName = "ifElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

  @Test
  public void testCfgForIfElseIfStatement() {
    methodName = "ifElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

  @Test
  public void testCfgForIfElseCascadingStatement() {
    methodName = "ifElseCascadingStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

  @Test
  public void testCfgForIfElseCascadingElseIfStatement() {
    methodName = "ifElseCascadingElseIfStatement";
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfStatement");
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

  @Test
  public void testCfgForIfElseCascadingElseIfInElseStatement() {
    methodName = "ifElseCascadingElseIfInElseStatement";
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfInElseStatement");
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    System.out.println(methodName);
    System.out.println(cfgGraph.toDotFormat());
  }

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
}
