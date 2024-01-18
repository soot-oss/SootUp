package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.ArrayList;
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
public class DeclareIntTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "declareIntMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * void declareIntMethod(){
   * System.out.println(dec);
   * System.out.println(hex);
   * System.out.println(oct);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareInt",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "$stack1 = l0.<DeclareInt: int dec>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(int)>($stack1)",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = l0.<DeclareInt: int hex>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(int)>($stack3)",
            "$stack6 = <java.lang.System: java.io.PrintStream out>",
            "$stack5 = l0.<DeclareInt: int oct>",
            "virtualinvoke $stack6.<java.io.PrintStream: void println(int)>($stack5)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
