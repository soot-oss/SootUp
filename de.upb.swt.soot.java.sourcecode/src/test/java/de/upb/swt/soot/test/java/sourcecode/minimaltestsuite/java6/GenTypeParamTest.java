package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenTypeParamTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "geneTypeParamDisplay", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: GenTypeParam",
            "$r1 = new java.util.ArrayList",
            "specialinvoke $r1.<java.util.ArrayList: void <init>(int)>(3)",
            "$r2 = newarray (java.lang.Object[])[3]",
            "$r2[0] = 1",
            "$r2[1] = 2",
            "$r2[2] = 3",
            "$r3 = staticinvoke <java.util.Arrays: java.util.List asList(java.lang.Object[])>($r2)",
            "r0 = new GenTypeParam",
            "specialinvoke $u0.<GenTypeParam: void <init>()>()",
            "virtualinvoke $u0.<GenTypeParam: void copy(java.util.List,java.util.List)>($r1, $r3)",
            "$r4 = <java.lang.System: java.io.PrintStream out>",
            "$r5 = virtualinvoke $u0.<GenTypeParam: java.lang.Number largestNum(java.lang.Number,java.lang.Number,java.lang.Number)>(2, 8, 3)",
            "$r6 = (java.lang.Integer) $r5",
            "virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.Object)>($r6)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
