/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodReturningVarTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 10", "return l1"));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 0", "return l1"));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 97", "return l1"));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 512", "return l1"));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 123456789L", "return l1"));

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(
        method, expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 3.14F", "return l1"));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("l0 := @this: MethodReturningVar", "l1 = 1.96969654", "return l1"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }
}
