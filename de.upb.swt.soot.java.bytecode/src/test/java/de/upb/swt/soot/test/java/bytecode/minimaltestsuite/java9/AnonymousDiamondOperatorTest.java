package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java9;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnonymousDiamondOperatorTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "innerClassDiamond", getDeclaredClassSignature(), "int", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: AnonymousDiamondOperator",
            "$stack3 = new AnonymousDiamondOperator$1",
            "specialinvoke $stack3.<AnonymousDiamondOperator$1: void <init>(AnonymousDiamondOperator)>(l0)",
            "l1 = $stack3",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(22)",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(23)",
            "$stack6 = virtualinvoke l1.<MyClass: java.lang.Object add(java.lang.Object,java.lang.Object)>($stack4, $stack5)",
            "l2 = (java/lang/Integer) $stack6",
            "$stack7 = virtualinvoke l2.<java.lang.Integer: int intValue()>()",
            "return $stack7")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
