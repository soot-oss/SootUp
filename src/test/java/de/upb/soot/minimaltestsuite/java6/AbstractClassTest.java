package de.upb.soot.minimaltestsuite.java6;

import de.upb.soot.minimaltestsuite.MinimalTestSuiteBase;
import de.upb.soot.signatures.MethodSignature;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractClassTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "abstractClass", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "r0 := @this: AbstractClass",
            "r0 = new AbstractClass",
            "specialinvoke $u0.<AbstractClass: void <init>()>()",
            "virtualinvoke $u0.<A: void a()>()",
            "return")
        .collect(Collectors.toList());
  }
}
