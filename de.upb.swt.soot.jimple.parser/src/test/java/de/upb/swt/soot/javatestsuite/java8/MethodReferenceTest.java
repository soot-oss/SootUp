package de.upb.swt.soot.javatestsuite.java8;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class MethodReferenceTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "methodRefMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** TODO Update the source code when WALA supports lambda expression */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodReference",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Instance Method\")",
            "$r2 = new MethodReference",
            "specialinvoke $r2.<MethodReference: void <init>()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
