package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class MethodAcceptingVarTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: short",
            "$stack2 = l1 + 1",
            "l1 = (short) $stack2",
            "return"));

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: byte",
            "$stack2 = l1 + 1",
            "l1 = (byte) $stack2",
            "return"));

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: char", "l1 = 97", "return"));

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: int", "l1 = l1 + 1", "return"));

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: long",
            "l1 = 123456777L",
            "return"));

    method = loadMethod(getMethodSignature("float"));

    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar", "l1 := @parameter0: float", "l1 = 7.77F", "return"));

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: MethodAcceptingVar",
            "l1 := @parameter0: double",
            "l1 = 1.787777777",
            "return"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList(datatype));
  }
}
