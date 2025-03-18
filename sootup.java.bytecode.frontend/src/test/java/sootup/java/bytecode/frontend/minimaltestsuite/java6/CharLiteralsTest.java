package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class CharLiteralsTest extends MinimalBytecodeTestSuiteBase {

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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 97", "return");
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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 37", "return");
  }

  /**
   *
   *
   * <pre>
   *     public void charBackslashT(){ char val = '\t'; }
   * </pre>
   */
  public List<String> expectedBodyStmtsCharBackslashT() {
    return Arrays.asList("this := @this: CharLiterals", "l1 = 9", "return");
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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 92", "return");
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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 39", "return");
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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 937", "return");
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
    return Arrays.asList("this := @this: CharLiterals", "l1 = 8482", "return");
  }
}
