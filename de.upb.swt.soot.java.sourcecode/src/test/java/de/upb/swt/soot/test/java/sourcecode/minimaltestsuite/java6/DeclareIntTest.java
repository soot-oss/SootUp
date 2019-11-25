package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class DeclareIntTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "declareIntMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareInt",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = r0.<DeclareInt: int dec>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$i1 = r0.<DeclareInt: int hex>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i1)",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "$i2 = r0.<DeclareInt: int oct>",
            "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i2)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
