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
public class AutomaticWideningTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "automaticWidening", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void automaticWidening(){
   * int x = 10;
   * long y = x;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: AutomaticWidening", "i0 = 10", "i1 = i0", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
