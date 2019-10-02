package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
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
  public void testDemo() {
    defaultTest();
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "r0 := @this: VolatileVariable",
            "$i0 = r0.<VolatileVariable: int counter>",
            "$i1 = $i0 + 1",
            "r0.<VolatileVariable: int counter> = $i1",
            "return $i0")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
