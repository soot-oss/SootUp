package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class AnnotationLibraryImplTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "displayAnnotationLibraryImpl",
        getDeclaredClassSignature(),
        "void",
        Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    assertTrue(
        getInterfaces().stream()
            .anyMatch(
                javaClassType -> {
                  return javaClassType.getClassName().equalsIgnoreCase("AnnotationLibrary");
                }));
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: AnnotationLibraryImpl",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Annotated interface implemented\")",
            "return")
        .collect(Collectors.toList());
  }
}
