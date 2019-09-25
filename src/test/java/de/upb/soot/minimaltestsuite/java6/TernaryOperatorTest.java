package de.upb.soot.minimaltestsuite.java6;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.minimaltestsuite.LoadClassesWithWala;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class TernaryOperatorTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "TernaryOperator";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void staticMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "ternaryOperatorMethod",
                loadClassesWithWala.declareClassSig,
                "boolean",
                Collections.emptyList()));
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
                "r0 := @this: TernaryOperator",
                "$i0 = 2",
                "$i1 = 0 ",
                "$z0 = $i0 < $i1",
                "$z1 = $i0 > $i1",
                "if $z0 == 0 $b0 = false",
                "if $z1 == 0 $b0 = true",
                "return $b0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
