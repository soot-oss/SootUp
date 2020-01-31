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
public class GenTypeParamTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "geneTypeParamDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: GenTypeParam",
            "$stack4 = new java/util/ArrayList",
            "specialinvoke $stack4.<java.util.ArrayList: void <init>(int)>(3)",
            "l1 = $stack4",
            "$stack5 = newarray (java/lang/Integer)[3]",
            "$stack6 = 0",
            "$stack7 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(1)",
            "$stack8 = $stack6[$stack7]", // Ideally $stack8[$stack6] = $stack7
            "$stack9 = 1",
            "$stack10 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack11 = $stack9[$stack10]",
            "$stack12 = 2",
            "$stack13 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack14 = $stack12[$stack13]",
            "$stack15 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack14)",
            "l2 = $stack15",
            "$stack16 = new GenTypeParam",
            "specialinvoke $stack16.<GenTypeParam: void <init>()>()",
            "l3 = $stack16",
            "virtualinvoke l3.<GenTypeParam: void copy(java.util.List,java.util.List)>(l1, l2)",
            "$stack17 = <java.lang.System: java.io.PrintStream out>",
            "$stack18 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack19 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack20 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack21 = virtualinvoke l3.<GenTypeParam: java.lang.Number largestNum(java.lang.Number,java.lang.Number,java.lang.Number)>($stack18, $stack19, $stack20)",
            "virtualinvoke $stack17.<java.io.PrintStream: void println(java.lang.Object)>($stack21)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
