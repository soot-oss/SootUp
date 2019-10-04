/// ** @author: Hasitha Rajapakse */
// package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertTrue;
//
// import categories.Java8Test;
// import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
// import de.upb.swt.soot.core.model.Body;
// import de.upb.swt.soot.core.model.SootMethod;
// import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
// import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
// import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.LoadClassesWithWala;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.experimental.categories.Category;
//
// @Category(Java8Test.class)
// public class EvalOrderWithParenthesesTest {
//  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
//  private String className = "EvaluationOrderWithParentheses";
//  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();
//
//  @Before
//  public void loadClasses() {
//    loadClassesWithWala.classLoader(srcDir, className);
//  }
//
//  @Test
//  public void evaluationOrderWithParenthesesTest() {
//    Optional<SootMethod> m =
//        WalaClassLoaderTestUtils.getSootMethod(
//            loadClassesWithWala.loader,
//            loadClassesWithWala.identifierFactory.getMethodSignature(
//                "evaluationOrderWithParentheses",
//                loadClassesWithWala.declareClassSig,
//                "void",
//                Collections.emptyList()));
//    assertTrue(m.isPresent());
//    SootMethod method = m.get();
//    Utils.print(method, false);
//    Body body = method.getBody();
//    assertNotNull(body);
//
//    List<String> actualStmts =
//        body.getStmts().stream()
//            .map(Stmt::toString)
//            .collect(Collectors.toCollection(ArrayList::new));
//
//    List<String> expectedStmts =
//        Stream.of(
//                "r0 := @this: EvaluationOrderWithParentheses",
//                "$i0 = 1 + 2",
//                "$i1 = $i0 * 3",
//                "return")
//            .collect(Collectors.toCollection(ArrayList::new));
//
//    assertEquals(expectedStmts, actualStmts);
//  }
// }

/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class EvaluationOrderWithParenthesesTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "evaluationOrderWithParentheses",
        getDeclaredClassSignature(),
        "void",
        Collections.emptyList());
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "r0 := @this: EvaluationOrderWithParentheses", "$i0 = 1 + 2", "$i1 = $i0 * 3", "return")
        .collect(Collectors.toList());
  }
}
