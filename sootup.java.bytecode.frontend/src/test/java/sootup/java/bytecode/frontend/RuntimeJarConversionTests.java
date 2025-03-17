package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.transform.BodyInterceptorMetric;
import sootup.core.transform.RunTimeBodyInterceptor;
import sootup.core.util.Utils;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.interceptors.CopyPropagator;
import sootup.interceptors.DeadAssignmentEliminator;
import sootup.interceptors.TypeAssigner;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class RuntimeJarConversionTests {

  @Test
  public void testJarWithDefaultInterceptors() {
    AnalysisInputLocation inputLocation =
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library);
    convertInputLocation(inputLocation);
  }

  private static void convertInputLocation(AnalysisInputLocation inputLocation) {
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    int[] failedConversions = {0};
    long[] count = {0};
    view.getClasses()
        .filter(t -> t.getType().getPackageName().toString().startsWith("java.lang"))
        .flatMap(c -> c.getMethods().stream())
        .filter(SootMethod::isConcrete)
        .forEach(
            javaSootMethod -> {
              try {
                count[0]++;
                javaSootMethod.getBody();
              } catch (Exception e) {
                failedConversions[0]++;
              }
            });
    assertTrue(count[0] > 0);
    assertEquals(0, failedConversions[0]);
  }

  @Disabled
  @Test
  public void testJar() {
    AnalysisInputLocation inputLocation =
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library, Collections.emptyList());
    convertInputLocation(inputLocation);
  }

  /** helps debugging the conversion of a single method */
  static BiFunction<BodyInterceptor, Body.BodyBuilder, Boolean> step =
      (interceptor, builder) ->
          interceptor.getClass() == CopyPropagator.class
              || interceptor.getClass() == DeadAssignmentEliminator.class;

  static List<BodyInterceptor> bodyInterceptors =
      Utils.wrapEachBodyInterceptorWith(
          BytecodeBodyInterceptors.Default.getBodyInterceptors(), step);

  private static void convertMethod(String methodSignature) {
    AnalysisInputLocation inputLocation =
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library, bodyInterceptors);
    convertMethod(methodSignature, inputLocation);
  }

  private static void convertMethod(String methodSignature, AnalysisInputLocation inputLocation) {

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final SootMethod sootMethod =
        view.getMethod(view.getIdentifierFactory().parseMethodSignature(methodSignature))
            .orElse(null);
    assertNotNull(sootMethod);
    sootMethod.getBody();
  }

  @Disabled
  @Test
  public void testExample() {
    /* Example to start quickly */
    convertMethod("<java.awt.GraphicsEnvironment: java.awt.GraphicsEnvironment createGE()>");
  }

  /** e.g. to measure Runtime (Time and Memory Usage) of every interceptor */
  @Test
  public void runTimeOfBodyInterceptorOnJar() {
    // Note: mrjar.jar used just for test purpose, you can put any jar file.
    String baseDir = "../shared-test-resources/multi-release-jar/mrjar.jar";
    // List<BodyInterceptor> bodyInterceptorsList =
    // BytecodeBodyInterceptors.Default.getBodyInterceptors();
    List<BodyInterceptor> bodyInterceptorsList =
        Arrays.asList(new TypeAssigner(), new CopyPropagator());
    List<RunTimeBodyInterceptor> runTimeBodyInterceptorsList = new ArrayList<>();
    for (BodyInterceptor bodyInterceptor : bodyInterceptorsList) {
      RunTimeBodyInterceptor runTimeBodyInterceptor = new RunTimeBodyInterceptor(bodyInterceptor);
      runTimeBodyInterceptorsList.add(runTimeBodyInterceptor);
    }
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            baseDir, SourceType.Library, Collections.unmodifiableList(runTimeBodyInterceptorsList));
    JavaView view = new JavaView(inputLocation);
    view.getClasses()
        .forEach(javaSootClass -> javaSootClass.getMethods().forEach(SootMethod::getBody));
    runTimeBodyInterceptorsList.forEach(
        runTimeBodyInterceptor -> {
          BodyInterceptorMetric biMetric = runTimeBodyInterceptor.getBiMetric();
          System.out.println(
              runTimeBodyInterceptor.getBodyInterceptor()
                  + " took "
                  + biMetric.getRuntime()
                  + " ms.");
          System.out.println(
              runTimeBodyInterceptor.getBodyInterceptor()
                  + " consumed "
                  + biMetric.getMemoryUsage()
                  + " MB.");
        });
  }
}
