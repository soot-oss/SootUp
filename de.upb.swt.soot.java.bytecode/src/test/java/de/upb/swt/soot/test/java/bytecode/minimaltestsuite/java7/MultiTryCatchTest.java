package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java7;

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
public class MultiTryCatchTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void test() {
    /** TODO [KK] Stack positions changing. Add to issue list. */
  }

  @Ignore
  public void ignoreTest() {
    SootMethod sootMethod = loadMethod(getMethodSignature());
    assertJimpleStmts(sootMethod, expectedBodyStmts());
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printFile", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: MultiTryCatch",
            "$stack6 = new java.io.BufferedReader",
            "$stack7 = new java.io.FileReader",
            "specialinvoke $stack7.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack7)",
            "l1 = $stack6",
            "label01:",
            "l2 = \"\"",
            "l3 = 2",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(int)>(l3)",
            "label02:",
            "$stack11 = l1",
            "$stack9 = virtualinvoke $stack11.<java.io.BufferedReader: java.lang.String readLine()>()",
            "l2 = $stack9",
            "if $stack9 == null goto label03",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(java.lang.String)>(l2)",
            "goto label02",
            "label03:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label04:",
            "goto label19",
            "label05:",
            "$stack19 := @caughtexception",
            "l2 = $stack19",
            "goto label19",
            "label06:",
            "$stack12 := @caughtexception",
            "l2 = $stack12",
            "label07:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label08:",
            "goto label19",
            "label09:",
            "$stack15 := @caughtexception",
            "l2 = $stack15",
            "goto label19",
            "label10:",
            "$stack16 := @caughtexception",
            "l2 = $stack16",
            "label11:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label12:",
            "goto label19",
            "label13:",
            "$stack13 := @caughtexception",
            "l2 = $stack13",
            "goto label19",
            "label14:",
            "$stack14 := @caughtexception",
            "l4 = $stack14",
            "label15:",
            "virtualinvoke l1.<java.io.BufferedReader: void close()>()",
            "label16:",
            "goto label18",
            "label17:",
            "$stack17 := @caughtexception",
            "l5 = $stack17",
            "label18:",
            "$stack18 = l4",
            "throw $stack18",
            "label19:",
            "return",
            "catch java.io.IOException from label03 to label04 with label05",
            "catch java.io.IOException from label01 to label03 with label06",
            "catch java.lang.NumberFormatException from label01 to label03 with label06",
            "catch java.io.IOException from label07 to label08 with label09",
            "catch java.lang.Exception from label01 to label03 with label10",
            "catch java.io.IOException from label11 to label12 with label13",
            "catch java.lang.Throwable from label01 to label03 with label14",
            "catch java.io.IOException from label15 to label16 with label17",
            "catch java.lang.Throwable from label14 to label15 with label14")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
