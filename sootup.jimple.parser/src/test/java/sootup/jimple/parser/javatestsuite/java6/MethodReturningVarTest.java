package sootup.jimple.parser.javatestsuite.java6;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class MethodReturningVarTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 10", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 0", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 97", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 512", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 123456789L", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 3.14F", "return l1")
            .collect(Collectors.toList()));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        Stream.of("l0 := @this: MethodReturningVar", "l1 = 1.96969654", "return l1")
            .collect(Collectors.toList()));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), datatype + "Variable", datatype, Collections.emptyList());
  }
}
