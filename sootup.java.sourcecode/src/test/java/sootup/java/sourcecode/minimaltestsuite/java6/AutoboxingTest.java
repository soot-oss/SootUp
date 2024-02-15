/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class AutoboxingTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "autoboxing", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void autoboxing(){
   * int i = 5;
   * i++;
   * Integer j = i;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: Autoboxing",
            "i0 = 5",
            "i1 = i0",
            "i2 = i0 + 1",
            "i0 = i2",
            "i3 = i0",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
