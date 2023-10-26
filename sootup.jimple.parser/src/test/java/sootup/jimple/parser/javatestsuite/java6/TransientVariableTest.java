package sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class TransientVariableTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "transientVariable", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getName().equals("transientVar")
                        && sootField.getModifiers().contains(FieldModifier.TRANSIENT)));
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TransientVariable",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "$stack1 = l0.<TransientVariable: int transientVar>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(int)>($stack1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
