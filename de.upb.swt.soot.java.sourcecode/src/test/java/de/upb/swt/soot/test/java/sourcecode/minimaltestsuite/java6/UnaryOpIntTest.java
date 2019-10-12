package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnaryOpIntTest extends MinimalTestSuiteBase {
    @Override
    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                "methodUnaryOpInt", getDeclaredClassSignature(), "void", Collections.emptyList());
    }

    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "r0 := @this: UnaryOpInt",
                "$i0 := r0.<DeclareInt: int i",
                "$i1 := r0.<DeclareInt: int j",
                "$i2 = $i0 + $i1",
                "$i0 = $i2",
                "return ")
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
