package sootup.apk.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.jimple.visitor.AbstractStmtVisitor;

public class DexStmtVisitor extends AbstractStmtVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexStmtVisitor.class);

  List<BuilderInstruction> instructions;

  DexBuilder dexBuilder;
  MethodImplementationBuilder methodImplementationBuilder = new MethodImplementationBuilder(10);
  DexConstantVisitor dexConstantVisitor;
  DexExprVisitor dexExprVisitor;
  DexRefVisitor dexRefVisitor;
  RegisterAllocator registerAllocator;

  public DexStmtVisitor() {
    instructions = new ArrayList<>();
    dexBuilder = new DexBuilder(Opcodes.getDefault());
    dexConstantVisitor = new DexConstantVisitor(dexBuilder, this);
    registerAllocator = new RegisterAllocator(dexConstantVisitor);
    dexExprVisitor =
        new DexExprVisitor(dexBuilder, methodImplementationBuilder, registerAllocator, this);
    dexRefVisitor = new DexRefVisitor(this, registerAllocator);
  }

  @Override
  public void caseBreakpointStmt(@NonNull JBreakpointStmt stmt) {}

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    Optional<AbstractInvokeExpr> optionalExpr = stmt.getInvokeExpr();
    optionalExpr.ifPresent(abstractInvokeExpr -> abstractInvokeExpr.accept(dexExprVisitor));
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    LValue leftOp = stmt.getLeftOp();
    Value rightOp = stmt.getRightOp();

    // TODO simplify arrayInitialization with fill-array-data

    if (leftOp instanceof Ref && rightOp instanceof Ref) {
      throw new RuntimeException("Both left-hand side and right-hand side of AssignStmt are ref");
    }

    if (leftOp instanceof Ref ref) {
      if (!(rightOp instanceof Immediate rightOpImmediate)) {
        throw new RuntimeException(
            "Right-side of AssignStmt is no Immediate: " + rightOp.getType());
      }
      Register register = registerAllocator.getRegisterForImmediate(rightOpImmediate);
      dexRefVisitor.setOperation("PUT");
      dexRefVisitor.setRegister(register);
      ref.accept(dexRefVisitor);

    } else if (leftOp instanceof Local leftOpLocal) {
      Register targetRegister = registerAllocator.getRegisterForImmediate(leftOpLocal);

      if (rightOp instanceof Constant constant) {
        dexConstantVisitor.setTargetRegister(targetRegister);
        constant.accept(dexConstantVisitor);

      } else if (rightOp instanceof Expr expr) {
        dexExprVisitor.setTargetRegister(targetRegister);
        expr.accept(dexExprVisitor);

      } else if (rightOp instanceof Ref ref) {
        dexRefVisitor.setOperation("GET");
        dexRefVisitor.setRegister(targetRegister);
        ref.accept(dexRefVisitor);

      } else if (rightOp instanceof Local sourceLocal) {
        if (leftOpLocal != sourceLocal) {
          Register sourceRegister = registerAllocator.getRegisterForImmediate(sourceLocal);
          dexExprVisitor.generateMoveInstruction(
              targetRegister, sourceRegister, sourceLocal.getType());
        }

      } else {
        throw new IllegalArgumentException(
            "Unknown type of rightOp in AssignStmt: " + rightOp.getClass());
      }
    } else {
      throw new RuntimeException(
          "Left-hand side of AssignStmt is neither Ref nor Local: " + leftOp.getClass());
    }
  }

  @Override
  public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
    IdentityRef op2 = stmt.getRightOp();
    if (op2 instanceof JCaughtExceptionRef) {
      Register register = registerAllocator.getRegisterForImmediate(stmt.getLeftOp());
      dexRefVisitor.setRegister(register);
    }
    op2.accept(dexRefVisitor);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op);
    log.info("monitor-enter v{}", register.getNumber());
    this.addInstruction(new BuilderInstruction11x(Opcode.MONITOR_ENTER, register.getNumber()));
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op);
    log.info("monitor-exit v{}", register.getNumber());
    this.addInstruction(new BuilderInstruction11x(Opcode.MONITOR_EXIT, register.getNumber()));
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    // goto
    // goto/16
    // goto/32
    // TODO
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    // if-*
    // TODO
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    log.info("nop");
    this.addInstruction(new BuilderInstruction10x(Opcode.NOP));
  }

  @Override
  public void caseRetStmt(@NonNull JRetStmt stmt) {
    throw new RuntimeException("JRetStmt should not occur in android bytecode");
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op);
    String dexType = DexUtil.toDexType(op.getType());
    Opcode opcode;
    if (DexUtil.isObject(dexType)) {
      opcode = Opcode.RETURN_OBJECT;
    } else if (DexUtil.isWide(dexType)) {
      opcode = Opcode.RETURN_WIDE;
    } else {
      opcode = Opcode.RETURN;
    }
    log.info("{} v{}", opcode.toString().toLowerCase().replace("_", "-"), register.getNumber());
    this.addInstruction(new BuilderInstruction11x(opcode, register.getNumber()));
  }

  @Override
  public void caseReturnVoidStmt(@NonNull JReturnVoidStmt stmt) {
    log.info("return-void");
    this.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    // packed-switch
    // sparse-switch
    // TODO
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op);
    log.info("throw v{}", register.getNumber());
    this.addInstruction(new BuilderInstruction11x(Opcode.THROW, register.getNumber()));
  }

  @Override
  public void defaultCaseStmt(Stmt stmt) {
    throw new RuntimeException("Unknown Statement " + stmt.getClass());
  }

  protected void addInstruction(BuilderInstruction instruction) {
    instructions.add(instruction);
  }

  public List<BuilderInstruction> getInstructions() {
    return instructions;
  }
}
