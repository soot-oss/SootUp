package sootup.java.bytecode;


import java.util.Arrays;
import java.util.List;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.Body;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixMissingTraphandlerPredecessor {
  final String directory = "../shared-test-resources/bugfixes/";

  @Test
  public void testMissingTrapHandlerPredecessor() {
    AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(directory);
    JavaView view = new JavaView(inputLocation);
    Body body =
        view.getMethod(
                view.getIdentifierFactory()
                    .parseMethodSignature("<MissingTrapHandlerAssignment: long run()>"))
            .get()
            .getBody();

    List<String> expectedStmts =
        Arrays.asList(
            "l0 := @this: MissingTrapHandlerAssignment",
            "label1:",
            // It's important that this assignment doesn't get inlined into the return statement
            // below, because the trap handler (try/catch) at the bottom would be broken by that
            "$stack2 = 1L",
            "label2:",
            "return $stack2",
            "label3:",
            "$stack3 := @caughtexception",
            "l1 = $stack3",
            "return 2L",
            "catch java.lang.Throwable from label1 to label2 with label3");

    assertEquals(Utils.bodyStmtsAsStrings(body), expectedStmts);
  }
}
