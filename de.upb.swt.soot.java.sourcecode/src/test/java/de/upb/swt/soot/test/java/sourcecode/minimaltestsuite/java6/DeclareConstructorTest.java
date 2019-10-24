package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;


import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DeclareConstructorTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignatureInitOneParam() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.singletonList("int"));
  }

  public MethodSignature getMethodSignatureInitTwoParam() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Arrays.asList("int", "int"));
  }

  @Test
  @Override
  public void defaultTest() {
    loadMethod(expectedBodyStmts(), getMethodSignatureInitOneParam());
    loadMethod(expectedBodyStmts1(), getMethodSignatureInitTwoParam());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            "specialinvoke r0.<java.lang.Object: void <init>()>()",
            "r0.<DeclareConstructor: int var1> = $i0",
            "r0.<DeclareConstructor: int var2> = 0",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            "$i1 := @parameter1: int",
            "specialinvoke r0.<java.lang.Object: void <init>()>()",
            "r0.<DeclareConstructor: int var1> = $i0",
            "r0.<DeclareConstructor: int var2> = $i1",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
