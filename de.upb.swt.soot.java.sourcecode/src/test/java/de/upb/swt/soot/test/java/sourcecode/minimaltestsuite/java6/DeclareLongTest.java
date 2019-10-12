package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeclareLongTest extends MinimalTestSuiteBase {
    @Override
    public MethodSignature getMethodSignature() {
        return null;
    }

    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "r0 := @this: DeclareLong",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "$i0 = r0.<DeclareLong: long i>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(long)>($i0)",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "$i1 = r0.<DeclareLong: long j>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(long)>($i1)",
                "return")
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
