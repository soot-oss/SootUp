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
public class ContinueInWhileLoopTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "continueInWhileLoop", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**  <pre>
   * public void continueInWhileLoop(){
   * int num = 0;
   * while (num < 10) {
   * if (num == 5) {
   * num++;
   * continue;
   * }
   *
   * <pre>*/
  /**  <pre>
   * public void whileLoop(){
   * int num = 10;
   * int i = 0;
   * while(num>i){
   * num--;
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: ContinueInWhileLoop",
            "l1 = 0",
            "label1:",
            "$stack3 = l1",
            "$stack2 = 10",
            "if $stack3 >= $stack2 goto label3",
            "if l1 != 5 goto label2",
            "l1 = l1 + 1",
            "goto label1",
            "label2:",
            "l1 = l1 + 1",
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
