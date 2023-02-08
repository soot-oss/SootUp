/** @author: Hasitha Rajapakse */
package sootup.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

@Category(Java8Test.class)
public class ForEachLoopTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "forEachLoop", "void", Collections.emptyList());
  }

  /**  <pre>
   * public void forEachLoop(){
   * int[] numArray = {10,20,30,40,50,60,71,80,90};
   * int count = 0;
   * for (int item :numArray) {
   * count++;
   * }
   * }
   *
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: ForEachLoop",
            "$r1 = newarray (int)[9]",
            "$r1[0] = 10",
            "$r1[1] = 20",
            "$r1[2] = 30",
            "$r1[3] = 40",
            "$r1[4] = 50",
            "$r1[5] = 60",
            "$r1[6] = 71",
            "$r1[7] = 80",
            "$r1[8] = 90",
            "$i0 = 0",
            "$r2 = $r1",
            "$i1 = 0",
            "label1:",
            "$i2 = lengthof $r2",
            "$z0 = $i1 < $i2",
            "if $z0 == 0 goto label2",
            "$r3 = $r2[$i1]",
            "$i3 = $i0",
            "$i4 = $i0 + 1",
            "$i0 = $i4",
            "$i5 = $i1",
            "$i6 = $i1 + 1",
            "$i1 = $i6",
            "goto label1",
            "label2:",
            "return")
        .collect(Collectors.toList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
