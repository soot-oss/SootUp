package sootup.java.bytecode.minimaltestsuite.java15;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Bastian Haverkamp */
public class MultilineStringsTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "multi", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void multi() {
   *       String html = """
   *               <html>
   *                   <body>
   *                       <p>Hello, world</p>
   *                   </body>
   *               </html>
   *               """;
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MultilineStrings",
            "l1 = \"<html>\\n    <body>\\n        <p>Hello, world</p>\\n    </body>\\n</html>\\n\"",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
