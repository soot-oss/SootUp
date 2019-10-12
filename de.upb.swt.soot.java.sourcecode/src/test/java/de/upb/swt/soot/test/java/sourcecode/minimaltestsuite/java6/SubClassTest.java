package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class SubClassTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
            "subclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** @returns the method signature needed for second method in testCase */
  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
            "superclassMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void testDemo() {
    loadMethod(expectedBodyStmts1(), getMethodSignature1());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: SubClass",
            "r0.<SubClass: int aa> = 10",
            "r0.<SubClass: int bb> = 20",
            "r0.<SubClass: int cc> = 30",
            "r0.<SubClass: int dd> = 40",
            "return")
            .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: SubClass",
            "specialinvoke r0.<SuperClass: void superclassMethod()>()",
            "r0.<SuperClass: int a> = 100",
            "r0.<SuperClass: int b> = 200",
            "r0.<SuperClass: int c> = 300",
            "return")
            .collect(Collectors.toCollection(ArrayList::new));
  }
}