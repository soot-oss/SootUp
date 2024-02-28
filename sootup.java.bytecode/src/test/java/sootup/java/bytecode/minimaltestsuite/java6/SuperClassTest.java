package sootup.java.bytecode.minimaltestsuite.java6;

import categories.TestCategories;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag(TestCategories.JAVA_8_CATEGORY)
public class SuperClassTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "superclassMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void superclassMethod() {
   * a=10;
   * b=20;
   * c=30;
   * d=40;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: SuperClass",
            "this.<SuperClass: int a> = 10",
            "this.<SuperClass: int b> = 20",
            "this.<SuperClass: int c> = 30",
            "this.<SuperClass: int d> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
