package sootup.util;

import java.util.Collections;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.signatures.MethodSignature;

public class InvokeExprUtil {

  /** will return a dummy virtual invoke expression
   * local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt;
   * arguments is an empty list
   */
  public static JVirtualInvokeExpr createDummyVirtualInvokeExpr() {
    Local local = LocalUtil.createDummyLocalForObject();
    MethodSignature methodSignature = SignatureUtil.createDummyMethodSignature();
    return new JVirtualInvokeExpr(local,methodSignature,Collections.emptyList());
  }

  /** will return a dummy special invoke expression
   * local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt;
   * arguments is an empty list
   */
  public static JSpecialInvokeExpr createDummySpecialInvokeExpr() {
    Local local = LocalUtil.createDummyLocalForObject();
    MethodSignature methodSignature = SignatureUtil.createDummyMethodSignature();
    return new JSpecialInvokeExpr(local,methodSignature,Collections.emptyList());
  }

  /** will return a dummy interface invoke expression
   * local is called a and has the class type Test
   * method signature is &lt;Test: int test ()&gt;
   * arguments is an empty list
   */
  public static JInterfaceInvokeExpr createDummyInterfaceInvokeExpr() {
    Local local = LocalUtil.createDummyLocalForObject();
    MethodSignature methodSignature = SignatureUtil.createDummyMethodSignature();
    return new JInterfaceInvokeExpr(local,methodSignature,Collections.emptyList());
  }

  /** will return a dummy static invoke expression
   * method signature is &lt;Test: int test ()&gt;
   * arguments is an empty list
   */
  public static JStaticInvokeExpr createDummyStaticInvokeExpr() {
    MethodSignature methodSignature = SignatureUtil.createDummyMethodSignature();
    return new JStaticInvokeExpr(methodSignature,Collections.emptyList());
  }

}
