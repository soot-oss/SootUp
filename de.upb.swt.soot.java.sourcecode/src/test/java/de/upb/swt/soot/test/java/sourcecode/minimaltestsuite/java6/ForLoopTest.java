package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.java.sourcecode.WalaClassLoaderTestUtils;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.LoadClassesWithWala;
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
public class ForLoopTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "ForLoop";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void forLoopTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "forLoop", loadClassesWithWala.declareClassSig, "void", Collections.emptyList()));
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
                "r0 := @this: ForLoop",
                "$i0 = 10", // j
                "$i1 = 0", // num
                "$i2 = 0", // i
                "$z0 = $i2 < $i0",
                "if $z0 == 0 goto return",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "$i5 = $i2",
                "$i6 = $i2 + 1",
                "$i2 = $i6",
                "goto [?= $z0 = $i2 < $i0]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
