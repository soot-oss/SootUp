package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java7;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiTryCatchTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printFile", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MultiTryCatch",
            "$r1 = new java.io.BufferedReader",
            "$r2 = new java.io.FileReader",
            "specialinvoke $r2.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $r1.<java.io.BufferedReader: void <init>(java.io.Reader)>($r2)",
            "$r3 = \"\"",
            "$i0 = 10 / 5",
            "$r4 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i0)",
            "$r5 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "$r3 = $r5",
            "$z0 = $r5 != null",
            "if $z0 == 0 goto (branch)",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto [?= $r5 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()]",
            "goto [?= virtualinvoke $r1.<java.io.BufferedReader: void close()>()]",
            "$r7 := @caughtexception",
            "$r8 = $r7",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "goto [?= return]",
            "$r9 := @caughtexception",
            "$r10 = $r9",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
