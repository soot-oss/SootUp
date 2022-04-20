package de.upb.swt.soot.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class UncheckedCastTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "uncheckedCastDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
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
            "$stack4 = 0",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(5)",
            "$stack3[$stack4] = $stack5",
            "$stack6 = 1",
            "$stack7 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack3[$stack6] = $stack7",
            "$stack8 = 2",
            "$stack9 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(9)",
            "$stack3[$stack8] = $stack9",
            "$stack10 = 3",
            "$stack11 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(6)",
            "$stack3[$stack10] = $stack11",
            "$stack12 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack3)",
            "l1 = $stack12",
            "l2 = l1",
            "$stack13 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack13.<java.io.PrintStream: void println(java.lang.Object)>(l2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
