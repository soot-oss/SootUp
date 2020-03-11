package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java9;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class TryWithResourcesConciseTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printFile", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    /** TODO [KK] Stack positions changing. Add to issue list. */
  }

  @Ignore
  public void ignoreTest() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TryWithResourcesConcise",
            "$stack5 = new java.io.BufferedReader",
            "$stack6 = new java.io.FileReader",
            "specialinvoke $stack6.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack5.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack6)",
            "l1 = $stack5",
            "l2 = l1",
            "label1:",
            "l3 = \"\"",
            "label2:",
            "$stack9 = l1",
            "$stack7 = virtualinvoke $stack9.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l3 = $stack7",
            "if $stack7 == null goto label3",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>(l3)",
            "goto label2",
            "label3:",
            "if l2 == null goto label9",
            "virtualinvoke l2.<java.io.BufferedReader: void close()>()",
            "goto label9",
            "label4:",
            "$stack11 := @caughtexception",
            "l3 = $stack11",
            "if l2 == null goto label8",
            "label5:",
            "virtualinvoke l2.<java.io.BufferedReader: void close()>()",
            "label6:",
            "goto label8",
            "label7:",
            "$stack10 := @caughtexception",
            "l4 = $stack10",
            "virtualinvoke l3.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l4)",
            "label8:",
            "$stack12 = l3",
            "$stack12 = $stack12",
            "throw $stack12",
            "label9:",
            "return",
            "catch java.lang.Throwable from label1 to label3 with label4",
            "catch java.lang.Throwable from label5 to label6 with label7")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
