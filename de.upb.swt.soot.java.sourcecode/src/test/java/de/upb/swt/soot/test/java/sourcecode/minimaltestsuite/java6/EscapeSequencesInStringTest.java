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
public class EscapeSequencesInStringTest extends MinimalTestSuiteBase {

  @Test
  public void defaultTest() {
    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash b \\u0008\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslashB"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash t \\t\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslashT"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash n \\n\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslashN"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash f \\f\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslashF"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash r \\r\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslashR"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes double quotes \\\"\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeDoubleQuotes"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes single quote \\\'\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeSingleQuote"));

    loadMethod(
        expectedBodyStmts(
            Stream.of(
                    "r0 := @this: EscapeSequencesInString",
                    "$r1 = \"This escapes backslash \\\\\"",
                    "return")
                .collect(Collectors.toList())),
        getMethodSignature("escapeBackslash"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts(List<String> jimpleLines) {
    return jimpleLines;
  }
}
