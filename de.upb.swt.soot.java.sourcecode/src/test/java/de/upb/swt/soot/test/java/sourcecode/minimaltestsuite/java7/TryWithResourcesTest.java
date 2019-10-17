package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java7;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class TryWithResourcesTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "printFile", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TryWithResources",
            "$r1 = new java.io.BufferedReader",
            "$r2 = new java.io.FileReader",
            "specialinvoke $r2.<java.io.FileReader: void <init>(java.lang.String)>(\"file.txt\")",
            "specialinvoke $r1.<java.io.BufferedReader: void <init>(java.io.Reader)>($r2)",
            "$r3 = \"\"",
            "$r4 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()",
            "goto [?= $r3 = $r4]",
            "$r5 := @caughtexception",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "throw $r5",
            "$r3 = $r4",
            "$z0 = $r4 != null",
            "if $z0 == 0 goto virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r3)",
            "goto [?= $r4 = virtualinvoke $r1.<java.io.BufferedReader: java.lang.String readLine()>()]",
            "virtualinvoke $r1.<java.io.BufferedReader: void close()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
