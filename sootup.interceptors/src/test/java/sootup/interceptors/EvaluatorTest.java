package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.types.PrimitiveType;

/**
 * Defines tests for the {@link sootup.interceptors.Evaluator} class.
 */
public class EvaluatorTest {
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
                Evaluator.getConstantValueOf(new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getLong()))
        );

        // Int -> Float
        // Lossless only when the value can fit in 24 bits.
        assertEquals(
                FloatConstant.getInstance(42.0f),
                Evaluator.getConstantValueOf(new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getFloat()))
        );

        // Int -> Double
        assertEquals(
                DoubleConstant.getInstance(42.0D),
                Evaluator.getConstantValueOf(new JCastExpr(IntConstant.getInstance(42), PrimitiveType.getDouble()))
        );

        // Float -> Double
        assertEquals(
                DoubleConstant.getInstance(42.0D),
                Evaluator.getConstantValueOf(new JCastExpr(FloatConstant.getInstance(42.0f), PrimitiveType.getDouble()))
        );
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
                Evaluator.getConstantValueOf(new JCastExpr(IntConstant.getInstance(384), PrimitiveType.getByte()))
        );

        // 0000 0000 0000 0001 0000 0001 0000 0000 (65792) -> 0000 0001 0000 0000 (256)
        assertEquals(
                IntConstant.getInstance(256),
                Evaluator.getConstantValueOf(new JCastExpr(IntConstant.getInstance(65792), PrimitiveType.getShort()))
        );

        // NaN is converted to 0 or 0L
        assertEquals(
                IntConstant.getInstance(0),
                Evaluator.getConstantValueOf(new JCastExpr(FloatConstant.getInstance(Float.NaN), PrimitiveType.getInt()))
        );
    }
}
