/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class EscapeSequencesInStringTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature("escapeBackslashB"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash b \\u0008\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeBackslashT"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash t \\t\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeBackslashN"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash n \\n\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeBackslashF"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash f \\f\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeBackslashR"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash r \\r\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeDoubleQuotes"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes double quotes \\\"\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeSingleQuote"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes single quote \\\'\"",
            "return"));

    method = loadMethod(getMethodSignature("escapeBackslash"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash \\\\\"",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
