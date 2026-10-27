package sootup.apk.frontend.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.JCmpExpr;
import sootup.core.jimple.common.expr.JCmpgExpr;
import sootup.core.jimple.common.expr.JCmplExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.UnknownType;

/**
 * Dex has a comparison for longs ({@code cmp-long}) and two for floats and doubles, which differ in
 * the result for NaN ({@code cmpl-*}: -1, {@code cmpg-*}: 1). Jimple has the same three: {@code
 * cmp}, {@code cmpl} and {@code cmpg}.
 */
@ExtendWith(MockitoExtension.class)
public class CmpInstructionTest {

  @Mock private DexBody body;

  private final Local result = Jimple.newLocal("result", UnknownType.getInstance());
  private final Local first = Jimple.newLocal("first", UnknownType.getInstance());
  private final Local second = Jimple.newLocal("second", UnknownType.getInstance());

  /** Translates the comparison with the given opcode and returns its expression */
  private Value jimplify(Opcode opcode) {
    Instruction23x instr = mock(Instruction23x.class);
    when(instr.getOpcode()).thenReturn(opcode);
    when(instr.getRegisterA()).thenReturn(0);
    when(instr.getRegisterB()).thenReturn(1);
    when(instr.getRegisterC()).thenReturn(2);
    when(body.getRegisterLocal(0)).thenReturn(result);
    when(body.getRegisterLocal(1)).thenReturn(first);
    when(body.getRegisterLocal(2)).thenReturn(second);

    new CmpInstruction(instr, 0).jimplify(body);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(body).add(captor.capture());
    JAssignStmt stmt = captor.getValue();
    assertSame(result, stmt.getLeftOp());
    AbstractBinopExpr expr = (AbstractBinopExpr) stmt.getRightOp();
    assertSame(first, expr.getOp1());
    assertSame(second, expr.getOp2());
    return expr;
  }

  @Test
  public void cmplFloatIsCmpl() {
    assertEquals(JCmplExpr.class, jimplify(Opcode.CMPL_FLOAT).getClass());
  }

  @Test
  public void cmplDoubleIsCmpl() {
    assertEquals(JCmplExpr.class, jimplify(Opcode.CMPL_DOUBLE).getClass());
  }

  @Test
  public void cmpgFloatIsCmpg() {
    assertEquals(JCmpgExpr.class, jimplify(Opcode.CMPG_FLOAT).getClass());
  }

  @Test
  public void cmpgDoubleIsCmpg() {
    assertEquals(JCmpgExpr.class, jimplify(Opcode.CMPG_DOUBLE).getClass());
  }

  @Test
  public void cmpLongIsCmp() {
    assertEquals(JCmpExpr.class, jimplify(Opcode.CMP_LONG).getClass());
  }
}
