package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java8;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class MethodAcceptingLamExprTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "lambdaAsParamMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Ignore
  @Override
  public void defaultTest() {}

  @Ignore
  public void ignoreTest() {
    super.defaultTest();
  }
  /** TODO update the expectedBodyStmts when Lambda are supported by Wala */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: MethodAcceptingLamExpr", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
