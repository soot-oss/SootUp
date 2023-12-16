package sootup.java.codepropertygraph.cfg;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

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
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCfgForIfElseStatement() {
    methodName = "ifElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCfgForIfElseIfStatement() {
    methodName = "ifElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCfgForIfElseCascadingStatement() {
    methodName = "ifElseCascadingStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfStatement() {
    methodName = "ifElseCascadingElseIfStatement";
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfStatement");
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCfgForIfElseCascadingElseIfInElseStatement() {
    methodName = "ifElseCascadingElseIfInElseStatement";
    Optional<? extends SootMethod> method =
        getMinimalTestSuiteMethod("ifElseCascadingElseIfInElseStatement");
    assertTrue(method.isPresent());
    MethodCfg cfg = new MethodCfg(method.get());
    CfgGraph cfgGraph = CfgToGraphConverter.convert(cfg);
    writeGraph(cfgGraph.toDotFormat(), methodName);
  }

  Optional<? extends SootMethod> getMinimalTestSuiteMethod(String methodName) {
    PathBasedAnalysisInputLocation inputLocation =
        PathBasedAnalysisInputLocation.create(MINIMAL_TEST_SUITE_DIR, null);
    View<JavaSootClass> view = new JavaView(inputLocation);
    ClassType appClass = view.getIdentifierFactory().getClassType(CLASS_NAME);
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(appClass, methodName, "int", Collections.singletonList("int"));
    return view.getMethod(methodSignature);
  }

  private void writeGraph(String dotGraph, String methodName) {
    System.out.println(methodName);
    System.out.println(dotGraph);

    // writeToFile(dotGraph, methodName);
  }

  private static void writeToFile(String dotGraph, String methodName) {
    File file = new File("temp/cfg_" + methodName + ".dot");
    System.out.println(file.toPath());

    // Create the output folder if it doesn't exist
    File folder = file.getParentFile();
    if (folder != null && !folder.exists()) {
      folder.mkdirs();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(dotGraph);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
