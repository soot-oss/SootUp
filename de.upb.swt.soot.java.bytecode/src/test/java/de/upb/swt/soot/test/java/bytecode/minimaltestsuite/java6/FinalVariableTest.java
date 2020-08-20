package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

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
public class FinalVariableTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "finalVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>
   * public void finalVariable() {
   * final int num = 5;
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: FinalVariable", "return").collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
