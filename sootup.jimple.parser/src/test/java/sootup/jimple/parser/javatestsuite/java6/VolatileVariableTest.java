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
public class VolatileVariableTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "increaseCounter", "int", Collections.emptyList());
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
                    sootField.getName().equals("counter")
                        && sootField.getModifiers().contains(FieldModifier.VOLATILE)));
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: VolatileVariable",
            "$stack1 = l0.<VolatileVariable: int counter>",
            "$stack2 = $stack1 + 1",
            "l0.<VolatileVariable: int counter> = $stack2",
            "return $stack1")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
