package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class UncheckedCastTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "uncheckedCastDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: UncheckedCast",
            "$stack3 = newarray (java/lang/Integer)[4]",
            "$stack4 = 0",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(5)",
            "$stack6 = $stack4[$stack5]",
            "$stack7 = 1",
            "$stack8 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack9 = $stack7[$stack8]",
            "$stack10 = 2",
            "$stack11 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(9)",
            "$stack12 = $stack10[$stack11]",
            "$stack13 = 3",
            "$stack14 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(6)",
            "$stack15 = $stack13[$stack14]",
            "$stack16 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack15)",
            "l1 = $stack16",
            "l2 = l1",
            "$stack17 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack17.<java.io.PrintStream: void println(java.lang.Object)>(l2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
