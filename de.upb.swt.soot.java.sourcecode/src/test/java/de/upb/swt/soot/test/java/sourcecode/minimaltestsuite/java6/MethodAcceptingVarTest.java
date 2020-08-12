package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Hasitha Rajapakse
 * @author Kaustubh Kelkar
 */
@Category(Java8Test.class)
public class MethodAcceptingVarTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("short"));
    assertJimpleStmts(method, expectedBodyStmtsShort());

    method = loadMethod(getMethodSignature("byte"));
    assertJimpleStmts(method, expectedBodyStmtsByte());

    method = loadMethod(getMethodSignature("char"));
    assertJimpleStmts(method, expectedBodyStmtsChar());

    method = loadMethod(getMethodSignature("int"));
    assertJimpleStmts(method, expectedBodyStmtsInt());

    method = loadMethod(getMethodSignature("long"));
    assertJimpleStmts(method, expectedBodyStmtsLong());

    method = loadMethod(getMethodSignature("float"));

    assertJimpleStmts(method, expectedBodyStmtsFloat());

    method = loadMethod(getMethodSignature("double"));
    assertJimpleStmts(method, expectedBodyStmtsDouble());
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable",
        getDeclaredClassSignature(),
        "void",
        Collections.singletonList(datatype));
  }

  public List<String> expectedBodyStmtsShort() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$s0 := @parameter0: short",
            "$s1 = $s0",
            "$s2 = $s0 + 1",
            "$s0 = $s2",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsByte() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$b0 := @parameter0: byte",
            "$b1 = $b0",
            "$b2 = $b0 + 1",
            "$b0 = $b2",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsChar() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar", "$c0 := @parameter0: char", "$c0 = 97", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsInt() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$i0 := @parameter0: int",
            "$i1 = $i0",
            "$i2 = $i0 + 1",
            "$i0 = $i2",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsLong() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$l0 := @parameter0: long",
            "$l0 = 123456777",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsFloat() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar", "$f0 := @parameter0: float", "$f0 = 7.77F", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsDouble() {
    return Stream.of(
            "r0 := @this: MethodAcceptingVar",
            "$d0 := @parameter0: double",
            "$d0 = 1.787777777",
            "return")
        .collect(Collectors.toList());
  }
}
