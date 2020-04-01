package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SubClassTest extends MinimalBytecodeTestSuiteBase {
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
  public void testSuperClassStmts() {
    SootMethod m = loadMethod(getMethodSignature1());
    assertJimpleStmts(m, expectedBodyStmts1());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getSuperclass().get().getClassName().equals("SuperClass"));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SubClass",
            "l0.<SubClass: int aa> = 10",
            "l0.<SubClass: int bb> = 20",
            "l0.<SubClass: int cc> = 30",
            "l0.<SubClass: int dd> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "l0 := @this: SubClass",
            "specialinvoke l0.<SuperClass: void superclassMethod()>()",
            "l0.<SubClass: int a> = 100",
            "l0.<SubClass: int b> = 200",
            "l0.<SubClass: int c> = 300",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
