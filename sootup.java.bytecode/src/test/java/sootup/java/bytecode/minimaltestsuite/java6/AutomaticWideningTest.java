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
public class AutomaticWideningTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "automaticWidening", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void automaticWidening(){
   * int x = 10;
   * long y = x;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("l0 := @this: AutomaticWidening", "l1 = 10", "l2 = (long) l1", "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
