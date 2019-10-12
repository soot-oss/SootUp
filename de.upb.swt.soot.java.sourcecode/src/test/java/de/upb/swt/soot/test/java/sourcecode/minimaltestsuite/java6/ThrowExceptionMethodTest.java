package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class ThrowExceptionMethodTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divide", getDeclaredClassSignature(), "void", Collections.emptyList());
  } // How to check for exceptions in method signature

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$s0 := 8/0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$s0 := 8/0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
