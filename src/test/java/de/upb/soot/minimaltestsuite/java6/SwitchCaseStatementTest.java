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
public class SwitchCaseStatementTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "SwitchCaseStatement";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void switchCaseStatementTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "switchCaseStatement",
                loadClassesWithWala.declareClassSig,
                "void",
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
                "r0 := @this: SwitchCaseStatement",
                "$r1 = \"red\"",
                "$r2 = \"\"",
                "if $r1 == $i0 goto $r2 = \"color red detected\"",
                "if $r1 == $i1 goto $r2 = \"color green detected\"",
                "goto [?= $r2 = \"invalid color\"]",
                "$r2 = \"color red detected\"",
                "goto [?= return]",
                "$r2 = \"color green detected\"",
                "goto [?= return]",
                "$r2 = \"invalid color\"",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
