package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

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
    assertJimpleStmts(method, expectedBodyStmtsEscapeSingleQuote());

    method = loadMethod(getMethodSignature("escapeBackslash"));
    assertJimpleStmts(method, expectedBodyStmtsEscapeBackslash());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeBackslashB(){
   *         String str = "This escapes backslash b \b";
   *     }
   *     </pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslashB() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash b \\u0008\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeBackslashT(){
   *         String str = "This escapes backslash t \t";
   *     }</pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslashT() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash t \\t\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeBackslashN(){
   *         String str = "This escapes backslash n \n";
   *     }</pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslashN() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash n \\n\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeBackslashF(){
   *         String str = "This escapes backslash f \f";
   *     }
   *     </pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslashF() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash f \\f\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void escapeBackslashR(){
   *         String str = "This escapes backslash r \r";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslashR() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash r \\r\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeDoubleQuotes(){
   *         String str = "This escapes double quotes \"";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsEscapeDoubleQuotes() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes double quotes \\\"\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeSingleQuote(){
   *         String str = "This escapes single quote \'";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsEscapeSingleQuote() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes single quote \\\'\"",
            "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void escapeBackslash(){
   *         String str = "This escapes backslash \\";
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsEscapeBackslash() {
    return Stream.of(
            "r0 := @this: EscapeSequencesInString",
            "$r1 = \"This escapes backslash \\\\\"",
            "return")
        .collect(Collectors.toList());
  }
}
