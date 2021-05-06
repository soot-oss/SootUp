package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java14;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Bastian Haverkamp */
public class SwitchExprWithoutBreakTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "switchSomething", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *   void switchSomething() {
   *     int k = 5;
   *     String s = "";
   *
   *     // new arrow syntax, will not fall through
   *     s += switch (k) {
   *       case 1 -> "single";
   *       case 2, 3 -> "double";
   *       default -> "somethingElse";
   *     };
   *
   *     // new arrow syntax + code block with new yield statement
   *     s += switch (k) {
   *       case 1  -> {
   *         int temp = k + 5;
   *         yield temp;
   *       }
   *       case 2, 3 -> "double";
   *       default -> "somethingElse";
   *     };
   *
   *     // old syntax with new yield statement
   *     s += switch(k) {
   *       case 1:
   *         yield "no fall through";
   *       case 2,3:
   *         yield "still no fall through";
   *       default: {
   *         yield "we will not fall through";
   *       }
   *     };
   *
   *     System.out.println(s);
   *   }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("return").collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
