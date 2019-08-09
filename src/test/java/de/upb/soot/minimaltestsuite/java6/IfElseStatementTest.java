package de.upb.soot.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.types.JavaClassType;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@Category(Java8Test.class)
public class IfElseStatementTest {

    private WalaClassLoader loader;
    private DefaultIdentifierFactory identifierFactory;
    private JavaClassType declareClassSig;

    @Before
    public void loadClassesWithWala() {
        String srcDir = "src/test/resources/minimaltestsuite/java6";
        loader = new WalaClassLoader(srcDir, null);
        identifierFactory = DefaultIdentifierFactory.getInstance();
        declareClassSig = identifierFactory.getClassType("IfElseStatement");
    }

    @Test
    public void test() {
        Optional<SootMethod> m =
                WalaClassLoaderTestUtils.getSootMethod(
                        loader,
                        identifierFactory.getMethodSignature(
                                "ifElseStatement",
                                declareClassSig,
                                "java.lang.String",
                                Arrays.asList("int", "int", "int")));
        assertTrue(m.isPresent());
        SootMethod method = m.get();
        Utils.print(method, false);
        Body body = method.getBody();
        assertNotNull(body);

        List<String> actualStmts =
                body.getStmts().stream()
                        .map(Stmt::toString)
                        .collect(Collectors.toCollection(ArrayList::new));

        List<String> expectedStmts =
                Stream.of(
                        "r0 := @this: IfElseStatement",
                        "$i0 := @parameter0: int",
                        "$i1 := @parameter1: int",
                        "$i2 := @parameter2: int",
                        "$r1 = null",
                        "$z0 = $i0 < $i1",
                        "if $z0 == 0 goto $z1 = $i1 < $i2",
                        "$r1 = \"if statement\"",
                        "goto [?= return $r1]",
                        "$z1 = $i1 < $i2",
                        "if $z1 == 0 goto $r1 = \"else statement\"",
                        "$r1 = \"else if statement\"",
                        "goto [?= return $r1]",
                        "$r1 = \"else statement\"",
                        "return $r1")
                        .collect(Collectors.toCollection(ArrayList::new));

        assertEquals(expectedStmts, actualStmts);
    }
}