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
public class BreakInWhileLoopTest {

  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
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
                "int",
                Collections.singletonList("int")));
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
                "$i0 := @parameter0: int",
                "$i1 = 1",
                "if $i1 == 0 goto return $i0",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "$r2 = \"Current value in While Loop is \" + $i0",
                "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>($r2)",
                "$i2 = $i0",
                "$i3 = $i0 - 1",
                "$i0 = $i3",
                "$z0 = $i0 == 0",
                "if $z0 == 0 goto (branch)",
                "goto [?= return $i0]",
                "goto [?= (branch)]",
                "return $i0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
