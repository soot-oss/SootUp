package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DeclareConstrctorTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignatureInitOneParam() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
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
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getMethod(getMethodSignature()).isPresent());
    assertEquals(3, sootClass.getMethods().size());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            // "$i1 := @parameter1: int",
            "return ")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            "$i1 := @parameter1: int",
            "return ")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
