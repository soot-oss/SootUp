package de.upb.swt.soot.java.bytecode.minimaltestsuite.java9;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DynamicInvokeTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "stringConcatenation", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 = \"This test\"",
            "$stack1 = dynamicinvoke makeConcatWithConstants <java.lang.String (java.lang.String)>(l0) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001 is cool\")",
            "l0 = $stack1",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(java.lang.String)>(l0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
