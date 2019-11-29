package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java7;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class TryWithResourcesTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printFile", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TryWithResources","$stack6 = new java/io/BufferedReader","$stack7 = new java/io/FileReader","specialinvoke $stack7.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")","specialinvoke $stack6.<java.io.BufferedReader: void <init>(java.io.Reader)>($stack7)","l1 = $stack6","l2 = null","label01:","l3 = \"\"","label02:","$stack10 = l1","$stack8 = virtualinvoke $stack10.<java.io.BufferedReader: java.lang.String readLine()>()","l3 = $stack8","if $stack8 == null goto label03","$stack9 = <java.lang.System: java.io.PrintStream; out>","virtualinvoke $stack9.<java.io.PrintStream: void println(java.lang.String)>(l3)","goto label02","label03:","if l1 == null goto label16","if l2 == null goto label07","label04:","virtualinvoke l1.<java.io.BufferedReader: void close()>()","label05:","goto label16","label06:","$stack12 := @caughtexception","l3 = $stack12","virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l3)","goto label16","label07:","virtualinvoke l1.<java.io.BufferedReader: void close()>()","goto label16","label08:","$stack11 := @caughtexception","l3 = $stack11","l2 = l3","throw l3","label09:","$stack14 := @caughtexception","l4 = $stack14","label10:","if l1 == null goto label15","if l2 == null goto label14","label11:","virtualinvoke l1.<java.io.BufferedReader: void close()>()","label12:","goto label15","label13:","$stack13 := @caughtexception","l5 = $stack13","virtualinvoke l2.<java.lang.Throwable: void addSuppressed(java.lang.Throwable)>(l5)","goto label15","label14:","virtualinvoke l1.<java.io.BufferedReader: void close()>()","label15:","$stack15 = l4","$stack15 = $stack15","$stack15 = $stack15","throw $stack15","label16:","return","catch java.lang.Throwable from label04 to label05 with label06","catch java.lang.Throwable from label01 to label03 with label08","catch java.lang.Throwable from label01 to label03 with label09","catch java.lang.Throwable from label11 to label12 with label13","catch java.lang.Throwable from label08 to label10 with label09")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
