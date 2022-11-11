/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.Utils;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
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
    return Stream.of("r0 := @this: NullVariable", "$r1 = null", "return")
        .collect(Collectors.toList());
  }

  @Ignore
  public void test() {
    // FIXME see InstructionConverter.convertUnaryOpInstruction(...)
    SootMethod method = loadMethod(getMethodSignature());
    assertEquals(
        "[java.lang.String $r1, NullVariable r0, r0 := @this: NullVariable, $r1 = null, return]",
        Utils.filterJimple(method.getBody().toString()));
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
