package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class StaticInitializerTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "staticMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = r0.<StaticVariable: int i>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"Static Block called\")",
            "return")
        .collect(Collectors.toList());
  }
}
