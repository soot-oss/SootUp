package de.upb.swt.soot.java.bytecode.minimaltestsuite.java11;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Bastian Haverkamp */
public class TypeInferenceLambdaTest extends MinimalBytecodeTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "lambda", getDeclaredClassSignature(), "void", Collections.emptyList());
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
            "$stack3 = dynamicinvoke apply <java.util.function.BinaryOperator ()>() <java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>(methodtype: java.lang.Object __METHODTYPE__(java.lang.Object,java.lang.Object), handle: <TypeInferenceLambda: java.lang.Integer lambda$lambda$0(java.lang.Integer,java.lang.Integer)>, methodtype: java.lang.Integer __METHODTYPE__(java.lang.Integer,java.lang.Integer))",
            "l1 = $stack3",
            "$stack4 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(2)",
            "$stack5 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(3)",
            "$stack6 = interfaceinvoke l1.<java.util.function.BinaryOperator: java.lang.Object apply(java.lang.Object,java.lang.Object)>($stack4, $stack5)",
            "$stack7 = (java.lang.Integer) $stack6",
            "$stack8 = virtualinvoke $stack7.<java.lang.Integer: int intValue()>()",
            "l2 = $stack8",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
