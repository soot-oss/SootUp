package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java7;

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
public class UnderscoreInIntTest extends MinimalTestSuiteBase {
    @Override
    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                "underscoreInInt", getDeclaredClassSignature(), "void", Collections.emptyList());
    }

    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "r0 := @this: UnderscoreInInt",
                "$i0 = 2147483647",
                "return")
                .collect(Collectors.toList());
    }
}
