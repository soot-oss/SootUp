package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
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
public class CharLiteralsTest extends MinimalSourceTestSuiteBase {

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature("charCharacter"));
    assertJimpleStmts(method, expectedBodyStmtsCharCharacter());

    method = loadMethod(getMethodSignature("charSymbol"));
    assertJimpleStmts(method, expectedBodyStmtsCharSymbol());

    method = loadMethod(getMethodSignature("charBackslashT"));
    assertJimpleStmts(method, expectedBodyStmtsCharBackslashT());

    method = loadMethod(getMethodSignature("charBackslash"));
    assertJimpleStmts(method, expectedBodyStmtsCharBackslash());

    method = loadMethod(getMethodSignature("charSingleQuote"));
    assertJimpleStmts(method, expectedBodyStmtsCharSingleQuote());

    method = loadMethod(getMethodSignature("charUnicode"));
    assertJimpleStmts(method, expectedBodyStmtsCharUnicode());

    method = loadMethod(getMethodSignature("specialChar"));
    assertJimpleStmts(method, expectedBodyStmtsSpecialChar());
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), methodName, "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     public void charCharacter(){
   *         char val = 'a';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharCharacter() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 97", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charSymbol(){
   *         char val = '%';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharSymbol() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 37", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   * public void charBackslashT(){ char val = '\t'; }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharBackslashT() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 9", "return").collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charBackslash(){
   *         char val = '\\';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharBackslash() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 92", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charSingleQuote(){
   *         char val = '\'';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharSingleQuote() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 39", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void charUnicode(){
   *         char val = '\u03a9';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharUnicode() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 937", "return")
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *     public void specialChar(){
   *         char val = '™';
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmtsSpecialChar() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 8482", "return")
        .collect(Collectors.toList());
  }
}
