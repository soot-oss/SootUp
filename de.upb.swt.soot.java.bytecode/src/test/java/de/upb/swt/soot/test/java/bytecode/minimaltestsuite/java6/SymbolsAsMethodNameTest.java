package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SymbolsAsMethodNameTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "αρετη", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SymbolsAsMethodName",
            "$stack1 = <java.lang.System: java.io.PrintStream; out>",
            "virtualinvoke $stack1.<java.io.PrintStream: void println(java.lang.String)>(\"this is \\u03b1\\u03c1\\u03b5\\u03c4\\u03b7 method\")",
            "return")
        .collect(Collectors.toList());
  }
}
