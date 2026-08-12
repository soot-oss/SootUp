import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import sootup.apk.backend.DexMethodBuilder;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.*;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.java.core.*;

public class DexMethodTest {

  @Test
  public void testMethodCreation() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();

    MethodSignature methodSig =
        factory.getMethodSignature("com.example.Example", "testMethod", "int", List.of());

    Local x = Jimple.newLocal("x", PrimitiveType.getInt());

    JReturnStmt returnStmt = Jimple.newReturnStmt(x, StmtPositionInfo.getNoStmtPositionInfo());

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();

    graph.setStartingStmt(returnStmt);
    graph.addNode(returnStmt);

    Body body = Body.builder(graph).setMethodSignature(methodSig).build();

    BodySource bodySource =
        new BodySource() {
          @Override
          public @NonNull Body resolveBody(@NonNull Iterable<MethodModifier> modifiers)
              throws ResolveException, IOException {
            return body;
          }

          @Override
          public Object resolveAnnotationsDefaultValue() {
            return null;
          }

          @Override
          public @NonNull MethodSignature getSignature() {
            return methodSig;
          }
        };

    JavaSootMethod method =
        new JavaSootMethod(
            bodySource,
            methodSig,
            Set.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            List.of(),
            NoPositionInformation.getInstance());

    DexMethodBuilder dexMethodBuilder = new DexMethodBuilder(null);
    ImmutableMethod dexMethod = dexMethodBuilder.createMethod(method);

    assertEquals("testMethod", dexMethod.getName());
    assertEquals("Lcom/example/Example;", dexMethod.getDefiningClass());
    assertEquals(List.of(), dexMethod.getParameterTypes());
    assertEquals("I", dexMethod.getReturnType());

    ImmutableMethodImplementation immutableMethodImplementation = dexMethod.getImplementation();
    assertNotNull(immutableMethodImplementation);
    ImmutableList<? extends ImmutableInstruction> instructions =
        immutableMethodImplementation.getInstructions();
    assertEquals(1, instructions.size());
    ImmutableInstruction instruction = instructions.get(0);
    assertEquals(Opcode.RETURN, instruction.getOpcode());
    assertEquals(1, instruction.getCodeUnits());
    assertEquals(Format.Format11x, instruction.getFormat());
  }

  @Test
  public void testIf() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();

    MethodSignature methodSig =
        factory.getMethodSignature("com.example.Example", "testMethod", "int", List.of());

    Local x = new Local("x", PrimitiveType.getInt());
    Local y = new Local("y", PrimitiveType.getInt());
    JAssignStmt s1 =
        Jimple.newAssignStmt(
            x, IntConstant.getInstance(1), StmtPositionInfo.getNoStmtPositionInfo());
    JIfStmt s2 =
        Jimple.newIfStmt(
            new JEqExpr(x, IntConstant.getInstance(0)), StmtPositionInfo.getNoStmtPositionInfo());

    JAssignStmt s3 =
        Jimple.newAssignStmt(
            y, IntConstant.getInstance(10), StmtPositionInfo.getNoStmtPositionInfo());
    JAssignStmt s4 =
        Jimple.newAssignStmt(
            y, IntConstant.getInstance(20), StmtPositionInfo.getNoStmtPositionInfo());
    JReturnStmt s6 = Jimple.newReturnStmt(y, StmtPositionInfo.getNoStmtPositionInfo());
    JReturnStmt s7 = Jimple.newReturnStmt(y, StmtPositionInfo.getNoStmtPositionInfo());

    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(s1);
    graph.addBlock(List.of(s1, s2));
    graph.addBlock(List.of(s3, s6));
    graph.addBlock(List.of(s4, s7));

    graph.putEdge(s2, 0, s3);
    graph.putEdge(s2, 1, s4);

    Body body = Body.builder(graph).setMethodSignature(methodSig).build();

    BodySource bodySource =
        new BodySource() {
          @Override
          public @NonNull Body resolveBody(@NonNull Iterable<MethodModifier> modifiers)
              throws ResolveException, IOException {
            return body;
          }

          @Override
          public Object resolveAnnotationsDefaultValue() {
            return null;
          }

          @Override
          public @NonNull MethodSignature getSignature() {
            return methodSig;
          }
        };

    JavaSootMethod method =
        new JavaSootMethod(
            bodySource,
            methodSig,
            Set.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            List.of(),
            NoPositionInformation.getInstance());

    DexMethodBuilder dexMethodBuilder = new DexMethodBuilder(null);
    ImmutableMethod dexMethod = dexMethodBuilder.createMethod(method);

    assertEquals("testMethod", dexMethod.getName());
    assertEquals("Lcom/example/Example;", dexMethod.getDefiningClass());
    assertEquals(List.of(), dexMethod.getParameterTypes());
    assertEquals("I", dexMethod.getReturnType());

    ImmutableMethodImplementation immutableMethodImplementation = dexMethod.getImplementation();
    assertNotNull(immutableMethodImplementation);
    ImmutableList<? extends ImmutableInstruction> instructions =
        immutableMethodImplementation.getInstructions();
    assertEquals(6, instructions.size());

    ImmutableInstruction ifInstruction = instructions.get(1);
    assertEquals(Opcode.IF_EQZ, ifInstruction.getOpcode());
    assertEquals(2, ifInstruction.getCodeUnits());

    int[] addresses = new int[instructions.size()];
    int address = 0;
    for (int i = 0; i < instructions.size(); i++) {
      addresses[i] = address;
      address += instructions.get(i).getCodeUnits();
    }

    int ifIndex = -1;
    int ifAddress = -1;
    OffsetInstruction ifBranch = (OffsetInstruction) ifInstruction;

    for (int i = 0; i < instructions.size(); i++) {
      Opcode opcode = instructions.get(i).getOpcode();

      if (opcode == Opcode.IF_EQZ) {
        ifIndex = i;
        ifAddress = addresses[ifIndex];
        break;
      }
    }

    assertTrue(ifIndex >= 0);

    int branchAddress = ifAddress + ifBranch.getCodeOffset();
    int targetIndex = findInstructionAt(addresses, branchAddress);
    assertTrue(targetIndex >= 0);

    int fallThroughIndex = ifIndex + 1;
    assertTrue(fallThroughIndex < instructions.size());

    assertNotEquals(fallThroughIndex, targetIndex);

    ImmutableInstruction returnInstruction = instructions.get(5);
    assertEquals(Opcode.RETURN, returnInstruction.getOpcode());
    assertEquals(1, returnInstruction.getCodeUnits());
    assertEquals(Format.Format11x, returnInstruction.getFormat());
  }

  private int findInstructionAt(int[] addresses, int address) {

    for (int i = 0; i < addresses.length; i++) {
      if (addresses[i] == address) {
        return i;
      }
    }

    return -1;
  }
}
