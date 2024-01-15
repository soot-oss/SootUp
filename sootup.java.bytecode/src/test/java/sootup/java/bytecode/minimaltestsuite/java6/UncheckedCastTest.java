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
public class UncheckedCastTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "uncheckedCastDisplay", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void uncheckedCastDisplay(){
   * List list = Arrays.asList(5,8,9,6);
   * List<Double> intList= list;
   * System.out.println(intList);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: UncheckedCast",
            "$stack3 = newarray (java.lang.Integer)[4]",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(5)",
            "$stack3[0] = $stack4",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack3[1] = $stack5",
            "$stack6 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(9)",
            "$stack3[2] = $stack6",
            "$stack7 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(6)",
            "$stack3[3] = $stack7",
            "l1 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack3)",
            "l2 = l1",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.Object)>(l2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
