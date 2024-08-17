package sootup.java.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.util.DotExporter;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.core.interceptors.BytecodeBodyInterceptors;
import sootup.java.core.interceptors.CopyPropagator;
import sootup.java.core.interceptors.DeadAssignmentEliminator;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class RuntimeJarConversionTests {
  private static boolean debug = false;

  @Test
  public void testJarWithDefaultInterceptors() {
    AnalysisInputLocation inputLocation = new DefaultRTJarAnalysisInputLocation(SourceType.Library);
    convertInputLocation(inputLocation);
  }

  private static void convertInputLocation(AnalysisInputLocation inputLocation) {
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    long classesCount = view.getClasses().count();
    if (debug) {
      System.out.println("classes: " + classesCount);
    }
    int[] failedConversions = {0};
    long[] progress = {0};

    long count =
        view.getClasses()
            .peek(
                c -> {
                  if (!debug) {
                    return;
                  }
                  System.out.println(
                      "converted classes: "
                          + progress[0]
                          + "  failed: "
                          + failedConversions[0]
                          + " - progress "
                          + ((double) progress[0]++ / classesCount));
                })
            .flatMap(c -> c.getMethods().stream())
            .filter(SootMethod::isConcrete)
            .peek(
                javaSootMethod -> {
                  try {
                    javaSootMethod.getBody();
                  } catch (Exception e) {
                    e.printStackTrace();
                    failedConversions[0]++;
                  }
                })
            .count();
    assertTrue(count > 0);
    assertEquals(0, failedConversions[0]);
  }

  // @Test
  public void testJar() {
    AnalysisInputLocation inputLocation =
        new DefaultRTJarAnalysisInputLocation(SourceType.Library, Collections.emptyList());
    convertInputLocation(inputLocation);
  }

  /**
   * helps debugging the conversion of a single method
   *
   * @return
   */
  static BiFunction<BodyInterceptor, Body.BodyBuilder, Boolean> step =
      (interceptor, builder) -> {
        if (interceptor.getClass() != CopyPropagator.class
            && interceptor.getClass() != DeadAssignmentEliminator.class) {
          return false;
        }
        if (debug) {
          System.out.println(DotExporter.createUrlToWebeditor(builder.getStmtGraph()));
        }
        return true;
      };

  static List<BodyInterceptor> bodyInterceptors =
      Utils.wrapEachBodyInterceptorWith(
          BytecodeBodyInterceptors.Default.getBodyInterceptors(), step);

  private static Body convertMethod(String methodSignature) {
    AnalysisInputLocation inputLocation =
        new DefaultRTJarAnalysisInputLocation(SourceType.Library, bodyInterceptors);
    return convertMethod(methodSignature, inputLocation);
  }

  private static Body convertMethod(String methodSignature, AnalysisInputLocation inputLocation) {

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final SootMethod sootMethod =
        view.getMethod(view.getIdentifierFactory().parseMethodSignature(methodSignature)).get();
    return sootMethod.getBody();
  }

  @Disabled
  @Test
  public void testExample() {
    /* Example to start quickly */
    convertMethod("<java.awt.GraphicsEnvironment: java.awt.GraphicsEnvironment createGE()>");
  }
}
