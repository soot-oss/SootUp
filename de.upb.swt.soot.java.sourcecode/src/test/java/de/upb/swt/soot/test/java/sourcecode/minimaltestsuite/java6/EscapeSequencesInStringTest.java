/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class EscapeSequencesInStringTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash b \\u0008\"",
            "return"),
        getMethodSignature("escapeBackslashB"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash t \\t\"",
            "return"),
        getMethodSignature("escapeBackslashT"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash n \\n\"",
            "return"),
        getMethodSignature("escapeBackslashN"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash f \\f\"",
            "return"),
        getMethodSignature("escapeBackslashF"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash r \\r\"",
            "return"),
        getMethodSignature("escapeBackslashR"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes double quotes \\\"\"",
            "return"),
        getMethodSignature("escapeDoubleQuotes"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes single quote \\\'\"",
            "return"),
        getMethodSignature("escapeSingleQuote"));

    loadMethod(
        expectedBodyStmts(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash \\\\\"",
            "return"),
        getMethodSignature("escapeBackslash"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}
