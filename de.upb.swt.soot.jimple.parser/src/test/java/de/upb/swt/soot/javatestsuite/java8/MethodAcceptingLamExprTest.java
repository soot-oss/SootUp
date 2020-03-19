package de.upb.swt.soot.javatestsuite.java8;

import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class MethodAcceptingLamExprTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "lambdaAsParamMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @org.junit.Test
  @Ignore
  public void defaultTest() {
    super.defaultTest();
  }
  /** TODO update the expectedBodyStmts when Lambda are supported by Wala */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: MethodAcceptingLamExpr", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
