package sootup.java.sourcecode.minimaltestsuite.java7;

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

/** @author: Hasitha Rajapakse * */
@Category(Java8Test.class)
public class BinaryLiteralInIntTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "binaryLiteralInInt", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void binaryLiteralInInt(){
   * int a = 0b10100001010001011010000101000101;
   * int b = 0b101;
   * int c = 0B101;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: BinaryLiteralInInt", "$i0 = -1589272251", "$i1 = 5", "$i2 = 5", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
