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
public class SubClassTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "SubClass";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void subclassMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "subclassMethod",
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
                "r0 := @this: SubClass",
                "r0.<SubClass: int aa> = 10",
                "r0.<SubClass: int bb> = 20",
                "r0.<SubClass: int cc> = 30",
                "r0.<SubClass: int dd> = 40",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void superclassMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "superclassMethod",
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
                "r0 := @this: SubClass",
                "specialinvoke r0.<SuperClass: void superclassMethod()>()",
                "r0.<SuperClass: int a> = 100",
                "r0.<SuperClass: int b> = 200",
                "r0.<SuperClass: int c> = 300",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
