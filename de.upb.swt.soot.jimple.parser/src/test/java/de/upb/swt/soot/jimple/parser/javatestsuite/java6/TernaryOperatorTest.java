package de.upb.swt.soot.jimple.parser.javatestsuite.java6;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.jimple.parser.categories.Java8Test;
import de.upb.swt.soot.jimple.parser.javatestsuite.JimpleTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class TernaryOperatorTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "ternaryOperatorMethod", "boolean", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TernaryOperator",
            "$stack1 = l0.<TernaryOperator: int num>",
            "if $stack1 >= 0 goto label1",
            "$stack2 = 0",
            "goto label2",
            "label1:",
            "$stack2 = 1",
            "label2:",
            "return $stack2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
