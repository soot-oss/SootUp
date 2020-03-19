package de.upb.swt.soot.javatestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class VolatileVariableTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "increaseCounter", getDeclaredClassSignature(), "int", Collections.emptyList());
  }

  @Test
  public void test() {
    super.test();
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField -> {
                  return sootField.getName().equals("counter")
                      && sootField.getModifiers().contains(Modifier.VOLATILE);
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: VolatileVariable",
            "$i0 = r0.<VolatileVariable: int counter>",
            "$i1 = $i0 + 1",
            "r0.<VolatileVariable: int counter> = $i1",
            "return $i0")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
