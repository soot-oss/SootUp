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
public class LabelledLoopBreakTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "LabelledLoopBreak";
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
                "labelledLoopBreak",
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
                "r0 := @this: LabelledLoopBreak",
                "$i0 = 0",
                "$z0 = $i0 < 5",
                "if $z0 == 0 goto return",
                "$i1 = 0",
                "$z1 = $i1 < 5",
                "if $z1 == 0 goto $i4 = $i0",
                "$z2 = $i0 == 1",
                "if $z2 == 0 goto $i2 = $i1",
                "goto [?= return]",
                "$i2 = $i1",
                "$i3 = $i1 + 1",
                "$i1 = $i3",
                "goto [?= $z1 = $i1 < 5]",
                "$i4 = $i0",
                "$i5 = $i0 + 1",
                "$i0 = $i5",
                "goto [?= $z0 = $i0 < 5]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
