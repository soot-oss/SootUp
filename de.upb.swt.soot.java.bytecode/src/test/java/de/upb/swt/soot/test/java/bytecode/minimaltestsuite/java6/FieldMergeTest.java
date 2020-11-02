package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category(Java8Test.class)
public class FieldMergeTest extends MinimalBytecodeTestSuiteBase {
    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                "<init>", getDeclaredClassSignature(), "void", Collections.emptyList());
    }


    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "")
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Test
    public void test() {
        SootMethod method = loadMethod(getMethodSignature());
        assertJimpleStmts(method, expectedBodyStmts());
    }
}
