package sootup.java.bytecode.frontend.minimaltestsuite.java6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/**
 * @author Kaustubh Kelkar
 */
public class DeclareConstructorTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignatureInitOneParam() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "<init>", "void", Collections.singletonList("int"));
  }

  public MethodSignature getMethodSignatureInitTwoParam() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "<init>", "void", Arrays.asList("int", "int"));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignatureInitOneParam());
    assertJimpleStmts(method, expectedBodyStmts());
    method = loadMethod(getMethodSignatureInitTwoParam());
    assertJimpleStmts(method, expectedBodyStmts1());
  }

  /**
   *
   *
   * <pre>
   * public DeclareConstructor(int var1){
   *         this.var1=var1;
   *         this.var2=0;
   *     }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "this := @this: DeclareConstructor",
            "l1 := @parameter0: int",
            "specialinvoke this.<java.lang.Object: void <init>()>()",
            "this.<DeclareConstructor: int var1> = l1",
            "this.<DeclareConstructor: int var2> = 0",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   *
   *
   * <pre>
   *     public DeclareConstructor(int var1, int var2){
   *         this.var1=var1;
   *         this.var2=var2;
   *     }
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "this := @this: DeclareConstructor",
            "l1 := @parameter0: int",
            "l2 := @parameter1: int",
            "specialinvoke this.<java.lang.Object: void <init>()>()",
            "this.<DeclareConstructor: int var1> = l1",
            "this.<DeclareConstructor: int var2> = l2",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
