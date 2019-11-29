package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class SuperClassTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "superclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SuperClass",
            "l0.<SuperClass: I a> = 10",
            "l0.<SuperClass: I b> = 20",
            "l0.<SuperClass: I c> = 30",
            "l0.<SuperClass: I d> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
