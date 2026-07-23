package sootup.apk.backend;

import java.util.*;
import java.util.stream.Collectors;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.AbstractExprVisitor;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;

public class DexExprVisitor extends AbstractExprVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexExprVisitor.class);

  private Register targetRegister;
  private Stmt currentStmt;
  private Stmt targetStmt;

  private final RegisterAllocator registerAllocator;
  private final DexStmtVisitor dexStmtVisitor;

  public DexExprVisitor(RegisterAllocator registerAllocator, DexStmtVisitor dexStmtVisitor) {
    this.registerAllocator = registerAllocator;
    this.dexStmtVisitor = dexStmtVisitor;
  }

  public void setTargetRegister(Register targetRegister) {
    this.targetRegister = targetRegister;
  }

  public void setCurrentStmt(Stmt currentStmt) {
    this.currentStmt = currentStmt;
  }

  public void setTargetStmt(Stmt targetStmt) {
    this.targetStmt = targetStmt;
  }

  @Override
  public void caseAddExpr(@NonNull JAddExpr expr) {
    generateBinopExpr(expr, "ADD");
  }

  @Override
  public void caseAndExpr(@NonNull JAndExpr expr) {
    generateBinopExpr(expr, "AND");
  }

  @Override
  public void caseCmpExpr(@NonNull JCmpExpr expr) {
    generateIntBinopExpr(expr, "CMP");
  }

  @Override
  public void caseCmpgExpr(@NonNull JCmpgExpr expr) {
    generateIntBinopExpr(expr, "CMPG");
  }

  @Override
  public void caseCmplExpr(@NonNull JCmplExpr expr) {
    generateIntBinopExpr(expr, "CMPL");
  }

  @Override
  public void caseDivExpr(@NonNull JDivExpr expr) {
    generateBinopExpr(expr, "DIV");
  }

  @Override
  public void caseEqExpr(@NonNull JEqExpr expr) {
    generateConditionExpr(expr, "EQ");
  }

  @Override
  public void caseNeExpr(@NonNull JNeExpr expr) {
    generateConditionExpr(expr, "NE");
  }

  @Override
  public void caseGeExpr(@NonNull JGeExpr expr) {
    generateConditionExpr(expr, "GE");
  }

  @Override
  public void caseGtExpr(@NonNull JGtExpr expr) {
    generateConditionExpr(expr, "GT");
  }

  @Override
  public void caseLeExpr(@NonNull JLeExpr expr) {
    generateConditionExpr(expr, "LE");
  }

  @Override
  public void caseLtExpr(@NonNull JLtExpr expr) {
    generateConditionExpr(expr, "LT");
  }

  @Override
  public void caseMulExpr(@NonNull JMulExpr expr) {
    generateBinopExpr(expr, "MUL");
  }

  @Override
  public void caseOrExpr(@NonNull JOrExpr expr) {
    generateBinopExpr(expr, "OR");
  }

  @Override
  public void caseRemExpr(@NonNull JRemExpr expr) {
    generateBinopExpr(expr, "REM");
  }

  @Override
  public void caseShlExpr(@NonNull JShlExpr expr) {
    generateBinopExpr(expr, "SHL");
  }

  @Override
  public void caseShrExpr(@NonNull JShrExpr expr) {
    generateBinopExpr(expr, "SHR");
  }

  @Override
  public void caseUshrExpr(@NonNull JUshrExpr expr) {
    generateBinopExpr(expr, "USHR");
  }

  @Override
  public void caseSubExpr(@NonNull JSubExpr expr) {
    generateBinopExpr(expr, "SUB");
  }

  @Override
  public void caseXorExpr(@NonNull JXorExpr expr) {
    if (expr.getOp2().equals(IntConstant.getInstance(-1))
        || expr.getOp2().equals(LongConstant.getInstance(-1))) {
      generateNotExpr(expr);
      return;
    }
    generateBinopExpr(expr, "XOR");
  }

  private void generateBinopExpr(AbstractBinopExpr expr, String operation) {

    // Get operands and ensure that they are primitive
    Immediate op1 = expr.getOp1();
    Immediate op2 = expr.getOp2();

    if (op1 instanceof NullConstant) {
      op1 = IntConstant.getInstance(0);
    }
    if (op2 instanceof NullConstant) {
      op2 = IntConstant.getInstance(0);
    }

    // use lit8/lit16 opcodes if the second operand is an int constant
    if (DexUtil.isTypeSmallerOrEqual(targetRegister.getType(), PrimitiveType.getInt())
        && op2 instanceof IntConstant
        && !(expr.isJSubExpr())) {
      int op2Constant = ((IntConstant) op2).getValue();
      if (DexUtil.inSigned8Bit(op2Constant)) {
        fixObjectType(PrimitiveType.getInt());
        Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);
        dexStmtVisitor.addInstruction(
            new Instruction22b(
                Opcode.valueOf(operation + "_INT_LIT8"), targetRegister, op1Register, op2Constant),
            currentStmt);
        return;
      } else if (DexUtil.inSigned16Bit(op2Constant)
          && !expr.isJShlExpr()
          && !expr.isJShrExpr()
          && !expr.isJUshrExpr()) {
        fixObjectType(PrimitiveType.getInt());
        Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);
        dexStmtVisitor.addInstruction(
            new Instruction22s(
                Opcode.valueOf(operation + "_INT_LIT16"), targetRegister, op1Register, op2Constant),
            currentStmt);
        return;
      }
    } else if (DexUtil.isTypeSmallerOrEqual(targetRegister.getType(), PrimitiveType.getInt())
        && op1 instanceof IntConstant
        && expr.isJSubExpr()) {
      int op1Constant = ((IntConstant) op1).getValue();
      if (DexUtil.inSigned8Bit(op1Constant)) {
        fixObjectType(PrimitiveType.getInt());
        Register op2Register = registerAllocator.getRegisterForImmediate(op2, false);
        dexStmtVisitor.addInstruction(
            new Instruction22b(Opcode.RSUB_INT_LIT8, targetRegister, op2Register, op1Constant),
            currentStmt);
        return;
      } else if (DexUtil.inSigned16Bit(op1Constant)) {
        fixObjectType(PrimitiveType.getInt());
        Register op2Register = registerAllocator.getRegisterForImmediate(op2, false);
        dexStmtVisitor.addInstruction(
            new Instruction22s(Opcode.RSUB_INT, targetRegister, op2Register, op1Constant),
            currentStmt);
        return;
      }
    }

    // Get registers
    // Use tmp registers if the operand registers and/or target are of different types
    Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);
    Register op2Register = registerAllocator.getRegisterForImmediate(op2, false);

    if (!(op1Register.getType() instanceof PrimitiveType)
        || !(op2Register.getType() instanceof PrimitiveType)) {
      throw new IllegalArgumentException(
          "Operands in BinopExpr are not primitive: "
              + op1Register.getType()
              + "/"
              + op2Register.getType());
    }

    PrimitiveType calcType =
        DexUtil.getArithmeticType(
            (PrimitiveType) op1Register.getType(), (PrimitiveType) op2Register.getType());

    fixObjectType(calcType);

    Register op1CastRegister;
    Register op2CastRegister;

    if (DexUtil.isTypeBiggerOrEqual(op1Register.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op1Register.getType(), calcType)) {
      op1CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op1Register, op1CastRegister, op1Register.getType(), calcType);
    } else {
      op1CastRegister = op1Register;
    }

    if (DexUtil.isTypeBiggerOrEqual(op2Register.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op2Register.getType(), calcType)
        && !List.of("SHL", "SHR", "USHR").contains(operation)) {
      op2CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op2Register, op2CastRegister, op2Register.getType(), calcType);
    } else if (List.of("SHL", "SHR", "USHR").contains(operation)
        && !DexUtil.isTypeSmallerOrEqual(op2Register.getType(), PrimitiveType.getInt())) {
      op2CastRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
      castPrimitive(op2Register, op2CastRegister, op2Register.getType(), PrimitiveType.getInt());
    } else {
      op2CastRegister = op2Register;
    }

    Register targetTmpRegister = getTargetTmpRegister(op1CastRegister.getType());

    // use 2addr opcode if one operand has the same register as the target
    // else use default opcode
    if (targetTmpRegister.equals(op1CastRegister)) {
      Opcode opcode =
          Opcode.valueOf(operation + "_" + calcType.toString().toUpperCase() + "_2ADDR");
      dexStmtVisitor.addInstruction(
          new Instruction12x(opcode, targetTmpRegister, op2CastRegister), currentStmt);
    } else if (targetTmpRegister.equals(op2CastRegister)) {
      Opcode opcode =
          Opcode.valueOf(operation + "_" + calcType.toString().toUpperCase() + "_2ADDR");
      dexStmtVisitor.addInstruction(
          new Instruction12x(opcode, targetTmpRegister, op1CastRegister), currentStmt);
    } else {
      Opcode opcode = Opcode.valueOf(operation + "_" + calcType.toString().toUpperCase());
      dexStmtVisitor.addInstruction(
          new Instruction23x(opcode, targetTmpRegister, op1CastRegister, op2CastRegister),
          currentStmt);
    }
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  private void generateNotExpr(JXorExpr jXorExpr) {
    Immediate op1 = jXorExpr.getOp1();
    Immediate op2 = jXorExpr.getOp2();

    Register targetTmpRegister = getTargetTmpRegister(op2.getType());
    Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);

    if (DexUtil.isTypeSmallerOrEqual(op1Register.getType(), PrimitiveType.getInt())) {
      fixObjectType(PrimitiveType.getInt());
      dexStmtVisitor.addInstruction(
          new Instruction12x(Opcode.NOT_INT, targetTmpRegister, op1Register), currentStmt);

    } else if (op1Register.getType() == PrimitiveType.getLong()) {
      fixObjectType(PrimitiveType.getLong());
      dexStmtVisitor.addInstruction(
          new Instruction12x(Opcode.NOT_LONG, targetTmpRegister, op1Register), currentStmt);

    } else {
      throw new IllegalArgumentException(
          "Operands of of not-expression are neither int nor long: " + op1Register.getType());
    }

    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  private void generateConditionExpr(
      AbstractConditionExpr abstractConditionExpr, String operation) {
    Immediate op1 = abstractConditionExpr.getOp1();
    Immediate op2 = abstractConditionExpr.getOp2();

    if (op1 instanceof NullConstant) {
      op1 = IntConstant.getInstance(0);
    }
    if (op2 instanceof NullConstant) {
      op2 = IntConstant.getInstance(0);
    }

    Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);
    if (op2 instanceof IntConstant && ((IntConstant) op2).getValue() == 0) {
      Opcode opcode = Opcode.valueOf("IF_" + operation + "Z");
      dexStmtVisitor.addInstruction(
          new Instruction21t(opcode, op1Register, targetStmt), currentStmt);
    } else {
      Opcode opcode = Opcode.valueOf("IF_" + operation);
      Register op2Register = registerAllocator.getRegisterForImmediate(op2, false);
      dexStmtVisitor.addInstruction(
          new Instruction22t(opcode, op1Register, op2Register, targetStmt), currentStmt);
    }
  }

  private void generateIntBinopExpr(AbstractIntBinopExpr abstractIntBinopExpr, String operation) {
    Immediate op1 = abstractIntBinopExpr.getOp1();
    Register op1Register = registerAllocator.getRegisterForImmediate(op1, false);
    Immediate op2 = abstractIntBinopExpr.getOp2();
    Register op2Register = registerAllocator.getRegisterForImmediate(op2, false);

    Register op1TmpRegister;
    Register op2TmpRegister;

    if (op1Register == targetRegister) {
      op1TmpRegister = registerAllocator.getRegisterForType(op1Register.getType());
      generateMoveInstruction(op1TmpRegister, op1Register, op1Register.getType());
    } else {
      op1TmpRegister = op1Register;
    }

    if (op2Register == targetRegister) {
      op2TmpRegister = registerAllocator.getRegisterForType(op2Register.getType());
      generateMoveInstruction(op2TmpRegister, op2Register, op2Register.getType());
    } else {
      op2TmpRegister = op2Register;
    }

    fixObjectType(PrimitiveType.getInt());

    if (!(op1TmpRegister.getType() instanceof PrimitiveType)
        || !(op2TmpRegister.getType() instanceof PrimitiveType)) {
      throw new IllegalArgumentException(
          "Operands in IntBinopExpr are not primitive: "
              + op1Register.getType()
              + "/"
              + op2Register.getType());
    }

    log.info("Op1 register {} of type {}", op1TmpRegister.getNumber(), op1TmpRegister.getType());
    log.info("Op2 register {} of type {}", op2TmpRegister.getNumber(), op2TmpRegister.getType());

    PrimitiveType calcType =
        DexUtil.getArithmeticType(
            (PrimitiveType) op1TmpRegister.getType(), (PrimitiveType) op2TmpRegister.getType());

    Register op1CastRegister;
    Register op2CastRegister;

    if (DexUtil.isTypeBiggerOrEqual(op1TmpRegister.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op1TmpRegister.getType(), calcType)) {
      op1CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op1TmpRegister, op1CastRegister, op1TmpRegister.getType(), calcType);
    } else {
      op1CastRegister = op1TmpRegister;
    }

    if (DexUtil.isTypeBiggerOrEqual(op2TmpRegister.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op2TmpRegister.getType(), calcType)
        && !List.of("SHL", "SHR", "USHR").contains(operation)) {
      op2CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op2TmpRegister, op2CastRegister, op2TmpRegister.getType(), calcType);
    } else if (List.of("SHL", "SHR", "USHR").contains(operation)
        && !DexUtil.isTypeSmallerOrEqual(op2TmpRegister.getType(), PrimitiveType.getInt())) {
      op2CastRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
      castPrimitive(
          op2TmpRegister, op2CastRegister, op2TmpRegister.getType(), PrimitiveType.getInt());
    } else {
      op2CastRegister = op2TmpRegister;
    }

    Opcode opcode;
    if (operation.equals("CMP")) {
      if (op1CastRegister.getType() == PrimitiveType.getLong()) {
        opcode = Opcode.CMP_LONG;
      } else {
        log.info("{}", op1Register.getNumber());
        throw new IllegalArgumentException(
            "Operands in CmpExpr are not long: "
                + op1CastRegister.getType()
                + "/"
                + op2CastRegister.getType());
      }
    } else if (op1CastRegister.getType() == PrimitiveType.getDouble()) {
      opcode = Opcode.valueOf(operation + "_DOUBLE");
    } else if (op1CastRegister.getType() == PrimitiveType.getFloat()) {
      opcode = Opcode.valueOf(operation + "_FLOAT");
    } else {
      throw new IllegalArgumentException(
          "Operands in cmpg/cmpl expression are neither double nor float: "
              + op1Register.getType()
              + "/"
              + op2Register.getType());
    }
    Register targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    dexStmtVisitor.addInstruction(
        new Instruction23x(opcode, targetTmpRegister, op1CastRegister, op2CastRegister),
        currentStmt);
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  @Override
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    ClassType targetClasesType = expr.getMethodSignature().getDeclClassType();
    ClassType currentClassType = dexStmtVisitor.getSootMethod().getDeclClassType();

    String opcode;
    if (targetClasesType.equals(currentClassType)
        || expr.getMethodSignature().getName().equals("<init>")) { // constructor or private method
      opcode = "INVOKE_DIRECT";
    } else if (isCallToSuperClass(currentClassType, targetClasesType)) {
      opcode = "INVOKE_SUPER";
    } else if (expr.getMethodSignature().getDeclClassType().getClass().isInterface()) {
      opcode = "INVOKE_SUPER";
    } else {
      opcode = "INVOKE_VIRTUAL";
    }

    List<Register> argumentRegisters = getVirtualInvokeArgumentRegisters(expr);
    MethodReference methodReference = buildMethodReference(expr.getMethodSignature());
    buildInvokeInstruction(
        opcode, argumentRegisters, methodReference, expr.getMethodSignature().getType());
  }

  private boolean isCallToSuperClass(ClassType currentClassType, ClassType targetClassType) {

    ClassType current = currentClassType;

    while (dexStmtVisitor.getView().getClass(current).isPresent()) {
      SootClass sc = dexStmtVisitor.getView().getClass(current).get();
      current = sc.getSuperclass().get();

      if (sc.getSuperclass().isEmpty()) {
        return false;
      }

      if ((current == targetClassType)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void caseVirtualInvokeExpr(@NonNull JVirtualInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    MethodReference methodReference = buildMethodReference(methodSignature);
    if (DexUtil.toDexClassName(methodSignature.getDeclClassType().getFullyQualifiedName())
        .equals("Ljava/lang/invoke/MethodHandle;")) {
      String methodName = methodSignature.getName();
      if (methodName.equals("invoke") || methodName.equals("invokeExact")) {
        buildMethodHandle(expr, methodSignature);
      }
      return;
    }
    String invokeOpcode = "INVOKE_VIRTUAL";
    List<Register> argumentRegisters = getVirtualInvokeArgumentRegisters(expr);
    buildInvokeInstruction(
        invokeOpcode, argumentRegisters, methodReference, expr.getMethodSignature().getType());
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    MethodReference methodReference = buildMethodReference(methodSignature);
    String invokeOpcode = "INVOKE_INTERFACE";
    List<Register> argumentRegisters = getVirtualInvokeArgumentRegisters(expr);
    buildInvokeInstruction(
        invokeOpcode, argumentRegisters, methodReference, expr.getMethodSignature().getType());
  }

  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    MethodReference methodReference = buildMethodReference(methodSignature);
    String invokeOpcode = "INVOKE_STATIC";
    List<Register> argumentRegisters = getInvokeArgumentRegisters(expr);
    buildInvokeInstruction(
        invokeOpcode, argumentRegisters, methodReference, expr.getMethodSignature().getType());
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {
    // TODO
  }

  private void buildInvokeInstruction(
      String opcode, List<Register> argumentRegisters, Reference reference, Type returnType) {
    int argumentRegisterSize = DexUtil.getRegisterSizeCount(argumentRegisters);
    if (argumentRegisterSize <= 5) {
      Opcode opc = Opcode.valueOf(opcode.toUpperCase());
      Register[] argRegisterList = getInvokeFiveRegisters(argumentRegisters);
      dexStmtVisitor.addInstruction(
          new Instruction35c(
              opc,
              argumentRegisterSize,
              argRegisterList[0],
              argRegisterList[1],
              argRegisterList[2],
              argRegisterList[3],
              argRegisterList[4],
              reference),
          currentStmt);
    } else if (argumentRegisterSize <= 255) {
      Opcode opc = Opcode.valueOf(opcode.toUpperCase() + "_RANGE");
      List<Register> argRegisterList = getRegisterNextToEachOther(argumentRegisters);
      dexStmtVisitor.addInstruction(
          new Instruction3rc(opc, argRegisterList, argumentRegisterSize, reference), currentStmt);
    } else {
      throw new RuntimeException(
          "Too many parameter registers for invoke instruction (> 255): " + argumentRegisterSize);
    }
    if (targetRegister != null) {
      buildMoveResult(returnType);
    }
  }

  private void buildMethodHandle(
      JVirtualInvokeExpr jVirtualInvokeExpr, MethodSignature methodSignature) {
    MethodReference methodReference = buildMethodReference(methodSignature);
    MethodProtoReference methodProtoReference =
        new ImmutableMethodProtoReference(
            methodSignature.getParameterTypes().stream().map(DexUtil::toDexType).toList(),
            DexUtil.toDexType(methodSignature.getType()));
    List<Register> argumentRegisters = getVirtualInvokeArgumentRegisters(jVirtualInvokeExpr);
    int argumentRegisterSize = DexUtil.getRegisterSizeCount(argumentRegisters);
    if (argumentRegisterSize <= 5) {
      Register[] argRegisterList = getInvokeFiveRegisters(argumentRegisters);
      Opcode opcode = Opcode.INVOKE_POLYMORPHIC;
      dexStmtVisitor.addInstruction(
          new Instruction45cc(
              opcode,
              argumentRegisters.size(),
              argRegisterList[0],
              argRegisterList[1],
              argRegisterList[2],
              argRegisterList[3],
              argRegisterList[4],
              methodReference,
              methodProtoReference),
          currentStmt);
    } else if (argumentRegisterSize <= 255) {
      Opcode opcode = Opcode.INVOKE_POLYMORPHIC_RANGE;
      List<Register> argRegisterList = getRegisterNextToEachOther(argumentRegisters);
      dexStmtVisitor.addInstruction(
          new Instruction4rcc(
              opcode, argRegisterList, argumentRegisterSize, methodReference, methodProtoReference),
          currentStmt);
    } else {
      throw new RuntimeException(
          "Too many parameter registers for invoke instruction (> 255): " + argumentRegisterSize);
    }
    if (targetRegister != null) {
      buildMoveResult(methodSignature.getType());
    }
  }

  private List<Register> getVirtualInvokeArgumentRegisters(
      AbstractInstanceInvokeExpr abstractInstanceInvokeExpr) {
    List<Register> argumentRegisters = getInvokeArgumentRegisters(abstractInstanceInvokeExpr);
    argumentRegisters.add(
        0, registerAllocator.getRegisterForImmediate(abstractInstanceInvokeExpr.getBase(), false));
    return argumentRegisters;
  }

  private List<Register> getInvokeArgumentRegisters(AbstractInvokeExpr abstractInvokeExpr) {
    return abstractInvokeExpr.getArgs().stream()
        .map(a -> registerAllocator.getRegisterForImmediate(a, false))
        .collect(Collectors.toList());
  }

  private MethodReference buildMethodReference(MethodSignature methodSignature) {
    String className =
        DexUtil.toDexClassName(methodSignature.getDeclClassType().getFullyQualifiedName());
    String methodName = methodSignature.getName();
    List<String> methodParameters =
        methodSignature.getParameterTypes().stream().map(DexUtil::toDexType).toList();
    String methodReturnType = DexUtil.toDexType(methodSignature.getType());
    return new ImmutableMethodReference(className, methodName, methodParameters, methodReturnType);
  }

  private Register[] getInvokeFiveRegisters(List<Register> registers) {
    Register[] packedRegisters = new Register[5];
    packedRegisters[0] = !registers.isEmpty() ? registers.get(0) : null;
    packedRegisters[1] = registers.size() > 1 ? registers.get(1) : null;
    packedRegisters[2] = registers.size() > 2 ? registers.get(2) : null;
    packedRegisters[3] = registers.size() > 3 ? registers.get(3) : null;
    packedRegisters[4] = registers.size() > 4 ? registers.get(4) : null;
    return packedRegisters;
  }

  private List<Register> getRegisterNextToEachOther(List<Register> registers) {
    if (areRegistersNextToEachOther(registers)) {
      return registers;
    } else {
      return registers.stream()
          .map(
              r -> {
                Register reg = registerAllocator.getRegisterForType(r.getType());
                generateMoveInstruction(reg, r, r.getType());
                return reg;
              })
          .toList();
    }
  }

  private boolean areRegistersNextToEachOther(List<Register> registers) {
    if (registers == null || registers.size() < 2) {
      return true;
    }
    int nextReg = registers.get(0).getNumber() + registers.get(0).getSize();
    for (int i = 0; i < registers.size(); i++) {
      Register currentReg = registers.get(i);
      if (i != 0) {
        if (currentReg.getNumber() != nextReg) {
          return false;
        }
        nextReg += currentReg.getSize();
      }
    }
    return true;
  }

  private void buildMoveResult(Type methodReturnType) {
    log.info("Method return type: {}", methodReturnType);
    fixObjectType(methodReturnType);
    if (targetRegister != null) {
      Opcode opcode;
      if (DexUtil.isObject(methodReturnType)) {
        opcode = Opcode.MOVE_RESULT_OBJECT;
      } else if (DexUtil.isWide(methodReturnType)) {
        opcode = Opcode.MOVE_RESULT_WIDE;
      } else {
        opcode = Opcode.MOVE_RESULT;
      }
      dexStmtVisitor.addInstruction(new Instruction11x(opcode, targetRegister), currentStmt);
    }
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    Immediate op = expr.getOp();
    Type type = expr.getType();
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    log.info("Source register {}{}", register.getNumber(), register.getType());
    log.info("Expression {}{}", expr.getOp(), expr.getType());

    if (register == targetRegister) {
      targetRegister = registerAllocator.getRegisterForValueWithNewType(op, type, false);
    }

    fixObjectType(type);

    if (register.getType().equals(type) || type.toString().equals("java.lang.Object")) {
      generateMoveInstruction(targetRegister, register, register.getType());
    } else if (register.getType() instanceof PrimitiveType && type instanceof PrimitiveType) {
      castPrimitive(register, targetRegister, register.getType(), type);
    } else {
      castObject(register, type);
    }
  }

  private void castObject(Register sourceRegister, Type castType) {
    String dexType = DexUtil.toDexType(castType);
    TypeReference castTypeReference = new ImmutableTypeReference(dexType);
    fixObjectType(castType);
    if (sourceRegister.equals(targetRegister)) {
      dexStmtVisitor.addInstruction(
          new Instruction21c(Opcode.CHECK_CAST, targetRegister, castTypeReference), currentStmt);
    } else {
      Register tmpRegister = registerAllocator.getRegisterForType(sourceRegister.getType());
      generateMoveInstruction(tmpRegister, sourceRegister, castType);
      dexStmtVisitor.addInstruction(
          new Instruction21c(Opcode.CHECK_CAST, tmpRegister, castTypeReference), currentStmt);
      generateMoveInstruction(targetRegister, tmpRegister, castType);
    }
  }

  private void castPrimitive(
      Register sourceRegister, Register targetR, Type sourceType, Type castType) {

    if (sourceType instanceof NullType) {
      sourceType = PrimitiveType.getInt();
    }
    if (castType instanceof NullType) {
      castType = PrimitiveType.getInt();
    }

    if (!(sourceType instanceof PrimitiveType sourceTypeP)) {
      throw new RuntimeException("Type is not primitive: " + sourceType);
    }
    if (!(castType instanceof PrimitiveType castTypeP)) {
      throw new RuntimeException("Type is not primitive " + castType);
    }

    if (DexUtil.isTypeSmaller(sourceTypeP, PrimitiveType.getInt())) {
      sourceTypeP = PrimitiveType.getInt();
    }
    if (castTypeP == PrimitiveType.getBoolean()) {
      castTypeP = PrimitiveType.getInt();
    }

    if (targetR.getType().toString().equals("java.lang.Object") || targetR.isTypeGuessed()) {
      targetRegister.setType(castType);
      targetRegister.setIsTypeGuessed(true);
    }

    if (sourceTypeP == castTypeP) {
      if (targetR.getNumber() != sourceRegister.getNumber()) {
        generateMoveInstruction(targetR, sourceRegister, sourceTypeP);
      } else {
        dexStmtVisitor.addInstruction(new Instruction10x(Opcode.NOP), currentStmt);
      }
    } else if (DexUtil.isTypeEqualOrBigger(sourceTypeP, PrimitiveType.getLong())
        && !DexUtil.isTypeEqualOrBigger(castTypeP, PrimitiveType.getInt())) {
      // Cast not supported (e.g. source >= long && cast < int)
      // //therefore split into tmp = (int) src and taret = (cast) tmp
      Register tmpRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
      Opcode opcode1 =
          Opcode.valueOf(
              sourceTypeP.getName().toUpperCase()
                  + "_TO_"
                  + PrimitiveType.getInt().getName().toUpperCase());
      dexStmtVisitor.addInstruction(
          new Instruction12x(opcode1, tmpRegister, sourceRegister), currentStmt);
      Opcode opcode2 =
          Opcode.valueOf(
              PrimitiveType.getInt().getName().toUpperCase()
                  + "_TO_"
                  + castTypeP.getName().toUpperCase());
      dexStmtVisitor.addInstruction(new Instruction12x(opcode2, targetR, tmpRegister), currentStmt);

    } else {
      // Usual cast
      Opcode opcode =
          Opcode.valueOf(
              sourceTypeP.getName().toUpperCase() + "_TO_" + castTypeP.getName().toUpperCase());
      dexStmtVisitor.addInstruction(
          new Instruction12x(opcode, targetR, sourceRegister), currentStmt);
    }
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    fixObjectType(PrimitiveType.getInt());
    Immediate op = expr.getOp();
    Register opRegister = registerAllocator.getRegisterForImmediate(op, false);
    Type type = expr.getCheckType();
    String dexType = DexUtil.toDexType(type);
    dexStmtVisitor.addInstruction(
        new Instruction22c(
            Opcode.INSTANCE_OF,
            this.targetRegister,
            opRegister,
            new ImmutableTypeReference(dexType)),
        currentStmt);
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    Immediate arraySize = expr.getSize();
    Register arraySizeRegister = registerAllocator.getRegisterForImmediate(arraySize, false);
    Type type = expr.getBaseType();
    int dimensions = 1;
    while (type instanceof ArrayType arrayType) {
      type = arrayType.getElementType();
      dimensions++;
    }
    ArrayType arrayType = ArrayType.createArrayType(type, dimensions);
    fixObjectType(arrayType);
    String dexType = DexUtil.toDexType(arrayType);
    dexStmtVisitor.addInstruction(
        new Instruction22c(
            Opcode.NEW_ARRAY,
            this.targetRegister,
            arraySizeRegister,
            new ImmutableTypeReference(dexType)),
        currentStmt);
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    int dimensions = expr.getSizeCount();
    if (dimensions > 255) {
      throw new RuntimeException(
          "Too many dimensions (> 255) in JNewMultiArrayExpr: " + dimensions);
    }

    ArrayType baseType = expr.getBaseType();
    fixObjectType(baseType);
    String dexType = DexUtil.toDexArrayType(dimensions, baseType);
    TypeReference arrayTypeReference = new ImmutableTypeReference(dexType);

    List<Register> sizeRegister = new ArrayList<>();
    for (int i = 0; i < dimensions; i++) {
      Immediate currentSize = expr.getSize(i);
      Register currentRegister = registerAllocator.getRegisterForImmediate(currentSize, false);
      sizeRegister.add(currentRegister);
    }
    buildInvokeInstruction("FILLED_NEW_ARRAY", sizeRegister, arrayTypeReference, baseType);
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    Type type = expr.getType();
    fixObjectType(type);
    String dexType = DexUtil.toDexType(type);
    TypeReference typeReference = new ImmutableTypeReference(dexType);
    dexStmtVisitor.addInstruction(
        new Instruction21c(Opcode.NEW_INSTANCE, this.targetRegister, typeReference), currentStmt);
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    Immediate array = expr.getOp();
    Register arrayRegister = registerAllocator.getRegisterForImmediate(array, false);
    fixObjectType(PrimitiveType.getInt());
    Register targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    dexStmtVisitor.addInstruction(
        new Instruction12x(Opcode.ARRAY_LENGTH, targetTmpRegister, arrayRegister), currentStmt);
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    Immediate op = expr.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op, false);
    Type type = op.getType();
    Opcode opcode;
    Register targetTmpRegister;
    if (DexUtil.isTypeSmallerOrEqual(type, PrimitiveType.getInt())) {
      opcode = Opcode.NEG_INT;
      fixObjectType(PrimitiveType.getInt());
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    } else if (type == PrimitiveType.getFloat()) {
      opcode = Opcode.NEG_FLOAT;
      fixObjectType(PrimitiveType.getFloat());
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getFloat());
    } else if (type == PrimitiveType.getDouble()) {
      opcode = Opcode.NEG_DOUBLE;
      fixObjectType(PrimitiveType.getDouble());
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getDouble());
    } else if (type == PrimitiveType.getLong()) {
      fixObjectType(PrimitiveType.getLong());
      opcode = Opcode.NEG_LONG;
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getLong());
    } else {
      throw new IllegalArgumentException("Unknown type in JNegExpr: " + type);
    }
    dexStmtVisitor.addInstruction(
        new Instruction12x(opcode, targetTmpRegister, register), currentStmt);
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  @Override
  public void casePhiExpr(@NonNull JPhiExpr v) {
    throw new IllegalArgumentException("Unknown Expr " + v.getClass());
  }

  @Override
  public void defaultCaseExpr(Expr expr) {
    throw new IllegalArgumentException("Unknown Expr " + expr.getClass());
  }

  protected void generateMoveInstruction(
      Register targetR, Register sourceRegister, Type valueType) {

    if (sourceRegister.getType() != targetR.getType() && targetR.isTypeGuessed()) {
      log.info(
          "Change type of target register from {} to {}",
          targetR.getType(),
          sourceRegister.getType());
      targetR.setType(sourceRegister.getType());
    }

    log.info("Target register {} with type {}", targetR.getNumber(), targetR.getType());

    if (valueType instanceof ReferenceType) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction12x(Opcode.MOVE_OBJECT, targetR, sourceRegister), currentStmt);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction22x(Opcode.MOVE_OBJECT_FROM16, targetR, sourceRegister), currentStmt);
      } else {
        dexStmtVisitor.addInstruction(
            new Instruction32x(Opcode.MOVE_OBJECT_16, targetR, sourceRegister), currentStmt);
      }
    } else if (DexUtil.isWide(valueType)) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction12x(Opcode.MOVE_WIDE, targetR, sourceRegister), currentStmt);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction22x(Opcode.MOVE_WIDE_FROM16, targetR, sourceRegister), currentStmt);
      } else {
        dexStmtVisitor.addInstruction(
            new Instruction32x(Opcode.MOVE_WIDE_16, targetR, sourceRegister), currentStmt);
      }
    } else {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction12x(Opcode.MOVE, targetR, sourceRegister), currentStmt);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        dexStmtVisitor.addInstruction(
            new Instruction22x(Opcode.MOVE_FROM16, targetR, sourceRegister), currentStmt);
      } else {
        dexStmtVisitor.addInstruction(
            new Instruction32x(Opcode.MOVE_16, targetR, sourceRegister), currentStmt);
      }
    }
  }

  private Register getTargetTmpRegister(Type opType) {
    // if the target register has a smaller type than the operands
    // a tmp register is used and the result later cast back into the target register
    if (opType.toString().equals("java.lang.Object")) {
      return targetRegister;
    }

    if (!(opType instanceof PrimitiveType)) {
      throw new IllegalArgumentException("Type is not primitive: " + opType);
    }
    if (!(targetRegister.getType() instanceof PrimitiveType targetRegisterType)) {
      throw new IllegalArgumentException(
          "TargetRegister type is not primitive: " + targetRegister.getType());
    }
    if (DexUtil.isTypeBigger(opType, targetRegisterType)) {
      return registerAllocator.getRegisterForType(opType);
    } else {
      return targetRegister;
    }
  }

  private void castTmpRegisterBackToTarget(Register targetTmpRegister) {
    if (!targetTmpRegister.equals(targetRegister)) {
      castPrimitive(
          targetTmpRegister, targetRegister, targetTmpRegister.getType(), targetRegister.getType());
    }
  }

  private void fixObjectType(Type defaultType) {
    log.info("Set target register {} to type {}", targetRegister.getNumber(), defaultType);
    if (targetRegister.getType().toString().equals("java.lang.Object")
        || targetRegister.isTypeGuessed()) {

      if (targetRegister.getType() != defaultType
          && (targetRegister.getType().toString().equals("java.lang.Object")
              || DexUtil.isWide(targetRegister.getType()) != DexUtil.isWide(defaultType))) {
        if (currentStmt.isJAssignStmt()) {
          targetRegister =
              registerAllocator.getRegisterForValueWithNewType(
                  currentStmt.asJAssignStmt().getLeftOp(), defaultType, false);
        } else {
          targetRegister.setType(defaultType);
        }
      } else {
        targetRegister.setType(defaultType);
      }
      targetRegister.setIsTypeGuessed(true);
    }
  }
}
