package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
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
public class ForEachLoopTest {
  // TODO extends MinimalTestSuiteBase
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "ForEachLoop";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void forEachLoopTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "forEachLoop",
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
                "r0 := @this: ForEachLoop",
                "$r1 = newarray (int[])[9]", // numArray
                "$r1[0] = 10",
                "$r1[1] = 20",
                "$r1[2] = 30",
                "$r1[3] = 40",
                "$r1[4] = 50",
                "$r1[5] = 60",
                "$r1[6] = 71",
                "$r1[7] = 80",
                "$r1[8] = 90",
                "$i0 = 0", // count
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i3 = $i0",
                "$i4 = $i0 + 1",
                "$i0 = $i4",
                "$i5 = $i1",
                "$i6 = $i1 + 1",
                "$i1 = $i6",
                "goto [?= $i2 = lengthof $r2]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
