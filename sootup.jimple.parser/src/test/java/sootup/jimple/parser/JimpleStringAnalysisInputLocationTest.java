package sootup.jimple.parser;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import org.junit.Test;
import sootup.core.views.View;

public class JimpleStringAnalysisInputLocationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidInput() {
    String methodStr = "This is not Jimple its just a Sentence.";
    JimpleStringAnalysisInputLocation analysisInputLocation =
        new JimpleStringAnalysisInputLocation(methodStr);
  }

  @Test
  public void test() {

    String methodStr =
        "class DummyClass extends java.lang.Object {\n\t"
            + "void banana(){\n\t\t"
            + "params = new java.security.AlgorithmParameters;\n\t\t"
            + "return;\n\t"
            + "}\n"
            + "}";

    JimpleStringAnalysisInputLocation analysisInputLocation =
        new JimpleStringAnalysisInputLocation(methodStr);

    View view = new JimpleView(Collections.singletonList(analysisInputLocation));
    assertTrue(view.getClass(analysisInputLocation.getClassType()).isPresent());
  }
}
