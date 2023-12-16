package sootup.java.codepropertygraph.ddg;

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

public class MethodDdgTest {
  private static final Path MINIMAL_TEST_SUITE_DIR =
      Paths.get("..//shared-test-resources/miniTestSuite/java6/binary");

  private static final String CLASS_NAME = "IfElseStatement";

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

  @org.junit.Before
  public void setUp() {}

  @Test
  public void testDdgForIfStatement() {
    String methodName = "ifStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);
    writeGraph(ddgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testDdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);
    writeGraph(ddgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testDdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);
    writeGraph(ddgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);

    writeGraph(ddgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testDdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodDdg ddg = new MethodDdg(method.get());
    DdgGraph ddgGraph = DdgToGraphConverter.convert(ddg);

    writeGraph(ddgGraph.toDotFormat(), methodName);
  }

  private void writeGraph(String dotGraph, String methodName) {
    System.out.println(methodName);
    System.out.println(dotGraph);

    // writeToFile(dotGraph, methodName);
  }

  private static void writeToFile(String dotGraph, String methodName) {
    File file = new File("temp/ddg_" + methodName + ".dot");
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
