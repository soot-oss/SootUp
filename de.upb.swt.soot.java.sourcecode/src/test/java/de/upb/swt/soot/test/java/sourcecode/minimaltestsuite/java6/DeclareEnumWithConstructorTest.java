package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareEnumWithConstructorTest extends MinimalTestSuiteBase {

  public MethodSignature getInitMethodSignature() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "getValue", getDeclaredClassSignature(), "int", Collections.emptyList());
  }
  public MethodSignature getMainMethodSignature() {
    return identifierFactory.getMethodSignature(
            "main", getDeclaredClassSignature(), "void", Collections.singletonList("String[]"));
  }

  public MethodSignature getMainMethodSignature1() {
    return identifierFactory.getMethodSignature(
            "main", identifierFactory.getClassType("DeclareEnumWithConstructor$Number"), "void", Collections.singletonList("String[]"));
  }

  @Override
  public void defaultTest() {
    SootMethod sootMethod = loadMethod(getInitMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());

    sootMethod = loadMethod(getMainMethodSignature());
    assertJimpleStmts(sootMethod, expectedMainBodyStmts());

    /**
     * TODO sootClass.isEnum() return false as it checks for if the DeclareEnumConstructor class.
     */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareEnumWithConstructor",
            "specialinvoke r0.<java.lang.Object: void <init>()>()",
            "return")
        .collect(Collectors.toList());
  }

  public List<String> expectedMainBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareEnumWithConstructor",
            "specialinvoke r0.<java.lang.Object: void <init>()>()",
            "return")
            .collect(Collectors.toList());
  }
}
