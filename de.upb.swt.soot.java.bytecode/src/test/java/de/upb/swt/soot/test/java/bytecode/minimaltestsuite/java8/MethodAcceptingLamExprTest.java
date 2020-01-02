package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java8;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class MethodAcceptingLamExprTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "lambdaAsParamMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {}

  @Ignore
  public void ignoreTest() {
    /** MethodType is not working/ not implemented yet */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MethodAcceptingLamExpr",
            "$stack2 = dynamicinvoke \"calcPercentage\" <Percentage calcPercentage()>() <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(de.upb.swt.soot.core.jimple.common.constant.MethodType@49060a52, handle: <MethodAcceptingLamExpr: double lambda$lambdaAsParamMethod$0(double)>, de.upb.swt.soot.core.jimple.common.constant.MethodType@49060a52)",
            "l1 = $stack2",
            "$stack3 = <java.lang.System: java.io.PrintStream; out>",
            "$stack4 = interfaceinvoke l1.<Percentage: double calcPercentage(double)>(45.0)",
            "$stack5 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String makeConcatWithConstants(double)>($stack4) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"Percentage : \u0001\")",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
