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
public class UnaryOpIntTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodUnaryOpInt", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  /**
   *
   *
   * <pre>
   * void methodUnaryOpInt(){
   * int k = i+j;
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: UnaryOpInt",
            "$stack2 = l0.<UnaryOpInt: int i>",
            "$stack3 = l0.<UnaryOpInt: int j>",
            "l1 = $stack2 + $stack3",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
