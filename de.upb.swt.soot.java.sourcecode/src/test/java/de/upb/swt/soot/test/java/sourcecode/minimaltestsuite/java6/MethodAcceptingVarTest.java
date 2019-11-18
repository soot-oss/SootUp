/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodAcceptingVarTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "a := @parameter0: short",
            "$s0 = a",
            "$s1 = a + 1",
            "a = $s1",
            "return"),
        getMethodSignature("short"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "b := @parameter0: byte",
            "$b0 = b",
            "$b1 = b + 1",
            "b = $b1",
            "return"),
        getMethodSignature("byte"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar", "c := @parameter0: char", "c = 97", "return"),
        getMethodSignature("char"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "d := @parameter0: int",
            "$i0 = d",
            "$i1 = d + 1",
            "d = $i1",
            "return"),
        getMethodSignature("int"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar", "e := @parameter0: long", "e = 123456777", "return"),
        getMethodSignature("long"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar", "f := @parameter0: float", "f = 7.77F", "return"),
        getMethodSignature("float"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: MethodAcceptingVar",
            "g := @parameter0: double",
            "g = 1.787777777",
            "return"),
        getMethodSignature("double"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList(datatype));
  }
}
