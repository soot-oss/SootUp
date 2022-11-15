package sootup.java.sourcecode.minimaltestsuite.java6;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
public class UnicodeMethodNameTest extends MinimalSourceTestSuiteBase {
  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "αρετηAsClassName", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   * public class UnicodeMethodName {
   *     public void αρετη(){
   *         System.out.println("this is αρετη method");
   *     }
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: αρετη",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"this is αρετη class\")",
            "return")
        .collect(Collectors.toList());
  }

  @Ignore
  public void test() {
    // this only works on Unicode filesystems
    /**
     * Exception in thread "main" java.nio.file.InvalidPathException: Illegal char <?> at index 1:
     * a?et?.java
     */
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    System.out.println(sootClass.getClassSource().getClassType().getClassName());
  }
}
