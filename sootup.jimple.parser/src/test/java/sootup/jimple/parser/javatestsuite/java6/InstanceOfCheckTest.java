package sootup.jimple.parser.javatestsuite.java6;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.categories.Java8Test;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class InstanceOfCheckTest extends JimpleTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "instanceOfCheckMethod", "void", Collections.emptyList());
  }

  @org.junit.Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    SootClass<?> sootClass = loadClass(getDeclaredClassSignature());
    if (sootClass.getSuperclass().isPresent()) {
      assertEquals("InstanceOfCheckSuper", sootClass.getSuperclass().get().getClassName());
    }
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: InstanceOfCheck",
            "$stack2 = new InstanceOfCheck",
            "specialinvoke $stack2.<InstanceOfCheck: void <init>()>()",
            "l1 = $stack2",
            "$stack4 = <java.lang.System: java.io.PrintStream out>",
            "$stack3 = l1 instanceof InstanceOfCheckSuper",
            "virtualinvoke $stack4.<java.io.PrintStream: void println(boolean)>($stack3)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
