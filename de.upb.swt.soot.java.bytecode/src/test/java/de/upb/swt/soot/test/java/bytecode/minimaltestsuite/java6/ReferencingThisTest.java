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
public class ReferencingThisTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "thisMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: ReferencingThis",
            "$stack2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack2.<java.io.PrintStream: void println(java.lang.String)>(\" this keyword as an argument in the constructor call\")",
            "$stack3 = new ReferencingThis",
            "$stack5 = l0.<ReferencingThis: I a>",
            "$stack4 = l0.<ReferencingThis: I b>",
            "specialinvoke $stack3.<ReferencingThis: void <init>(int,int)>($stack5, $stack4)",
            "l1 = $stack3",
            "virtualinvoke l1.<ReferencingThis: void show()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
