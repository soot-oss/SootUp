package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class VolatileVariableTest extends MinimalBytecodeTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "increaseCounter", getDeclaredClassSignature(), "int", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return sootField.getName().equals("counter")
                      && sootField.getModifiers().contains(Modifier.VOLATILE);
                }));
  }

  /**
   *
   *
   * <pre>
   * public int increaseCounter(){
   * return counter++;
   * }
   *
   * </pre>
   */
  @Override
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
