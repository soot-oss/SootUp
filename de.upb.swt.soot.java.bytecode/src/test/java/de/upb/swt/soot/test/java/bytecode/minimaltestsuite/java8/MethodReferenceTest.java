package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java8;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodReferenceTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodRefMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** TODO Update the source code when WALA supports lambda expression */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MethodReference",
            "$stack3 = <java.lang.System: java.io.PrintStream; out>",
            "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(\"Instance Method\")",
            "$stack4 = new MethodReference",
            "specialinvoke $stack4.<MethodReference: void <init>()>()",
            "l1 = $stack4",
            "$stack5 = staticinvoke <java.util.Objects: java.lang.Object requireNonNull(java.lang.Object)>(l1)",
            "$stack6 = dynamicinvoke \"display\" <MyInterface display(MethodReference)>(l1) <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(de.upb.swt.soot.core.jimple.common.constant.MethodType@5b8e3cb1, handle: <MethodReference: void methodRefMethod()>, de.upb.swt.soot.core.jimple.common.constant.MethodType@5b8e3cb1)",
            "l2 = $stack6",
            "interfaceinvoke l2.<MyInterface: void display()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
