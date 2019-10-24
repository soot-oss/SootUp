package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author: Hasitha Rajapakse **/

@Category(Java8Test.class)
public class StaticMethodInvocationTest extends MinimalTestSuiteBase {
    @Override
    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                "staticMethodInvocation", getDeclaredClassSignature(), "void", Collections.emptyList());
    }

    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "r0 := @this: StaticMethodInvocation",
                "staticinvoke <StaticMethodInvocation: void staticmethod()>()",
                "return")
                .collect(Collectors.toList());
    }
}
