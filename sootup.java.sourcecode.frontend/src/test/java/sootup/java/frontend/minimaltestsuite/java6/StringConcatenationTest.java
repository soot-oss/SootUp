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
public class StringConcatenationTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "stringConcatenation", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void stringConcatenation(){
   * String str = "the" + "string";
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: StringConcatenation",
            "r2 = new java.lang.StringBuilder",
            "specialinvoke r2.<java.lang.StringBuilder: void <init>(java.lang.String)>(\"the\")",
            "r3 = virtualinvoke r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"string\")",
            "r1 = virtualinvoke r3.<java.lang.StringBuilder: java.lang.StringBuilder toString()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
