package sootup.jimple.parser.javatestsuite.java9;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnonymousDiamondOperatorTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "innerClassDiamond", "int", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: AnonymousDiamondOperator",
            "$stack3 = new AnonymousDiamondOperator$1",
            "specialinvoke $stack3.<AnonymousDiamondOperator$1: void <init>(AnonymousDiamondOperator)>(l0)",
            "l1 = $stack3",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(22)",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(23)",
            "$stack6 = virtualinvoke l1.<MyClass: java.lang.Object add(java.lang.Object,java.lang.Object)>($stack4, $stack5)",
            "l2 = (java.lang.Integer) $stack6",
            "$stack7 = virtualinvoke l2.<java.lang.Integer: int intValue()>()",
            "return $stack7")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
