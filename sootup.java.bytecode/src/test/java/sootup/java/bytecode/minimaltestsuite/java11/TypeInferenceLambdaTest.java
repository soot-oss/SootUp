package sootup.java.bytecode.minimaltestsuite.java11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Bastian Haverkamp */
public class TypeInferenceLambdaTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "lambda", "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *   public void lambda() {
   *     BinaryOperator<Integer> binOp = (var x, var y) -> x+y;
   *     int result = binOp.apply(2,3);
   *   }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: TypeInferenceLambda",
            "l1 = dynamicinvoke \"apply\" <java.util.function.BinaryOperator ()>() <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(methodtype: java.lang.Object __METHODTYPE__(java.lang.Object,java.lang.Object), handle: <TypeInferenceLambda: java.lang.Integer lambda$lambda$0(java.lang.Integer,java.lang.Integer)>, methodtype: java.lang.Integer __METHODTYPE__(java.lang.Integer,java.lang.Integer))",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack3 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack5 = interfaceinvoke l1.<java.util.function.BinaryOperator: java.lang.Object apply(java.lang.Object,java.lang.Object)>($stack4, $stack3)",
            "$stack6 = (java.lang.Integer) $stack5",
            "l2 = virtualinvoke $stack6.<java.lang.Integer: int intValue()>()",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
