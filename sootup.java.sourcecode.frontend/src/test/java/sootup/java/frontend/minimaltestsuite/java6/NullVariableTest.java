/** @author: Hasitha Rajapakse */
package sootup.java.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.Utils;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

@Tag("Java8")
public class NullVariableTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "nullVariable", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void nullVariable(){
   * String str = null;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: NullVariable", "r1 = null", "return")
        .collect(Collectors.toList());
  }

  @Disabled
  public void test() {
    // FIXME see InstructionConverter.convertUnaryOpInstruction(...)
    SootMethod method = loadMethod(getMethodSignature());
    assertEquals(
        "[java.lang.String r1, NullVariable r0, r0 := @this: NullVariable, r1 = null, return]",
        Utils.filterJimple(method.getBody().toString()));
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
