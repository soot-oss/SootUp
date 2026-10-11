package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;

/** Defines tests for the {@link sootup.interceptors.Evaluator} class. */
public class EvaluatorTest {
  private static final Type STRING = new JavaIdentifierFactory().getType("java.lang.String");

  @Test
  public void testIdentityCasts() {
    JCastExpr expr = new JCastExpr(LongConstant.getInstance(42L), PrimitiveType.getLong());

    Constant cst = Evaluator.getConstantValueOf(expr);
    assertEquals(LongConstant.getInstance(42L), cst);
  }

  @Test
  public void testWideningCastsLossless() {
    // Int -> Long
    assertEquals(
        LongConstant.getInstance(42L),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getLong())));

    // Int -> Float
    // Lossless only when the value can fit in 24 bits.
    assertEquals(
        FloatConstant.getInstance(42.0f),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getFloat())));

    // Int -> Double
    assertEquals(
        DoubleConstant.getInstance(42.0D),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getDouble())));

    // Float -> Double
    assertEquals(
        DoubleConstant.getInstance(42.0D),
        Evaluator.getConstantValueOf(
            new JCastExpr(FloatConstant.getInstance(42.0f), PrimitiveType.getDouble())));
  }

  @Test
  public void testWideningCastsLossy() {
    // 2^24 + 1
    JCastExpr expr = new JCastExpr(IntConstant.getInstance(16777217), PrimitiveType.getFloat());
    assertEquals(FloatConstant.getInstance(16777216.0f), Evaluator.getConstantValueOf(expr));
  }

  @Test
  public void testNarrowingCasts() {
    // 0000 0001 1000 0000 (384) -> 0b1000 0000 (-128)
    assertEquals(
        IntConstant.getInstance(-128),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(384), PrimitiveType.getByte())));

    // 0000 0000 0000 0001 0000 0001 0000 0000 (65792) -> 0000 0001 0000 0000 (256)
    assertEquals(
        IntConstant.getInstance(256),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(65792), PrimitiveType.getShort())));

    // 0000 0000 0000 0001 1000 0000 0000 0000 (98304) -> 1000 0000 0000 0000 (signed -32768)
    assertEquals(
        IntConstant.getInstance(-32768),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(98304), PrimitiveType.getShort())));

    // 0000 0000 0000 0001 1000 0000 0000 0000 (98304) -> 1000 0000 0000 0000 (unsigned 32768)
    assertEquals(
        IntConstant.getInstance(32768),
        Evaluator.getConstantValueOf(
            new JCastExpr(IntConstant.getInstance(98304), PrimitiveType.getChar())));

    // NaN is converted to 0 or 0L
    assertEquals(
        IntConstant.getInstance(0),
        Evaluator.getConstantValueOf(
            new JCastExpr(FloatConstant.getInstance(Float.NaN), PrimitiveType.getInt())));
  }

  /**
   * Constants in dex code are untyped until the types are assigned, so a binary expression can have
   * constant operands of different types, e.g., an int and a float. Such an expression is not
   * folded.
   */
  @Test
  public void testMixedConstantTypesAreNotFolded() {
    JDivExpr div = new JDivExpr(IntConstant.getInstance(1), FloatConstant.getInstance(2.0f));
    assertFalse(Evaluator.isConstantValue(div));
    assertNull(Evaluator.getConstantValueOf(div));

    assertFalse(
        Evaluator.isConstantValue(
            new JAddExpr(LongConstant.getInstance(1L), IntConstant.getInstance(2))));
    assertFalse(
        Evaluator.isConstantValue(
            new JLtExpr(DoubleConstant.getInstance(1.0), FloatConstant.getInstance(2.0f))));
    assertFalse(
        Evaluator.isConstantValue(
            new JEqExpr(IntConstant.getInstance(1), FloatConstant.getInstance(1.0f))));
  }

  @Test
  public void testConstantsOfTheSameTypeAreFolded() {
    assertEquals(
        IntConstant.getInstance(3),
        Evaluator.getConstantValueOf(
            new JAddExpr(IntConstant.getInstance(1), IntConstant.getInstance(2))));
    assertEquals(
        FloatConstant.getInstance(0.5f),
        Evaluator.getConstantValueOf(
            new JDivExpr(FloatConstant.getInstance(1.0f), FloatConstant.getInstance(2.0f))));
    // the shift distance of a long is an int
    assertEquals(
        LongConstant.getInstance(4L),
        Evaluator.getConstantValueOf(
            new JShlExpr(LongConstant.getInstance(1L), IntConstant.getInstance(2))));
    // strings are compared with each other, regardless of the types of the operands
    assertEquals(
        IntConstant.getInstance(0),
        Evaluator.getConstantValueOf(
            new JEqExpr(new StringConstant("a", STRING), new StringConstant("b", STRING))));
  }
}
