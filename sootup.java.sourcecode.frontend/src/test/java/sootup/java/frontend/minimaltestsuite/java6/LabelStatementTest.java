/** @author: Hasitha Rajapakse */
package sootup.java.frontend.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class LabelStatementTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "labelStatement", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void labelStatement(){
   * int num = 20;
   * int i = 1;
   * start:
   * while (i<num){
   * if ( i % 10 == 0 )
   * break start;
   * else
   * i++;
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: LabelStatement",
            "i0 = 20",
            "i1 = 1",
            "label1:",
            "z0 = i1 < i0",
            "if z0 == 0 goto label3",
            "i2 = i1 % 10",
            "z1 = i2 == 0",
            "if z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "i3 = i1",
            "i4 = i1 + 1",
            "i1 = i4",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
