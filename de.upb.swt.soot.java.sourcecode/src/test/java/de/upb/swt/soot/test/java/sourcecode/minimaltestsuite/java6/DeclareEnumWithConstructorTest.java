package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
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

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    super.defaultTest();
    SootClass clazz = loadClass(getDeclaredClassSignature());
    /**
     * TODO check whether object holds ENUM value assertTrue(clazz.getFields().stream().anyMatch());
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
}
