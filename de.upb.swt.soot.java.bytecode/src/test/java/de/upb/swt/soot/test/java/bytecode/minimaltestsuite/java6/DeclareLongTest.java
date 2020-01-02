package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class DeclareLongTest extends MinimalBytecodeTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "declareLongMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: DeclareLong",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$l0 = r0.<DeclareLong: long l1>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(long)>($l0)",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "$l1 = r0.<DeclareLong: long l2>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(long)>($l1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
