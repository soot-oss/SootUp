package de.upb.soot.javasourcecodefrontend.minimaltestsuite.java6;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.javasourcecodefrontend.frontend.Utils;
import de.upb.soot.javasourcecodefrontend.frontend.WalaClassLoaderTestUtils;
import de.upb.soot.javasourcecodefrontend.minimaltestsuite.LoadClassesWithWala;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
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
public class IfElseStatementTest {

  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "IfElseStatement";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void ifElseStatementTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "ifElseStatement",
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
                "r0 := @this: IfElseStatement",
                "$i0 = 10",
                "$i1 = 20",
                "$i2 = 30",
                "$i3 = 0",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto $z1 = $i1 < $i2",
                "$i3 = 1",
                "goto [?= return]",
                "$z1 = $i1 < $i2",
                "if $z1 == 0 goto $i3 = 3",
                "$i3 = 2",
                "goto [?= return]",
                "$i3 = 3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
