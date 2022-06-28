package de.upb.swt.soot.java.bytecode.minimaltestsuite.java8;

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

/** @author Kaustubh Kelkar, Bastian Haverkamp */
@Category(Java8Test.class)
public class MethodAcceptingLamExprTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "lambdaAsParamMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
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
            "$stack2 = dynamicinvoke calcPercentage <Percentage ()>() <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(methodtype: double __METHODTYPE__(double), handle: <MethodAcceptingLamExpr: double lambda$lambdaAsParamMethod$0(double)>, methodtype: double __METHODTYPE__(double))",
            "l1 = $stack2",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = new java.lang.StringBuilder",
            "specialinvoke $stack3.<java.lang.StringBuilder: void <init>()>()",
            "$stack5 = virtualinvoke $stack3.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"Percentage : \")",
            "$stack6 = interfaceinvoke l1.<Percentage: double calcPercentage(double)>(45.0)",
            "$stack7 = virtualinvoke $stack5.<java.lang.StringBuilder: java.lang.StringBuilder append(double)>($stack6)",
            "$stack8 = virtualinvoke $stack7.<java.lang.StringBuilder: java.lang.String toString()>()",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack8)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}