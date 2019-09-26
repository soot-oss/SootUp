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
public class DeclareIntTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "DeclareInt";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void declareIntMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "declareIntMethod",
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
                "r0 := @this: DeclareInt",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "$i0 = r0.<DeclareInt: int dec>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "$i1 = r0.<DeclareInt: int hex>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i1)",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "$i2 = r0.<DeclareInt: int oct>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i2)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
