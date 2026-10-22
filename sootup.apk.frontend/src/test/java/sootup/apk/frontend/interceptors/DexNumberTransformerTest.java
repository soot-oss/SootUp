package sootup.apk.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.UnknownType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * Dex constants are untyped: an int constant may hold the bits of a float, a long constant those of
 * a double. The transformer gives a local's constants their floating point value when the local is
 * used as a float or double.
 */
class DexNumberTransformerTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();
  private static final JavaIdentifierFactory FACTORY = new JavaIdentifierFactory();
  private static final MethodSignature USE_FLOAT =
      FACTORY.parseMethodSignature("<example.Test: void use(float)>");
  private static final MethodSignature USE_INT =
      FACTORY.parseMethodSignature("<example.Test: void use(int)>");
  private static final MethodSignature USE_INT_FLOAT =
      FACTORY.parseMethodSignature("<example.Test: void use(int,float)>");
  private static final FieldSignature DOUBLE_FIELD =
      FACTORY.parseFieldSignature("<example.Test: double d>");

  private static Body.BodyBuilder builder(String returnType, List<Local> locals, Stmt... stmts) {
    var graph = new MutableBlockControlFlowGraph();
    for (int i = 0; i + 1 < stmts.length; i++) {
      graph.putEdge((FallsThroughStmt) stmts[i], stmts[i + 1]);
    }
    graph.setStartingStmt(stmts[0]);
    return Body.builder(graph)
        .setLocals(new LinkedHashSet<>(locals))
        .setMethodSignature(
            FACTORY.getMethodSignature(
                FACTORY.getClassType("example.Test"),
                "test",
                FACTORY.getType(returnType),
                List.of()));
  }

  private static void transform(Body.BodyBuilder builder) {
    new DexNumberTranformer().interceptBody(builder, new JavaView(Collections.emptyList()));
  }

  private static Object constantOfFirstStmt(Body.BodyBuilder builder) {
    return ((JAssignStmt) builder.getStmts().get(0)).getRightOp();
  }

  @Test
  void intBitsPassedAsFloatBecomeAFloat() {
    // l = 1078530011; use((float) l); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l),
            Jimple.newAssignStmt(l, IntConstant.getInstance(Float.floatToIntBits(3.5f)), POS),
            Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_FLOAT, l), POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(FloatConstant.getInstance(3.5f), constantOfFirstStmt(builder));
  }

  @Test
  void longBitsReturnedAsDoubleBecomeADouble() {
    // l = <bits of 2.5>; return l;  -- in a method that returns a double
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        builder(
            "double",
            List.of(l),
            Jimple.newAssignStmt(l, LongConstant.getInstance(Double.doubleToLongBits(2.5)), POS),
            Jimple.newReturnStmt(l, POS));

    transform(builder);

    assertEquals(DoubleConstant.getInstance(2.5), constantOfFirstStmt(builder));
  }

  @Test
  void longBitsStoredInADoubleFieldBecomeADouble() {
    // l = <bits of 0.25>; Test.d = l; return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l),
            Jimple.newAssignStmt(l, LongConstant.getInstance(Double.doubleToLongBits(0.25)), POS),
            Jimple.newAssignStmt(Jimple.newStaticFieldRef(DOUBLE_FIELD), l, POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(DoubleConstant.getInstance(0.25), constantOfFirstStmt(builder));
  }

  @Test
  void intPassedAsIntStaysAnInt() {
    // l = 7; use(l); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l),
            Jimple.newAssignStmt(l, IntConstant.getInstance(7), POS),
            Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_INT, l), POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(IntConstant.getInstance(7), constantOfFirstStmt(builder));
  }

  @Test
  void intBitsDividedByAFloatParameterBecomeAFloat() {
    // l = <bits of 6500.0f>; r = l / x; use(0, r); return;  -- x is a float
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local x = Jimple.newLocal("x", UnknownType.getInstance());
    Local r = Jimple.newLocal("r", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l, x, r),
            Jimple.newAssignStmt(l, IntConstant.getInstance(Float.floatToIntBits(6500f)), POS),
            Jimple.newAssignStmt(r, Jimple.newDivExpr(l, x), POS),
            Jimple.newInvokeStmt(
                Jimple.newStaticInvokeExpr(USE_INT_FLOAT, List.of(IntConstant.getInstance(0), r)),
                POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(FloatConstant.getInstance(6500f), constantOfFirstStmt(builder));
  }

  @Test
  void intBitsComparedWithCmplBecomeAFloat() {
    // l = <bits of 1.5f>; c = l cmpl x; use(c); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local x = Jimple.newLocal("x", UnknownType.getInstance());
    Local c = Jimple.newLocal("c", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l, x, c),
            Jimple.newAssignStmt(l, IntConstant.getInstance(Float.floatToIntBits(1.5f)), POS),
            Jimple.newAssignStmt(c, Jimple.newCmplExpr(l, x), POS),
            Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_INT, c), POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(FloatConstant.getInstance(1.5f), constantOfFirstStmt(builder));
  }

  @Test
  void longBitsInArithmeticOnAReturnedDoubleBecomeADouble() {
    // l = <bits of 0.5>; r = l * y; s = -r; return s;  -- in a method that returns a double
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local y = Jimple.newLocal("y", UnknownType.getInstance());
    Local r = Jimple.newLocal("r", UnknownType.getInstance());
    Local s = Jimple.newLocal("s", UnknownType.getInstance());
    var builder =
        builder(
            "double",
            List.of(l, y, r, s),
            Jimple.newAssignStmt(l, LongConstant.getInstance(Double.doubleToLongBits(0.5)), POS),
            Jimple.newAssignStmt(r, Jimple.newMulExpr(l, y), POS),
            Jimple.newAssignStmt(s, Jimple.newNegExpr(r), POS),
            Jimple.newReturnStmt(s, POS));

    transform(builder);

    assertEquals(DoubleConstant.getInstance(0.5), constantOfFirstStmt(builder));
  }

  @Test
  void intInIntArithmeticStaysAnInt() {
    // l = 7; r = l + y; use(r); return;
    Local l = Jimple.newLocal("l", UnknownType.getInstance());
    Local y = Jimple.newLocal("y", UnknownType.getInstance());
    Local r = Jimple.newLocal("r", UnknownType.getInstance());
    var builder =
        builder(
            "void",
            List.of(l, y, r),
            Jimple.newAssignStmt(l, IntConstant.getInstance(7), POS),
            Jimple.newAssignStmt(r, Jimple.newAddExpr(l, y), POS),
            Jimple.newInvokeStmt(Jimple.newStaticInvokeExpr(USE_INT, r), POS),
            Jimple.newReturnVoidStmt(POS));

    transform(builder);

    assertEquals(IntConstant.getInstance(7), constantOfFirstStmt(builder));
  }
}
