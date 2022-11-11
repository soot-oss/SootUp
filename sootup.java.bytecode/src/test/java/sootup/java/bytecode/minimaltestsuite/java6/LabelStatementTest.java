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
public class LabelStatementTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "labelStatement", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void labelStatement(){
   * int num = 20;
   * int i = 1;
   * start:
   * while (i<num){
   * if ( i % 10 == 0 )
   * break start;
   * else
   * i++;
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: LabelStatement",
            "l1 = 20",
            "l2 = 1",
            "label1:",
            "$stack5 = l2",
            "$stack4 = l1",
            "if $stack5 >= $stack4 goto label3",
            "$stack3 = l2 % 10",
            "if $stack3 != 0 goto label2",
            "goto label3",
            "label2:",
            "l2 = l2 + 1",
            "goto label1",
            "label3:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
