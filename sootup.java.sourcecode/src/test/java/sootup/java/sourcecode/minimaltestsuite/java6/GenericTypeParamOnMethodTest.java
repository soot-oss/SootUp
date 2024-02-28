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

/** @author: Hasitha Rajapakse * */
@Tag("Java8")
public class GenericTypeParamOnMethodTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "genericTypeParamOnMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void genericTypeParamOnMethod() {
   * a("Hello World");
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: GenericTypeParamOnMethod",
            "virtualinvoke r0.<GenericTypeParamOnMethod: void a(java.lang.Object)>(\"Hello World\")",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
