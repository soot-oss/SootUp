package sootup.apk.frontend.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

@ExtendWith(MockitoExtension.class)
public class FieldInstructionTest {

  @Mock private DexBody mockBody;

  @Mock private FieldReference mockFieldRef;

  @Mock private Local localA;

  @Mock private Local localB;

  @Mock private Local localC;

  @BeforeEach
  public void setUp() {}

  @Test
  public void testIputInstruction() {
    TwoRegisterInstruction instr =
        mock(
            TwoRegisterInstruction.class,
            withSettings().extraInterfaces(ReferenceInstruction.class));

    when(instr.getRegisterA()).thenReturn(0); // source
    when(instr.getRegisterB()).thenReturn(1); // object
    when(((ReferenceInstruction) instr).getReference()).thenReturn(mockFieldRef);
    when(mockFieldRef.getDefiningClass()).thenReturn("Ljava/lang/Object;");
    when(mockFieldRef.getName()).thenReturn("myField");
    when(mockFieldRef.getType()).thenReturn("I");

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);
    when(mockBody.getRegisterLocal(1)).thenReturn(localB);

    IputInstruction iput = new IputInstruction(instr, 0);
    iput.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getRightOp());
    assertTrue(stmt.getLeftOp() instanceof JInstanceFieldRef);
    assertEquals(localB, ((JInstanceFieldRef) stmt.getLeftOp()).getBase());
  }

  @Test
  public void testIgetInstruction() {
    TwoRegisterInstruction instr =
        mock(
            TwoRegisterInstruction.class,
            withSettings().extraInterfaces(ReferenceInstruction.class));

    when(instr.getRegisterA()).thenReturn(0); // dest
    when(instr.getRegisterB()).thenReturn(1); // object
    when(((ReferenceInstruction) instr).getReference()).thenReturn(mockFieldRef);
    when(mockFieldRef.getDefiningClass()).thenReturn("Ljava/lang/Object;");
    when(mockFieldRef.getName()).thenReturn("myField");
    when(mockFieldRef.getType()).thenReturn("I");

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);
    when(mockBody.getRegisterLocal(1)).thenReturn(localB);

    IgetInstruction iget = new IgetInstruction(instr, 0);
    iget.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getLeftOp());
    assertTrue(stmt.getRightOp() instanceof JInstanceFieldRef);
    assertEquals(localB, ((JInstanceFieldRef) stmt.getRightOp()).getBase());
  }

  @Test
  public void testSputInstruction() {
    OneRegisterInstruction instr =
        mock(
            OneRegisterInstruction.class,
            withSettings().extraInterfaces(ReferenceInstruction.class));

    when(instr.getRegisterA()).thenReturn(0); // source
    when(((ReferenceInstruction) instr).getReference()).thenReturn(mockFieldRef);
    when(mockFieldRef.getDefiningClass()).thenReturn("Ljava/lang/Object;");
    when(mockFieldRef.getName()).thenReturn("myField");
    when(mockFieldRef.getType()).thenReturn("I");

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);

    SputInstruction sput = new SputInstruction(instr, 0);
    sput.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getRightOp());
    assertTrue(stmt.getLeftOp() instanceof JStaticFieldRef);
  }

  @Test
  public void testSgetInstruction() {
    OneRegisterInstruction instr =
        mock(
            OneRegisterInstruction.class,
            withSettings().extraInterfaces(ReferenceInstruction.class));

    when(instr.getRegisterA()).thenReturn(0); // dest
    when(((ReferenceInstruction) instr).getReference()).thenReturn(mockFieldRef);
    when(mockFieldRef.getDefiningClass()).thenReturn("Ljava/lang/Object;");
    when(mockFieldRef.getName()).thenReturn("myField");
    when(mockFieldRef.getType()).thenReturn("I");

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);

    SgetInstruction sget = new SgetInstruction(instr, 0);
    sget.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getLeftOp());
    assertTrue(stmt.getRightOp() instanceof JStaticFieldRef);
  }

  @Test
  public void testAputInstruction() {
    Instruction23x instr = mock(Instruction23x.class);

    when(instr.getRegisterA()).thenReturn(0); // source
    when(instr.getRegisterB()).thenReturn(1); // arrayBase
    when(instr.getRegisterC()).thenReturn(2); // index

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);
    when(mockBody.getRegisterLocal(1)).thenReturn(localB);
    when(mockBody.getRegisterLocal(2)).thenReturn(localC);

    AputInstruction aput = new AputInstruction(instr, 0);
    aput.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getRightOp());
    assertTrue(stmt.getLeftOp() instanceof JArrayRef);
    assertEquals(localB, ((JArrayRef) stmt.getLeftOp()).getBase());
    assertEquals(localC, ((JArrayRef) stmt.getLeftOp()).getIndex());
  }

  @Test
  public void testAgetInstruction() {
    Instruction23x instr = mock(Instruction23x.class);

    when(instr.getRegisterA()).thenReturn(0); // dest
    when(instr.getRegisterB()).thenReturn(1); // arrayBase
    when(instr.getRegisterC()).thenReturn(2); // index

    when(mockBody.getRegisterLocal(0)).thenReturn(localA);
    when(mockBody.getRegisterLocal(1)).thenReturn(localB);
    when(mockBody.getRegisterLocal(2)).thenReturn(localC);

    AgetInstruction aget = new AgetInstruction(instr, 0);
    aget.jimplify(mockBody);

    ArgumentCaptor<JAssignStmt> captor = ArgumentCaptor.forClass(JAssignStmt.class);
    verify(mockBody).add(captor.capture());

    JAssignStmt stmt = captor.getValue();
    assertEquals(localA, stmt.getLeftOp());
    assertTrue(stmt.getRightOp() instanceof JArrayRef);
    assertEquals(localB, ((JArrayRef) stmt.getRightOp()).getBase());
    assertEquals(localC, ((JArrayRef) stmt.getRightOp()).getIndex());
  }
}
