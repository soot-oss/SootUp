package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmtsCharCharacter() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 97", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharSymbol() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 37", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharBackslashT() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 9", "return").collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharBackslash() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 92", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharSingleQuote() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 39", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsCharUnicode() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 937", "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedBodyStmtsSpecialChar() {
    return Stream.of("r0 := @this: CharLiterals", "$i0 = 8482", "return")
        .collect(Collectors.toList());
  }
}
