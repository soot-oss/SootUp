package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java7;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class UnderscoreInIntTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "underscoreInInt", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void underscoreInInt(){
   * int a = 0b0111_1111_1111_1111_1111_1111_1111_1111;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: UnderscoreInInt", "l1 = 2147483647", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
