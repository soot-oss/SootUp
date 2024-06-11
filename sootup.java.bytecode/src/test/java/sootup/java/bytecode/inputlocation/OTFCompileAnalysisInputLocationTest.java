package sootup.java.bytecode.inputlocation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class OTFCompileAnalysisInputLocationTest {

  @Test
  void testSimpleString() {
    String cucontent = "public class A { }\n";
    OTFCompileAnalysisInputLocation inputLocation =
        new OTFCompileAnalysisInputLocation("A.java", cucontent);
  }

  @Test
  void testMismatchingString() {
    // should fail
    String fileName = "B.java";
    String cucontent = "public class A { }\n";
    assertThrows(
        IllegalArgumentException.class,
        () -> new OTFCompileAnalysisInputLocation(fileName, cucontent));
  }

  @Test
  void testString() {
    String cucontent =
        "public class FieldAssignment {\n"
            + "    private static class A {\n"
            + "        String s;\n"
            + "    }\n"
            + "\n"
            + "    public static void entry() {\n"
            + "        A a = new A();\n"
            + "        String b = \"abc\";\n"
            + "\n"
            + "        a.s = b;\n"
            + "    }\n"
            + "}\n";
    OTFCompileAnalysisInputLocation inputLocation =
        new OTFCompileAnalysisInputLocation("FieldAssignment.java", cucontent);
  }

  @Test
  void testSingleInputfile() {
    String str = "../shared-test-resources/TypeResolverTestSuite/Misc/FieldAssignment.java";
    OTFCompileAnalysisInputLocation inputLocation =
        new OTFCompileAnalysisInputLocation(Paths.get(str));
  }
}
