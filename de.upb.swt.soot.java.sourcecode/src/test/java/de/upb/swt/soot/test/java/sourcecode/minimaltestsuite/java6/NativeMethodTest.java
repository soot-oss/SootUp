package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

public class NativeMethodTest extends MinimalTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "nullVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  public void defaultTest() {}

  @Ignore
  public void ignoreTest() {
    /** Can not check Native code feature */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: NullVariable", "$r1 = null", "return")
        .collect(Collectors.toList());
  }
}
