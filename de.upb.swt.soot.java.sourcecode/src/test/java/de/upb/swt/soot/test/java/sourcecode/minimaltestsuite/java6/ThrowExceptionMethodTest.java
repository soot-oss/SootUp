package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class ThrowExceptionMethodTest extends MinimalSourceTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "divideByZero", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public MethodSignature getThrowCustomExceptionSignature() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     void divideByZero() throws ArithmeticException{
   * 			int i=8/0;
   *        }
   * </pre>
   */

  /**  <pre>	void divideByZero() throws ArithmeticException{
   * int i=8/0;
   * }
   * void throwCustomException() throws CustomException {
   * throw new CustomException("Custom Exception");
   * }
   * } catch( CustomException e){
   * <pre>*/
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of("r0 := @this: ThrowExceptionMethod", "$i0 = 8 / 0", "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public MethodSignature getMethodSignature1() {
    return identifierFactory.getMethodSignature(
        "throwCustomException", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /**
   *
   *
   * <pre>
   *     void throwCustomException() throws CustomException {
   * 		throw new CustomException("Custom Exception");
   *        }
   * </pre>
   */
  public List<String> expectedBodyStmts1() {
    return Stream.of(
            "r0 := @this: ThrowExceptionMethod",
            "$r1 = new CustomException",
            "specialinvoke $r1.<CustomException: void <init>(java.lang.String)>(\"Custom Exception\")",
            "throw $r1")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Ignore
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("ArithmeticException")));
    method = loadMethod(getMethodSignature1());
    assertJimpleStmts(method, expectedBodyStmts1());
    assertTrue(
        method.getExceptionSignatures().stream()
            .anyMatch(classType -> classType.getClassName().equals("CustomException")));
  }
}
