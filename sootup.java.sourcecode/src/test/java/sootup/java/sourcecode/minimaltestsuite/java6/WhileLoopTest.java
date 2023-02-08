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
public class WhileLoopTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "whileLoop", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void whileLoop(){
   * int num = 10;
   * int i = 0;
   * while(num>i){
   * num--;
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: WhileLoop",
            "$i0 = 10",
            "$i1 = 0",
            "label1:",
            "$z0 = $i0 > $i1",
            "if $z0 == 0 goto label2",
            "$i2 = $i0",
            "$i3 = $i0 - 1",
            "$i0 = $i3",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
