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
public class VariableDeclarationTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$i0 = 10", "return")
                .collect(Collectors.toList())),
        getMethodSignature("shortVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$i0 = 0", "return")
                .collect(Collectors.toList())),
        getMethodSignature("byteVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$i0 = 97", "return")
                .collect(Collectors.toList())),
        getMethodSignature("charVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$i0 = 512", "return")
                .collect(Collectors.toList())),
        getMethodSignature("intVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$i0 = 123456789", "return")
                .collect(Collectors.toList())),
        getMethodSignature("longVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$f0 = 3.14F", "return")
                .collect(Collectors.toList())),
        getMethodSignature("floatVariable"));

    loadMethod(
        expectedBodyStmts(
            Stream.of("r0 := @this: VariableDeclaration", "$d0 = 1.96969654", "return")
                .collect(Collectors.toList())),
        getMethodSignature("doubleVariable"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts(List<String> jimpleLines) {
    return jimpleLines;
  }
}
