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
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
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

    if (leftOp instanceof Ref && rightOp instanceof Ref) {
      throw new RuntimeException("Both left-hand side and right-hand side of AssignStmt are ref");
    }

    if (leftOp instanceof Ref ref) {
      Register valueRegister;
      if (rightOp instanceof JCastExpr jCastExpr) {
        dexExprVisitor.setCurrentStmt(stmt);
        valueRegister = registerAllocator.getRegisterForType(jCastExpr.getType());
        dexExprVisitor.setTargetRegister(valueRegister);
        dexExprVisitor.setCurrentStmt(stmt);
        jCastExpr.accept(dexExprVisitor);
      } else if (rightOp instanceof Immediate) {
        valueRegister = registerAllocator.getRegisterForImmediate((Immediate) rightOp, false, stmt);
      } else {
        throw new RuntimeException(
            "Right-side of AssignStmt is no Immediate: "
                + rightOp
                + " with type "
                + rightOp.getType());
      }

      dexRefVisitor.setCurrentStmt(stmt);
      dexRefVisitor.setOperation("PUT");
      dexRefVisitor.setTargetRegister(valueRegister);
      ref.accept(dexRefVisitor);

    } else if (leftOp instanceof Local leftOpLocal) {
      Register targetRegister = registerAllocator.getRegisterForImmediate(leftOpLocal, false, stmt);

      if (rightOp instanceof Constant constant) {
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

          Register sourceRegister =
              registerAllocator.getRegisterForImmediate(sourceLocal, false, stmt);
          targetRegister = fixObjectType(sourceRegister.getType(), stmt, targetRegister);
          dexExprVisitor.setCurrentStmt(stmt);
          dexExprVisitor.generateMoveInstruction(
              targetRegister, sourceRegister, sourceRegister.getType(), true);
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
      Register register = registerAllocator.getRegisterForImmediate(stmt.getLeftOp(), false, stmt);
      dexRefVisitor.setCurrentStmt(stmt);
      dexRefVisitor.setTargetRegister(register);
    }
    dexRefVisitor.setImmediate(op1);
    op2.accept(dexRefVisitor);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false, stmt);
    dexMethodBuilder.addInstruction(new Instruction11x(Opcode.MONITOR_ENTER, register), stmt);
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false, stmt);
    dexMethodBuilder.addInstruction(new Instruction11x(Opcode.MONITOR_EXIT, register), stmt);
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    dexMethodBuilder.addInstruction(
        new Instruction10t(Opcode.GOTO, stmt.getTargetStmts(sootMethod.getBody()).get(0)), stmt);
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
    Register register = registerAllocator.getRegisterForImmediate(op, false, stmt);
    String dexType = DexUtil.toDexType(register.getType());
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

    Register register = registerAllocator.getRegisterForImmediate(stmt.getKey(), false, stmt);

    List<IntConstant> values = stmt.getValues();
    Opcode opcode = getSwitchOpcode(values);

    List<Stmt> targets = stmt.getTargetStmts(sootMethod.getBody()); // last entry is default
    Optional<Stmt> defaultTarget = stmt.getDefaultTarget(sootMethod.getBody());

    SwitchPayload switchPayload =
        new SwitchPayload(
            opcode,
            values.stream().map(IntConstant::getValue).mapToInt(Integer::intValue).toArray(),
            targets.toArray(Stmt[]::new));
    addSwitchPayload(switchPayload);

    Instruction31t instruction31t = new Instruction31t(opcode, register, switchPayload);
    this.addInstruction(instruction31t, stmt);

    defaultTarget.ifPresent(
        value -> dexMethodBuilder.addInstruction(new Instruction10t(Opcode.GOTO, value), stmt));
  }

  private Opcode getSwitchOpcode(List<IntConstant> values) {
    if (values.isEmpty()) {
      return Opcode.SPARSE_SWITCH;
    }

    ArrayList<IntConstant> valueList = new ArrayList<>(values);
    valueList.sort(Comparator.comparing(IntConstant::getValue));

    int packedSize =
        4 + (valueList.get(valueList.size() - 1).getValue() - valueList.get(0).getValue() + 1);
    int sparseSize = 2 + (valueList.size() * 2);
    return packedSize <= sparseSize ? Opcode.PACKED_SWITCH : Opcode.SPARSE_SWITCH;
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    Immediate op = stmt.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false, stmt);
    this.addInstruction(new Instruction11x(Opcode.THROW, register), stmt);
  }

  @Override
  public void defaultCaseStmt(Stmt stmt) {
    throw new RuntimeException("Unknown Statement " + stmt.getClass());
  }

  protected List<Stmt> newArrayInit(JAssignStmt newArrayStmt, List<Stmt> stmtsOfBlock) {
    this.caseAssignStmt(newArrayStmt);

    List<Number> arrayValues = new ArrayList<>();
    Set<Stmt> arrayInitStmts = new HashSet<>();
    int arraySize = -1;
    LinkedHashSet<Value> arrayRefs = new LinkedHashSet<>();
    arrayRefs.add(newArrayStmt.getLeftOp());

    // Get array size
    int index = stmtsOfBlock.indexOf(newArrayStmt);
    JNewArrayExpr newArrayExp = (JNewArrayExpr) newArrayStmt.getRightOp();
    if (newArrayExp.getSize() instanceof IntConstant intConstant) {
      // a = newarray (int)[4];
      arraySize = intConstant.getValue();
    } else if (newArrayExp.getSize() instanceof Local) {
      // a = (java.lang.Object) 4;
      // #l11 = (int) a;
      // a = newarray (int)[#l11];
      if (index > 1) {
        Stmt previousStmt = stmtsOfBlock.get(index - 1);
        Stmt secondToLastStmt = stmtsOfBlock.get(index - 2);
        if (previousStmt instanceof JAssignStmt jAssignStmt
            && jAssignStmt.getRightOp() instanceof JCastExpr
            && secondToLastStmt instanceof JAssignStmt jAssignStmt1) {
          if (jAssignStmt1.getRightOp() instanceof IntConstant intConstant) {
            arraySize = intConstant.getValue();
          } else if (jAssignStmt1.getRightOp() instanceof JCastExpr jCastExpr
              && jCastExpr.getOp() instanceof IntConstant intConstant) {
            arraySize = intConstant.getValue();
          }
        }
      }
    }

    if (arraySize < 0) {
      return stmtsOfBlock;
    }

    /*
    Considers array assignments in the form:
    #l0 = (int[]) a;
    #l0[0] = 10;
    #l16 = (int[]) a;
    #l16[1] = 20;
    #l12 = (int[]) a;
    #l12[2] = 30;
    #l11 = (int[]) a;
    #l11[3] = 40;
     */
    for (int i = index + 1; i < stmtsOfBlock.size(); i++) {
      Stmt currStmt = stmtsOfBlock.get(i);
      arrayInitStmts.add(currStmt);
      if (currStmt instanceof JAssignStmt jAssignStmt
          && jAssignStmt.getLeftOp() instanceof JArrayRef arrayRef
          && jAssignStmt.getRightOp() instanceof Constant) {
        Value rightOp = jAssignStmt.getRightOp();
        if (!arrayRefs.contains(arrayRef.getBase())) {
          break;
        }
        if (arrayRef.getIndex() instanceof IntConstant arrayIndex
            && arrayIndex.getValue() == arrayValues.size()) {
          if (rightOp instanceof IntConstant intConstant) {
            arrayValues.add(intConstant.getValue());
          } else if (rightOp instanceof LongConstant longConstant) {
            arrayValues.add(longConstant.getValue());
          } else if (rightOp instanceof FloatConstant floatConstant) {
            arrayValues.add(floatConstant.getValue());
          } else if (rightOp instanceof DoubleConstant doubleConstant) {
            arrayValues.add(doubleConstant.getValue());
          } else {
            break;
          }
          if (arrayIndex.getValue() == arraySize - 1) {
            if (arrayValues.size() == arraySize) {
              Value array = arrayRefs.stream().findFirst().orElse(null);
              Local arrayBase;
              if (array instanceof JArrayRef jArrayRef) {
                arrayBase = jArrayRef.getBase();
              } else if (array instanceof Local local) {
                arrayBase = local;
              } else {
                break;
              }
              Register arrayReg =
                  registerAllocator.getRegisterForImmediate(arrayBase, false, newArrayStmt);

              int elementSize = 0;
              if (arrayRef.getType() instanceof PrimitiveType.BooleanType
                  || arrayRef.getType() instanceof PrimitiveType.ByteType) {
                elementSize = 1;
              } else if (arrayRef.getType() instanceof PrimitiveType.CharType
                  || arrayRef.getType() instanceof PrimitiveType.ShortType) {
                elementSize = 2;
              } else if (arrayRef.getType() instanceof PrimitiveType.IntType
                  || arrayRef.getType() instanceof PrimitiveType.FloatType) {
                elementSize = 4;
              } else if (arrayRef.getType() instanceof PrimitiveType.LongType
                  || arrayRef.getType() instanceof PrimitiveType.DoubleType) {
                elementSize = 8;
              }

              ArrayPayload payload = new ArrayPayload(elementSize, arrayValues);
              dexMethodBuilder.addPayload(payload);
              dexMethodBuilder.addInstruction(
                  new Instruction31t(Opcode.FILL_ARRAY_DATA, arrayReg, payload), newArrayStmt);
              List<Stmt> updatedStmtsOfBlock = new ArrayList<>(stmtsOfBlock);
              updatedStmtsOfBlock.removeAll(arrayInitStmts);
              return updatedStmtsOfBlock;
            }
            break;
          }
          continue;
        }
        break;
      } else if (currStmt instanceof JAssignStmt jAssignStmt
          && jAssignStmt.getRightOp() instanceof JCastExpr jCastExpr) {
        if (arrayRefs.contains(jCastExpr.getOp())) {
          arrayRefs.add(jAssignStmt.getLeftOp());
          continue;
        }
      }
      break;
    }
    return stmtsOfBlock;
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

  protected void addSwitchPayload(SwitchPayload switchPayload) {
    dexMethodBuilder.addPayload(switchPayload);
  }

  private Register fixObjectType(Type defaultType, Stmt currentStmt, Register targetRegister) {
    if (targetRegister.getType().toString().equals("java.lang.Object")
        || targetRegister.isTypeGuessed()) {
      log.info("Set target register {} to type {}", targetRegister.getNumber(), defaultType);

      if (targetRegister.getType() != defaultType && currentStmt.isJAssignStmt()) {
        targetRegister =
            registerAllocator.getRegisterForValueWithNewType(
                currentStmt.asJAssignStmt().getLeftOp(), defaultType, false);
      } else {
        targetRegister.setType(defaultType);
      }
      log.info(
          "Set target register {} with type {} guessed true",
          targetRegister.getNumber(),
          targetRegister.getType());
      targetRegister.setIsTypeGuessed(true);
    }
    return targetRegister;
  }
}
