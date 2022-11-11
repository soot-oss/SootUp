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
public class ReflectionTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "checkReflection", "void", Collections.emptyList());
  }

  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: Reflection",
            "$stack4 = new Reflection",
            "specialinvoke $stack4.<Reflection: void <init>()>()",
            "l1 = $stack4",
            "l2 = class Reflection",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.Object)>(l2)",
            "$stack6 = newarray (java.lang.Class)[0]",
            "$stack7 = virtualinvoke l2.<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>($stack6)",
            "l3 = $stack7",
            "$stack8 = <java.lang.System: java.io.PrintStream out>",
            "$stack9 = virtualinvoke l3.<java.lang.reflect.Constructor: java.lang.String getName()>()",
            "virtualinvoke $stack8.<java.io.PrintStream: void println(java.lang.String)>($stack9)",
            "$stack10 = <java.lang.System: java.io.PrintStream out>",
            "$stack11 = virtualinvoke l2.<java.lang.Class: java.lang.reflect.Method[] getMethods()>()",
            "$stack12 = lengthof $stack11",
            "virtualinvoke $stack10.<java.io.PrintStream: void println(int)>($stack12)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
