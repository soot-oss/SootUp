package sootup.apk.backend;

import java.util.*;
import org.jf.dexlib2.Opcode;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
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
import sootup.core.model.SootMethod;
import sootup.core.views.View;

public class DexStmtVisitor extends AbstractStmtVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexStmtVisitor.class);

  private final DexMethodBuilder dexMethodBuilder;
  private final DexConstantVisitor dexConstantVisitor;
  private final DexExprVisitor dexExprVisitor;
  private final DexRefVisitor dexRefVisitor;
  private final RegisterAllocator registerAllocator;
  private final View view;
  private final SootMethod sootMethod;

  public DexStmtVisitor(
      View view,
      RegisterAllocator registerAllocator,
      DexConstantVisitor dexConstantVisitor,
      DexMethodBuilder dexMethodBuilder,
      SootMethod sootMethod) {

    dexExprVisitor = new DexExprVisitor(registerAllocator, this);
    dexRefVisitor = new DexRefVisitor(this, registerAllocator);
    this.view = view;
    this.dexMethodBuilder = dexMethodBuilder;
    this.registerAllocator = registerAllocator;
    this.dexConstantVisitor = dexConstantVisitor;
    this.sootMethod = sootMethod;
  }

  @Override
  public void caseBreakpointStmt(@NonNull JBreakpointStmt stmt) {}

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    Optional<AbstractInvokeExpr> optionalExpr = stmt.getInvokeExpr();
    optionalExpr.ifPresent(
        abstractInvokeExpr -> {
          dexExprVisitor.setCurrentStmt(stmt);
          dexExprVisitor.setTargetRegister(null);
          abstractInvokeExpr.accept(dexExprVisitor);
        });
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
      Register register = registerAllocator.getRegisterForImmediate(rightOpImmediate, false);
      dexRefVisitor.setCurrentStmt(stmt);
      dexRefVisitor.setOperation("PUT");
      dexRefVisitor.setTargetRegister(register);
      ref.accept(dexRefVisitor);

    } else if (leftOp instanceof Local leftOpLocal) {
      Register targetRegister = registerAllocator.getRegisterForImmediate(leftOpLocal, false);

      if (rightOp instanceof Constant constant) {
        log.info("New constant");
        if (targetRegister.getType().toString().equals("java.lang.Object")
            || targetRegister.isTypeGuessed()) {
          if (targetRegister.getType() != constant.getType()
              && !targetRegister.getType().toString().equals("java.lang.Object")) {
            targetRegister =
                registerAllocator.getRegisterForValueWithNewType(
                    leftOpLocal, constant.getType(), false);
          } else {
            targetRegister.setType(constant.getType());
          }
          targetRegister.setIsTypeGuessed(true);
        }
        dexConstantVisitor.setTargetRegister(targetRegister);
        dexConstantVisitor.setCurrentStmt(stmt);
        constant.accept(dexConstantVisitor);

      } else if (rightOp instanceof Expr expr) {
        dexExprVisitor.setCurrentStmt(stmt);
        dexExprVisitor.setTargetRegister(targetRegister);
        dexExprVisitor.setTargetStmt(stmt);
        expr.accept(dexExprVisitor);

      } else if (rightOp instanceof Ref ref) {
        dexRefVisitor.setCurrentStmt(stmt);
        dexRefVisitor.setOperation("GET");
        dexRefVisitor.setTargetRegister(targetRegister);
        ref.accept(dexRefVisitor);

      } else if (rightOp instanceof Local sourceLocal) {
        if (leftOpLocal != sourceLocal) {
          Register sourceRegister = registerAllocator.getRegisterForImmediate(sourceLocal, false);
          dexExprVisitor.setCurrentStmt(stmt);
          log.info("regular move instruction");
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
    Immediate op1 = stmt.getLeftOp();
    IdentityRef op2 = stmt.getRightOp();
    if (op2 instanceof JCaughtExceptionRef) {
      Register register = registerAllocator.getRegisterForImmediate(stmt.getLeftOp(), false);
      dexRefVisitor.setCurrentStmt(stmt);
      dexRefVisitor.setTargetRegister(register);
    }
    dexRefVisitor.setImmediate(op1);
    op2.accept(dexRefVisitor);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    dexMethodBuilder.addInstruction(new Instruction11x(Opcode.MONITOR_ENTER, register), stmt);
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    dexMethodBuilder.addInstruction(new Instruction11x(Opcode.MONITOR_EXIT, register), stmt);
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    dexMethodBuilder.addInstruction(
        new Instruction10t(Opcode.GOTO, stmt.getTargetStmts(sootMethod.getBody()).get(0)), stmt);
    // goto
    // goto/16
    // goto/32
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    AbstractConditionExpr expr = stmt.getCondition();
    List<Stmt> targetStmts = stmt.getTargetStmts(sootMethod.getBody());
    if (!targetStmts.isEmpty()) {
      Stmt trueStmt = targetStmts.get(0);
      dexExprVisitor.setCurrentStmt(stmt);
      dexExprVisitor.setTargetStmt(trueStmt);
      expr.accept(dexExprVisitor);
    }
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    dexMethodBuilder.addInstruction(new Instruction10x(Opcode.NOP), stmt);
  }

  @Override
  public void caseRetStmt(@NonNull JRetStmt stmt) {
    throw new RuntimeException("JRetStmt should not occur in android bytecode");
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    String dexType = DexUtil.toDexType(op.getType());
    Opcode opcode;
    if (DexUtil.isObject(dexType)) {
      opcode = Opcode.RETURN_OBJECT;
    } else if (DexUtil.isWide(dexType)) {
      opcode = Opcode.RETURN_WIDE;
    } else {
      opcode = Opcode.RETURN;
    }
    dexMethodBuilder.addInstruction(new Instruction11x(opcode, register), stmt);
  }

  @Override
  public void caseReturnVoidStmt(@NonNull JReturnVoidStmt stmt) {
    dexMethodBuilder.addInstruction(new Instruction10x(Opcode.RETURN_VOID), stmt);
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
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    this.addInstruction(new Instruction11x(Opcode.THROW, register), stmt);
  }

  @Override
  public void defaultCaseStmt(Stmt stmt) {
    throw new RuntimeException("Unknown Statement " + stmt.getClass());
  }

  public DexExprVisitor getDexExprVisitor() {
    return dexExprVisitor;
  }

  protected SootMethod getSootMethod() {
    return sootMethod;
  }

  protected View getView() {
    return view;
  }

  protected void addInstruction(AbstractInstruction instruction, Stmt stmt) {
    dexMethodBuilder.addInstruction(instruction, stmt);
  }
}
