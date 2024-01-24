package sootup.java.codepropertygraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

public class CpgTestSuiteBase {
  private JavaView minimalTsView;
  private JimpleView testResourcesView;

  @org.junit.Before
  public void setUp() {
    String MINIMAL_TEST_SUITE_DIR = "../shared-test-resources/miniTestSuite/java6/binary";
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new JavaClassPathAnalysisInputLocation(MINIMAL_TEST_SUITE_DIR));

    minimalTsView = new JavaView(inputLocations);

    String TEST_RESOURCES_DIR = "src/test/resources";
    testResourcesView =
        new JimpleView(new JimpleAnalysisInputLocation(Paths.get(TEST_RESOURCES_DIR)));
  }

  protected Optional<? extends SootMethod> getMinimalTestSuiteMethod(String methodName) {
    ClassType appClass = minimalTsView.getIdentifierFactory().getClassType("IfElseStatement");
    MethodSignature methodSignature =
        minimalTsView
            .getIdentifierFactory()
            .getMethodSignature(appClass, methodName, "int", Collections.singletonList("int"));
    return minimalTsView.getMethod(methodSignature);
  }

  protected Optional<? extends SootMethod> getTestResourcesMethod(
      String className, String methodName) {
    ClassType appClass = testResourcesView.getIdentifierFactory().getClassType(className);
    MethodSignature methodSignature =
        testResourcesView
            .getIdentifierFactory()
            .getMethodSignature(appClass, methodName, "void", Collections.emptyList());
    return testResourcesView.getMethod(methodSignature);
  }

  protected void writeGraph(String dotGraph, String methodName, String graphType) {
    System.out.printf("[%s] %s\n", graphType, methodName);
    System.out.println(dotGraph);

    writeToFile(dotGraph, methodName, graphType);
  }

  private static void writeToFile(String dotGraph, String methodName, String graphType) {
    File file = new File(String.format("temp/%s_%s.dot", graphType, methodName));
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
