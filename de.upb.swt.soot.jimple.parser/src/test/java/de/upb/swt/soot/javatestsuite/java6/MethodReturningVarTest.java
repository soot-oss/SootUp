/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.javatestsuite.java6;

import de.upb.swt.soot.categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.javatestsuite.JimpleTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodReturningVarTest extends JimpleTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0"));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0"));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0"));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0"));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0"));

    method = loadMethod(getMethodSignature("float"));
    assertJimpleStmts(
        method, expectedBodyStmts("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0"));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }
}
