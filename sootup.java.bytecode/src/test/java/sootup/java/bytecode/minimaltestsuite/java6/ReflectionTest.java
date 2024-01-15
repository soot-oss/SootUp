package sootup.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class ReflectionTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "checkReflection", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public Reflection(){
   * s="String";
   * }
   * public void checkReflection()  throws  NoSuchMethodException{
   *
   * Reflection reflection = new Reflection();
   * Class reflectionClass = Reflection.class;
   * System.out.println(reflectionClass);
   * Constructor constructor = reflectionClass.getConstructor();
   * System.out.println(constructor.getName());
   * System.out.println(reflectionClass.getMethods().length);
   * }
   *
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: Reflection",
            "$stack4 = new Reflection",
            "specialinvoke $stack4.<Reflection: void <init>()>()",
            "l1 = $stack4",
            "l2 = class \"LReflection;\"",
            "$stack5 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.Object)>(l2)",
            "$stack6 = newarray (java.lang.Class)[0]",
            "l3 = virtualinvoke l2.<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>($stack6)",
            "$stack7 = <java.lang.System: java.io.PrintStream out>",
            "$stack8 = virtualinvoke l3.<java.lang.reflect.Constructor: java.lang.String getName()>()",
            "virtualinvoke $stack7.<java.io.PrintStream: void println(java.lang.String)>($stack8)",
            "$stack9 = <java.lang.System: java.io.PrintStream out>",
            "$stack10 = virtualinvoke l2.<java.lang.Class: java.lang.reflect.Method[] getMethods()>()",
            "$stack11 = lengthof $stack10",
            "virtualinvoke $stack9.<java.io.PrintStream: void println(int)>($stack11)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
