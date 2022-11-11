package sootup.jimple.parser.javatestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class GenTypeParamTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "geneTypeParamDisplay", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: GenTypeParam",
            "$stack4 = new java.util.ArrayList",
            "specialinvoke $stack4.<java.util.ArrayList: void <init>(int)>(3)",
            "l1 = $stack4",
            "$stack5 = newarray (java.lang.Integer)[3]",
            "$stack6 = 0",
            "$stack7 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(1)",
            "$stack5[$stack6] = $stack7",
            "$stack8 = 1",
            "$stack9 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack5[$stack8] = $stack9",
            "$stack10 = 2",
            "$stack11 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack5[$stack10] = $stack11",
            "$stack12 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack5)",
            "l2 = $stack12",
            "$stack13 = new GenTypeParam",
            "specialinvoke $stack13.<GenTypeParam: void <init>()>()",
            "l3 = $stack13",
            "virtualinvoke l3.<GenTypeParam: void copy(java.util.List,java.util.List)>(l1, l2)",
            "$stack14 = <java.lang.System: java.io.PrintStream out>",
            "$stack15 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack16 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack17 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack18 = virtualinvoke l3.<GenTypeParam: java.lang.Number largestNum(java.lang.Number,java.lang.Number,java.lang.Number)>($stack15, $stack16, $stack17)",
            "virtualinvoke $stack14.<java.io.PrintStream: void println(java.lang.Object)>($stack18)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
