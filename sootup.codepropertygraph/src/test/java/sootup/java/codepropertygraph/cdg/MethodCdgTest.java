package sootup.java.codepropertygraph.cdg;

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
import sootup.core.IdentifierFactory;
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
    String methodName = "ifStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    writeGraph(cdgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCdgForIfElseStatement() {
    String methodName = "ifElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    writeGraph(cdgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    String methodName = "ifElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);
    writeGraph(cdgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    String methodName = "ifElseCascadingElseIfStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);

    writeGraph(cdgGraph.toDotFormat(), methodName);
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    String methodName = "ifElseCascadingElseIfInElseStatement";
    Optional<? extends SootMethod> method = getMinimalTestSuiteMethod(methodName);
    assertTrue(method.isPresent());
    MethodCdg cdg = new MethodCdg(method.get());
    CdgGraph cdgGraph = CdgToGraphConverter.convert(cdg);

    writeGraph(cdgGraph.toDotFormat(), methodName);
  }

  private void writeGraph(String dotGraph, String methodName) {
    System.out.println(methodName);
    System.out.println(dotGraph);

    File file = new File("temp/cdg_" + methodName + ".dot");
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
