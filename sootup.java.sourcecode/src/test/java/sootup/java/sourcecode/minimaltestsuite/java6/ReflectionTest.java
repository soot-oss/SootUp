package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

public class ReflectionTest extends MinimalSourceTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "checkReflection", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public void checkReflection()  throws  NoSuchMethodException{
   *
   * Reflection reflection = new Reflection();
   * Class reflectionClass = Reflection.class;
   * System.out.println(reflectionClass);
   * Constructor constructor = reflectionClass.getConstructor();
   * System.out.println(constructor.getName());
   * System.out.println(reflectionClass.getMethods().length);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: Reflection",
            "$r1 = new Reflection",
            "specialinvoke $r1.<Reflection: void <init>()>()",
            // TODO: [ms] check whether this representation of the class is like intended/correct
            "$r2 = class \"Ljava/lang/Class\"",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.Object)>($r2)",
            "$r4 = newarray (java.lang.Class)[0]",
            "$r5 = virtualinvoke $r2.<java.lang.Class: java.lang.reflect.Constructor getConstructor(java.lang.Class[])>($r4)",
            "$r6 = <java.lang.System: java.io.PrintStream out>",
            "$r7 = virtualinvoke $r5.<java.lang.reflect.Constructor: java.lang.String getName()>()",
            "virtualinvoke $r6.<java.io.PrintStream: void println(java.lang.String)>($r7)",
            "$r8 = <java.lang.System: java.io.PrintStream out>",
            "$r9 = virtualinvoke $r2.<java.lang.Class: java.lang.reflect.Method[] getMethods()>()",
            "$i0 = lengthof $r9",
            "virtualinvoke $r8.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
