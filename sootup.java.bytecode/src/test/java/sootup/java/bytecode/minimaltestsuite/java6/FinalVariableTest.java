package sootup.java.bytecode.minimaltestsuite.java6;

import categories.TestCategories;
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
public class FinalVariableTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "finalVariable", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void finalVariable() {
   * final int num = 5;
   * }
   *
   * </pre>
   */
  // FIXME: test does not test what is should as the class does not contain a variable at all.. it
  // needs a use like in sout
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("this := @this: FinalVariable", "return").collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
