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
public class EscapeSequencesInStringTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("escapeBackslashB"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslashB());

    method = loadMethod(getMethodSignature("escapeBackslashT"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslashT());

    method = loadMethod(getMethodSignature("escapeBackslashN"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslashN());

    method = loadMethod(getMethodSignature("escapeBackslashF"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslashF());

    method = loadMethod(getMethodSignature("escapeBackslashR"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslashR());

    method = loadMethod(getMethodSignature("escapeDoubleQuotes"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeDoubleQuotes());

    method = loadMethod(getMethodSignature("escapeSingleQuote"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeSingleQuotes());

    method = loadMethod(getMethodSignature("escapeBackslash"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslash());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmtsEscapeBackslashB() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash b \\u0008\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeBackslashT() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash t \\t\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeBackslashN() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash n \\n\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeBackslashF() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash f \\f\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeBackslashR() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash r \\r\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeDoubleQuotes() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes double quotes \\\"\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeSingleQuotes() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes single quote \\\'\"",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsEscapeBackslash() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash \\\\\"",
            "return")
        .collect(Collectors.toList());
  }
}
