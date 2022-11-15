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
public class GenTypeParamTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "geneTypeParamDisplay", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void geneTypeParamDisplay(){
   * List<Object> output = new ArrayList< Object >(3);
   * List<Integer> input = Arrays.asList(1,2,3);
   * GenTypeParam genTypeParam= new GenTypeParam();
   * genTypeParam.copy(output,input);
   * System.out.println(genTypeParam.largestNum(2,8,3));
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: GenTypeParam",
            "$stack4 = new java.util.ArrayList",
            "specialinvoke $stack4.<java.util.ArrayList: void <init>(int)>(3)",
            "l1 = $stack4",
            "$stack5 = newarray (java.lang.Integer)[3]",
            "$stack6 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(1)",
            "$stack5[0] = $stack6",
            "$stack7 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack5[1] = $stack7",
            "$stack8 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack5[2] = $stack8",
            "l2 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($stack5)",
            "$stack9 = new GenTypeParam",
            "specialinvoke $stack9.<GenTypeParam: void <init>()>()",
            "l3 = $stack9",
            "virtualinvoke l3.<GenTypeParam: void copy(java.util.List,java.util.List)>(l1, l2)",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "$stack13 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack12 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(8)",
            "$stack11 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack14 = virtualinvoke l3.<GenTypeParam: java.lang.Number largestNum(java.lang.Number,java.lang.Number,java.lang.Number)>($stack13, $stack12, $stack11)",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.Object)>($stack14)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
