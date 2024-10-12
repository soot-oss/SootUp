package sootup.codepropertygraph;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

public class BenchmarkTestSuiteBase {
  private final JavaView minimalTsView;
  private final JimpleView testResourcesView;

  public BenchmarkTestSuiteBase() {
    String MINIMAL_TEST_SUITE_DIR = "../shared-test-resources/miniTestSuite/java6/binary";
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            MINIMAL_TEST_SUITE_DIR, SourceType.Application, Collections.emptyList()));

    minimalTsView = new JavaView(inputLocations);

    String TEST_RESOURCES_DIR = "src/test/resources";
    testResourcesView =
        new JimpleView(new JimpleAnalysisInputLocation(Paths.get(TEST_RESOURCES_DIR)));
  }

  protected ClassType getClassType(String className) {
    return minimalTsView.getIdentifierFactory().getClassType(className);
  }

  protected MethodSignature getMethodSignature(
      ClassType classType, String methodName, String returnType, List<String> parameters) {
    return minimalTsView
        .getIdentifierFactory()
        .getMethodSignature(classType, methodName, returnType, parameters);
  }

  protected Optional<? extends SootMethod> getMinimalTestSuiteMethod(
      MethodSignature methodSignature) {
    return minimalTsView.getMethod(methodSignature);
  }

  protected Optional<? extends SootMethod> getTestResourcesMethod(MethodSignature methodSignature) {
    return testResourcesView.getMethod(methodSignature);
  }
}
