/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodReturningVarTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0")
                .collect(Collectors.toList())),
        getMethodSignature("short"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0")
                .collect(Collectors.toList())),
        getMethodSignature("byte"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0")
                .collect(Collectors.toList())),
        getMethodSignature("char"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0")
                .collect(Collectors.toList())),
        getMethodSignature("int"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0")
                .collect(Collectors.toList())),
        getMethodSignature("long"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0")
                .collect(Collectors.toList())),
        getMethodSignature("float"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0")
                .collect(Collectors.toList())),
        getMethodSignature("double"));
  }

  public MethodSignature getMethodSignature(String datatype) {
    return identifierFactory.getMethodSignature(
        datatype + "Variable", getDeclaredClassSignature(), datatype, Collections.emptyList());
  }

  public List<String> expectedBodyStmts(List<String> jimpleLines) {
    return jimpleLines;
  }
}
