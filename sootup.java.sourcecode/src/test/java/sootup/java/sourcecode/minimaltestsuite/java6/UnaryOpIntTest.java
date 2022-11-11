package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class UnaryOpIntTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodUnaryOpInt", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    /**
     * TODO Do we need to check the type of variable as int?
     * assertTrue(getFields().stream().anyMatch(sootField -> {return
     * sootField.getType().equals("int");}));
     */
  }

  /**
   *
   *
   * <pre>
   *     void methodUnaryOpInt(){
   * int k = i+j;
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: UnaryOpInt",
            "$i0 = r0.<UnaryOpInt: int i>",
            "$i1 = r0.<UnaryOpInt: int j>",
            "$i2 = $i0 + $i1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
