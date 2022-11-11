/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
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
            "$i0 = 20",
            "$i1 = 1",
            "label1:",
            "$z0 = $i1 < $i0",
            "if $z0 == 0 goto label3",
            "$i2 = $i1 % 10",
            "$z1 = $i2 == 0",
            "if $z1 == 0 goto label2",
            "goto label3",
            "label2:",
            "$i3 = $i1",
            "$i4 = $i1 + 1",
            "$i1 = $i4",
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
