package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class GenericTypeParamOnClassTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "genericTypeParamOnClass", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void genericTypeParamOnClass() {
   * A<Integer> a = new A<Integer>();
   * a.set(5);
   * int x = a.get();
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: GenericTypeParamOnClass",
            "$stack3 = new GenericTypeParamOnClass$A",
            "specialinvoke $stack3.<GenericTypeParamOnClass$A: void <init>(GenericTypeParamOnClass)>(l0)",
            "l1 = $stack3",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(5)",
            "staticinvoke <GenericTypeParamOnClass$A: void access$000(GenericTypeParamOnClass$A,java.lang.Object)>(l1, $stack4)",
            "$stack5 = virtualinvoke l1.<GenericTypeParamOnClass$A: java.lang.Object get()>()",
            "$stack6 = (java.lang.Integer) $stack5",
            "l2 = virtualinvoke $stack6.<java.lang.Integer: int intValue()>()",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
