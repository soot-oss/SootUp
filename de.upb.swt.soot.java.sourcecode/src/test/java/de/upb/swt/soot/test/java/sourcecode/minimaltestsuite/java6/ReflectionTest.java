package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "checkReflection", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: Reflection",
            "r0 = new Reflection",
            "specialinvoke $u0.<Reflection: void <init>()>()",
            "$r1 = class \"Ljava/lang/Class\"",
            "$r2 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.Object)>($r1)",
            "$r3 = newarray (java.lang.Class[])[0]",
            "$r4 = virtualinvoke $r1.<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>($r3)",
            "$r5 = <java.lang.System: java.io.PrintStream out>",
            "$r6 = virtualinvoke $r4.<java.lang.reflect.Constructor: java.lang.String getName()>()",
            "virtualinvoke $r5.<java.io.PrintStream: void println(java.lang.String)>($r6)",
            "$r7 = <java.lang.System: java.io.PrintStream out>",
            "$r8 = virtualinvoke $r1.<java.lang.Class: java.lang.reflect.Method[] getMethods()>()",
            "$i0 = lengthof $r8",
            "virtualinvoke $r7.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
