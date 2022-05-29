package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class VariableDeclarationTest extends JimpleTestSuiteBase {

  @Test
  public void test() {

    SootMethod method = loadMethod(getMethodSignature("shortVariable"));

    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 10", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("byteVariable"));

    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 0", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("charVariable"));

    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 97", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("intVariable"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 512", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("longVariable"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 123456789L", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("floatVariable"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 3.14F", "return")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("doubleVariable"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: VariableDeclaration", "l1 = 1.96969654", "return")
            .collect(Collectors.toList()));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }
}
