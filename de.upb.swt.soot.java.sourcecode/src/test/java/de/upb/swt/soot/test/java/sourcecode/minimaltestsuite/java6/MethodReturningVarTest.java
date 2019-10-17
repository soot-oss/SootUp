/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodReturningVarTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0"),
        getMethodSignature("short"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0"),
        getMethodSignature("byte"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0"),
        getMethodSignature("char"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0"),
        getMethodSignature("int"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0"),
        getMethodSignature("long"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0"),
        getMethodSignature("float"));

    loadMethod(
        expectedBodyStmts("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0"),
        getMethodSignature("double"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }
}
