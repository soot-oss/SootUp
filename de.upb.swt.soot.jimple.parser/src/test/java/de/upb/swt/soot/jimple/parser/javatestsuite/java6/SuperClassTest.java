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
public class SuperClassTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "superclassMethod", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SuperClass",
            "l0.<SuperClass: int a> = 10",
            "l0.<SuperClass: int b> = 20",
            "l0.<SuperClass: int c> = 30",
            "l0.<SuperClass: int d> = 40",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
