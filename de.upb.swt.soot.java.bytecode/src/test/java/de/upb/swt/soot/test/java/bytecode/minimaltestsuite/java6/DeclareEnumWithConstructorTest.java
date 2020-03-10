package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareEnumWithConstructorTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getInitMethodSignature() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    SootMethod sootMethod = loadMethod(getInitMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
    /** TODO sootClass.isEnum() return false as it checks for if the DeclareEnumConstructor class */
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: DeclareEnumWithConstructor",
            "specialinvoke l0.<java.lang.Object: void <init>()>()",
            "return")
        .collect(Collectors.toList());
  }
}
