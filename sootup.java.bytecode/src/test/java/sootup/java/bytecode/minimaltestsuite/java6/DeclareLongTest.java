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
public class DeclareLongTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "declareLongMethod", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * void declareLongMethod(){
   * System.out.println(l1);
   * System.out.println(l2);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareLong",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "$stack1 = l0.<DeclareLong: long l1>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(long)>($stack1)",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = l0.<DeclareLong: long l2>",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(long)>($stack3)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
