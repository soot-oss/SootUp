package sootup.core.jimple.common.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;

class JDynamicInvokeExprTest {

  private static ClassType dummyClassType() {
    return new ClassType() {
      @Override
      public String getFullyQualifiedName() {
        return JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME;
      }

      @Override
      public String getClassName() {
        return "InvokeDynamic";
      }

      @Override
      public PackageName getPackageName() {
        return SignatureInterner.getPackageName("sootup.dummy");
      }
    };
  }

  /**
   * Reproduces loss of method handle tag and failure to update methodSignature in
   * JDynamicInvokeExpr withers:
   *
   * <p>Subject pattern: Java lambdas, string concatenations (Indy), or pattern matching compiled to
   * `invokedynamic`. The invokedynamic instruction has an associated bootstrap method handle with a
   * tag (reference kind, e.g. H_INVOKESPECIAL, H_NEWINVOKESPECIAL).
   *
   * <p>Previously: 1. withBootstrapMethodSignature, withBootstrapArgs, withMethodSignature, and
   * withMethodArgs called the 4-argument constructor, resetting 'tag' to default H_INVOKESTATIC
   * (6). 2. withMethodSignature erroneously passed getMethodSignature() instead of methodSignature,
   * silently failing to apply the updated signature.
   */
  @Test
  void testWithersPreserveTagAndMethodSignature() {
    MethodSignature dummyMethod =
        SignatureInterner.getMethodSignature(
            dummyClassType(), "call", Collections.emptyList(), VoidType.getInstance());
    MethodSignature dummyMethod2 =
        SignatureInterner.getMethodSignature(
            dummyClassType(), "call2", Collections.emptyList(), VoidType.getInstance());
    MethodSignature bootstrapSig = TestUtil.createDummyMethodSignature();
    MethodSignature bootstrapSig2 =
        SignatureInterner.getMethodSignature(
            TestUtil.createDummyClassType(),
            SignatureInterner.getMethodSubSignature(
                "bootstrap2", VoidType.getInstance(), Collections.emptyList()));

    int originalTag = 7;
    List<Immediate> bsmArgs = Collections.singletonList(IntConstant.getInstance(1));
    List<Immediate> methodArgs = Collections.singletonList(IntConstant.getInstance(2));

    JDynamicInvokeExpr expr =
        new JDynamicInvokeExpr(bootstrapSig, bsmArgs, dummyMethod, originalTag, methodArgs);

    assertEquals(originalTag, expr.getHandleTag());

    JDynamicInvokeExpr withBsm = expr.withBootstrapMethodSignature(bootstrapSig2);
    assertEquals(originalTag, withBsm.getHandleTag());
    assertEquals(bootstrapSig2, withBsm.getBootstrapMethodSignature());

    List<Immediate> newBsmArgs = Collections.singletonList(IntConstant.getInstance(10));
    JDynamicInvokeExpr withBsmArgs = expr.withBootstrapArgs(newBsmArgs);
    assertEquals(originalTag, withBsmArgs.getHandleTag());
    assertEquals(newBsmArgs, withBsmArgs.getBootstrapArgs());

    JDynamicInvokeExpr withMethod = expr.withMethodSignature(dummyMethod2);
    assertEquals(originalTag, withMethod.getHandleTag());
    assertEquals(dummyMethod2, withMethod.getMethodSignature());

    List<Immediate> newMethodArgs = Collections.singletonList(IntConstant.getInstance(20));
    JDynamicInvokeExpr withArgs = expr.withMethodArgs(newMethodArgs);
    assertEquals(originalTag, withArgs.getHandleTag());
    assertEquals(newMethodArgs, withArgs.getArgs());
  }
}
