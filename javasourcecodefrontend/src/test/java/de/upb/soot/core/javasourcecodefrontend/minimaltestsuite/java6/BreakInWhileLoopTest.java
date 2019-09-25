package de.upb.soot.javasourcecodefrontend.minimaltestsuite.java6;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.javasourcecodefrontend.frontend.Utils;
import de.upb.soot.javasourcecodefrontend.frontend.WalaClassLoaderTestUtils;
import de.upb.soot.javasourcecodefrontend.minimaltestsuite.LoadClassesWithWala;
import de.upb.soot.jimple.common.stmt.Stmt;
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
public class BreakInWhileLoopTest {

  private String srcDir =
      "src/test/resources/java.de.upb.soot.javasourcecodefrontend.minimaltestsuite/java6/";
  private String className = "BreakInWhileLoop";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void breakInWhileLoopTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "breakInWhileLoop",
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
                "r0 := @this: BreakInWhileLoop",
                "$i0 = 10",
                "$i1 = 5",
                "$z0 = $i0 > 0",
                "if $z0 == 0 goto return",
                "$i2 = $i0",
                "$i3 = $i0 - 1",
                "$i0 = $i3",
                "$z1 = $i0 == $i1",
                "if $z1 == 0 goto (branch)",
                "goto [?= return]",
                "goto [?= $z0 = $i0 > 0]",
                "return")
            .collect(Collectors.toList());

    assertEquals(expectedStmts, actualStmts);
  }
}
