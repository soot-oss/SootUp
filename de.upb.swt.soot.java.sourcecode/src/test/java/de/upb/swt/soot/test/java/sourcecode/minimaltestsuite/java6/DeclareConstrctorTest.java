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

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "DeclareConstructor", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignatureTwoParam() {
    return identifierFactory.getMethodSignature(
        "DeclareConstructor", getDeclaredClassSignature(), "void", Arrays.asList("int", "int"));
  }

  @Test
  @Override
  public void defaultTest() {
    loadMethod(expectedBodyStmts(), getMethodSignature());
    loadMethod(expectedBodyStmts1(), getMethodSignatureTwoParam());
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    assertTrue(sootClass.getMethod(getMethodSignature()).isPresent());
    assertEquals(3, sootClass.getMethods().size());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            "$i1 := @parameter1: int",
            "$i2 = $i0 + $i1",
            "return $i2")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: DeclareConstructor",
            "$i0 := @parameter0: int",
            "$i1 = $i0 + $i0",
            "return $i1")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
