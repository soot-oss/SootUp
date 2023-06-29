package sootup.java.bytecode.minimaltestsuite.java8;

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

/** @author Kaustubh Kelkar, Bastian Haverkamp */
@Category(Java8Test.class)
public class MethodAcceptingLamExprTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "lambdaAsParamMethod", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  /**
   *
   *
   * <pre>
   * public void lambdaAsParamMethod(){
   * //        Percentage percentageValue = (value -> value/100);
   * //        System.out.println("Percentage : " + percentageValue.calcPercentage(45.0));
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MethodAcceptingLamExpr",
            "l1 = dynamicinvoke \"calcPercentage\" <Percentage ()>() <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(methodtype: double __METHODTYPE__(double), handle: <MethodAcceptingLamExpr: double lambda$lambdaAsParamMethod$0(double)>, methodtype: double __METHODTYPE__(double))",
            "$stack3 = <java.lang.System: java.io.PrintStream out>",
            "$stack2 = new java.lang.StringBuilder",
            "specialinvoke $stack2.<java.lang.StringBuilder: void <init>()>()",
            "$stack5 = virtualinvoke $stack2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"Percentage : \")",
            "$stack4 = interfaceinvoke l1.<Percentage: double calcPercentage(double)>(45.0)",
            "$stack6 = virtualinvoke $stack5.<java.lang.StringBuilder: java.lang.StringBuilder append(double)>($stack4)",
            "$stack7 = virtualinvoke $stack6.<java.lang.StringBuilder: java.lang.String toString()>()",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>($stack7)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
