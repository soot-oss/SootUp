package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.interceptors.TrapTightener;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class TrapTightenerTest {
  final JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  final String location =
      Paths.get(System.getProperty("user.dir")).getParent()
          + File.separator
          + "shared-test-resources/interceptors/";
  final Path path = Paths.get(location + "TrapTightenerExamples.class");
  List<BodyInterceptor> interceptors = Arrays.asList();
  List<BodyInterceptor> interceptorsWithTT = Arrays.asList(new TrapTightener());
  PathBasedAnalysisInputLocation inputLocation =
      new ClassFileBasedAnalysisInputLocation(path, "", SourceType.Application, interceptors);
  PathBasedAnalysisInputLocation inputLocationWithTT =
      new ClassFileBasedAnalysisInputLocation(path, "", SourceType.Application, interceptorsWithTT);
  AnalysisInputLocation javaInputLocation = new DefaultRuntimeAnalysisInputLocation();
  JavaView view = new JavaView(Arrays.asList(inputLocation, javaInputLocation));
  JavaView viewTT = new JavaView(Arrays.asList(inputLocationWithTT, javaInputLocation));
  ClassType clazzType = factory.getClassType("TrapTightenerExamples");

  @Test
  public void testExample1() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "example1", "void", Collections.emptyList());
    Body bodyAfterTT = viewTT.getMethod(methodSignature).get().getBody();
    String exceptedBody =
        "{\n"
            + "    TrapTightenerExamples this;\n"
            + "    unknown $stack4, l1, l2, l3;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapTightenerExamples;\n"
            + "    l1 = 1;\n"
            + "    l2 = 0;\n"
            + "    l3 = l1;\n"
            + "\n"
            + "  label1:\n"
            + "    l2 = l3 / l1;\n"
            + "\n"
            + "  label2:\n"
            + "    goto label4;\n"
            + "\n"
            + "  label3:\n"
            + "    $stack4 := @caughtexception;\n"
            + "    l3 = $stack4;\n"
            + "\n"
            + "    throw l3;\n"
            + "\n"
            + "  label4:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.ArithmeticException from label1 to label2 with label3;\n"
            + "}\n";

    assertEquals(exceptedBody, bodyAfterTT.toString());
  }

  @Test
  public void testExample2() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "example2", "void", Collections.emptyList());
    Body bodyAfterTT = viewTT.getMethod(methodSignature).get().getBody();
    String exceptedBody =
        "{\n"
            + "    TrapTightenerExamples this;\n"
            + "    unknown $stack6, l1, l2, l3, l4, l5;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapTightenerExamples;\n"
            + "    l1 = 1;\n"
            + "    l2 = 0;\n"
            + "    l3 = 2;\n"
            + "\n"
            + "  label1:\n"
            + "    l4 = l2 / l1;\n"
            + "\n"
            + "  label2:\n"
            + "    l5 = 0;\n"
            + "\n"
            + "  label3:\n"
            + "    l5 = l1 / l2;\n"
            + "\n"
            + "  label4:\n"
            + "    goto label6;\n"
            + "\n"
            + "  label5:\n"
            + "    $stack6 := @caughtexception;\n"
            + "    l3 = $stack6;\n"
            + "\n"
            + "    throw l3;\n"
            + "\n"
            + "  label6:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.ArithmeticException from label1 to label2 with label5;\n"
            + " catch java.lang.ArithmeticException from label3 to label4 with label5;\n"
            + "}\n";
    assertEquals(exceptedBody, bodyAfterTT.toString());
  }

  @Test
  public void testExample3() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "example3", "void", Collections.emptyList());
    Body bodyAfterTT = viewTT.getMethod(methodSignature).get().getBody();
    String exceptedBody =
        "{\n"
            + "    TrapTightenerExamples this;\n"
            + "    unknown $stack4, l1, l2, l3;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapTightenerExamples;\n"
            + "    l1 = 1;\n"
            + "    l2 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l3 = l2 / l1;\n"
            + "\n"
            + "  label2:\n"
            + "    l1 = l2;\n"
            + "\n"
            + "    goto label4;\n"
            + "\n"
            + "  label3:\n"
            + "    $stack4 := @caughtexception;\n"
            + "    l3 = $stack4;\n"
            + "\n"
            + "    throw l3;\n"
            + "\n"
            + "  label4:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.ArithmeticException from label1 to label2 with label3;\n"
            + "}\n";
    assertEquals(exceptedBody, bodyAfterTT.toString());
  }

  @Test
  public void testExample4() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "example4", "void", Collections.emptyList());
    Body bodyAfterTT = viewTT.getMethod(methodSignature).get().getBody();
    String exceptedBody =
        "{\n"
            + "    TrapTightenerExamples this;\n"
            + "    unknown $stack4, l1, l2, l3;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapTightenerExamples;\n"
            + "    l1 = 1;\n"
            + "    l2 = 0;\n"
            + "    l1 = l2;\n"
            + "\n"
            + "    goto label1;\n"
            + "\n"
            + "  label1:\n"
            + "    return;\n"
            + "}\n";
    assertEquals(exceptedBody, bodyAfterTT.toString());
  }

  @Test
  public void testExample5() {
    MethodSignature methodSignature =
        factory.getMethodSignature(clazzType, "example5", "void", Collections.emptyList());
    Body bodyAfterTT = viewTT.getMethod(methodSignature).get().getBody();
    String exceptedBody =
        "{\n"
            + "    TrapTightenerExamples this;\n"
            + "    unknown $stack7, $stack8, $stack9, l1, l2, l3, l4, l5, l6;\n"
            + "\n"
            + "\n"
            + "    this := @this: TrapTightenerExamples;\n"
            + "    l1 = 1;\n"
            + "    l2 = 0;\n"
            + "    $stack7 = newarray (int)[3];\n"
            + "    $stack7[0] = 1;\n"
            + "    $stack7[1] = 2;\n"
            + "    $stack7[2] = 3;\n"
            + "    l3 = $stack7;\n"
            + "    l4 = 2;\n"
            + "\n"
            + "  label1:\n"
            + "    l5 = l4 / l2;\n"
            + "\n"
            + "  label2:\n"
            + "    l3[3] = l5;\n"
            + "\n"
            + "  label3:\n"
            + "    l6 = 5;\n"
            + "\n"
            + "    goto label6;\n"
            + "\n"
            + "  label4:\n"
            + "    $stack8 := @caughtexception;\n"
            + "    l4 = $stack8;\n"
            + "\n"
            + "    throw l4;\n"
            + "\n"
            + "  label5:\n"
            + "    $stack9 := @caughtexception;\n"
            + "    l4 = $stack9;\n"
            + "\n"
            + "    throw l4;\n"
            + "\n"
            + "  label6:\n"
            + "    return;\n"
            + "\n"
            + " catch java.lang.ArithmeticException from label1 to label2 with label5;\n"
            + " catch java.lang.NullPointerException from label2 to label3 with label4;\n"
            + "}\n";
    assertEquals(exceptedBody, bodyAfterTT.toString());
  }
}
