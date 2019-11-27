package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class SymbolsAsClassNameTest extends MinimalTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "αρετηAsClassName", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public void defaultTest() {
    /**
     * Exception in thread "main" java.nio.file.InvalidPathException: Illegal char <?> at index 1:
     * a?et?.java
     */
  }

  @Ignore
  public void ignoreTest() {
    super.defaultTest();
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    System.out.println(sootClass.getClassSource().getClassType().getClassName());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: αρετη",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"this is αρετη class\")",
            "return")
        .collect(Collectors.toList());
  }
}
