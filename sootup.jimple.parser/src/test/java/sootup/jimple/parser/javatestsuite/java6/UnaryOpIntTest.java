package sootup.jimple.parser.javatestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class UnaryOpIntTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "methodUnaryOpInt", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    /**
     * TODO Do we need to check the type of variable as int?
     * assertTrue(getFields().stream().anyMatch(sootField -> {return
     * sootField.getType().equals("int");}));
     */
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: UnaryOpInt",
            "$stack3 = l0.<UnaryOpInt: int i>",
            "$stack2 = l0.<UnaryOpInt: int j>",
            "l1 = $stack3 + $stack2",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
