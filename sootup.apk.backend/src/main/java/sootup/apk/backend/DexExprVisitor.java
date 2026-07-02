package sootup.apk.backend;

import java.util.*;
import java.util.stream.Collectors;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.visitor.AbstractExprVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;

public class DexExprVisitor extends AbstractExprVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexExprVisitor.class);

  Register targetRegister;
  String targetLabel;

  DexBuilder dexBuilder;
  MethodImplementationBuilder methodImplementationBuilder;
  RegisterAllocator registerAllocator;
  DexStmtVisitor dexStmtVisitor;

  public DexExprVisitor(
      DexBuilder dexBuilder,
      MethodImplementationBuilder methodImplementationBuilder,
      RegisterAllocator registerAllocator,
      DexStmtVisitor dexStmtVisitor) {
    this.dexBuilder = dexBuilder;
    this.methodImplementationBuilder = methodImplementationBuilder;
    this.registerAllocator = registerAllocator;
    this.dexStmtVisitor = dexStmtVisitor;
  }

  public void setTargetRegister(Register targetRegister) {
    this.targetRegister = targetRegister;
  }

  public void setTargetLabel(String targetLabel) {
    this.targetLabel = targetLabel;
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

    if (!(op1.getType() instanceof PrimitiveType) || !(op2.getType() instanceof PrimitiveType)) {
      throw new IllegalArgumentException(
          "Operands in AbstractBinopExpr are not primitive: "
              + op1.getType()
              + "/"
              + op2.getType());
    }

    // use lit8/lit16 opcodes if the second operand is an int constant
    if (DexUtil.isTypeSmallerOrEqual(targetRegister.getType(), PrimitiveType.getInt())
        && op2 instanceof IntConstant
        && !(expr.isJSubExpr())) {
      int op2Constant = ((IntConstant) op2).getValue();
      if (DexUtil.inSigned8Bit(op2Constant)) {
        Register op1Register = registerAllocator.getRegisterForImmediate(op1);
        generateLit8Expr(Opcode.valueOf(operation + "_INT_LIT8"), op1Register, op2Constant);
        return;
      } else if (DexUtil.inSigned16Bit(op2Constant)
          && !expr.isJShlExpr()
          && !expr.isJShrExpr()
          && !expr.isJUshrExpr()) {
        Register op1Register = registerAllocator.getRegisterForImmediate(op1);
        generateLit16Expr(Opcode.valueOf(operation + "_INT_LIT16"), op1Register, op2Constant);
        return;
      }
    } else if (DexUtil.isTypeSmallerOrEqual(targetRegister.getType(), PrimitiveType.getInt())
        && op1 instanceof IntConstant
        && expr.isJSubExpr()) {
      int op1Constant = ((IntConstant) op1).getValue();
      if (DexUtil.inSigned8Bit(op1Constant)) {
        Register op2Register = registerAllocator.getRegisterForImmediate(op2);
        generateLit8Expr(Opcode.RSUB_INT_LIT8, op2Register, op1Constant);
        return;
      } else if (DexUtil.inSigned16Bit(op1Constant)) {
        Register op2Register = registerAllocator.getRegisterForImmediate(op2);
        generateLit16Expr(Opcode.RSUB_INT, op2Register, op1Constant);
        return;
      }
    }

    // Get registers
    // Use tmp registers if the operand registers and/or target are of different types
    Register op1Register = registerAllocator.getRegisterForImmediate(op1);
    Register op2Register = registerAllocator.getRegisterForImmediate(op2);

    PrimitiveType calcType =
        DexUtil.getArithmeticType((PrimitiveType) op1.getType(), (PrimitiveType) op2.getType());

    Register op1CastRegister;
    Register op2CastRegister;

    if (DexUtil.isTypeBigger(op1.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op1.getType(), calcType)) {
      op1CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op1Register, op1CastRegister, op1.getType(), calcType);
    } else {
      op1CastRegister = op1Register;
    }

    if (DexUtil.isTypeBigger(op2.getType(), PrimitiveType.getInt())
        && DexUtil.isTypeSmaller(op2.getType(), calcType)
        && !List.of("SHL", "SHR", "USHR").contains(operation)) {
      op2CastRegister = registerAllocator.getRegisterForType(calcType);
      castPrimitive(op2Register, op2CastRegister, op2.getType(), calcType);
    } else if (List.of("SHL", "SHR", "USHR").contains(operation)
        && !DexUtil.isTypeSmallerOrEqual(op2.getType(), PrimitiveType.getInt())) {
      op2CastRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
      castPrimitive(op2Register, op2CastRegister, op2.getType(), PrimitiveType.getInt());
    } else {
      op2CastRegister = op2Register;
    }

    Register targetTmpRegister = getTargetTmpRegister(op1CastRegister.getType());

    // use 2addr opcode if one operand has the same register as the target
    // else use default opcode
    if (targetTmpRegister.equals(op1CastRegister)) {
      generate2AddrExpr(operation, calcType, targetTmpRegister, op2CastRegister);
    } else if (targetTmpRegister.equals(op2CastRegister)) {
      generate2AddrExpr(operation, calcType, targetTmpRegister, op1CastRegister);
    } else {
      Opcode opcode = Opcode.valueOf(operation + "_" + calcType.toString().toUpperCase());
      log.info(
          "{}-{} v{}, v{}, v{}",
          operation.toLowerCase(),
          calcType.toString().toLowerCase(),
          targetTmpRegister.getNumber(),
          op1CastRegister.getNumber(),
          op2CastRegister.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction23x(
              opcode,
              targetTmpRegister.getNumber(),
              op1CastRegister.getNumber(),
              op2CastRegister.getNumber()));
    }
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  private void generateNotExpr(JXorExpr jXorExpr) {
    Immediate op1 = jXorExpr.getOp1();
    Immediate op2 = jXorExpr.getOp2();

    Register targetTmpRegister = getTargetTmpRegister(op2.getType());
    Register op1Register = registerAllocator.getRegisterForImmediate(op1);

    if (DexUtil.isTypeSmallerOrEqual(op1.getType(), PrimitiveType.getInt())) {
      log.info("not-int v{}, v{}", targetTmpRegister.getNumber(), op1Register.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction12x(
              Opcode.NOT_INT, targetTmpRegister.getNumber(), op1Register.getNumber()));

    } else if (op1.getType() == PrimitiveType.getLong()) {
      log.info("not-long v{}, v{}", targetTmpRegister.getNumber(), op1Register.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction12x(
              Opcode.NOT_LONG, targetTmpRegister.getNumber(), op1Register.getNumber()));

    } else {
      throw new IllegalArgumentException(
          "Operands of of not-expression are neither int nor long: " + op1.getType());
    }

    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  private void generateLit8Expr(Opcode opcode, Register op1Register, int op2Const) {
    log.info(
        "{} v{}, v{}, {}",
        opcode.toString().toLowerCase().replace("_", "-"),
        targetRegister.getNumber(),
        op1Register.getNumber(),
        op2Const);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction22b(
            opcode, targetRegister.getNumber(), op1Register.getNumber(), op2Const));
  }

  private void generateLit16Expr(Opcode opcode, Register op1Register, int op2Const) {
    log.info(
        "{} v{}, v{}, {}",
        opcode.toString().toLowerCase().replace("_", "-"),
        targetRegister.getNumber(),
        op1Register.getNumber(),
        op2Const);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction22s(
            opcode, targetRegister.getNumber(), op1Register.getNumber(), op2Const));
  }

  private void generate2AddrExpr(
      String operation, Type calcType, Register register, Register opRegister) {
    Opcode opcode = Opcode.valueOf(operation + "_" + calcType.toString().toUpperCase() + "_2ADDR");
    log.info(
        "{} v{}, v{}",
        opcode.toString().toLowerCase().replace("_", "-"),
        register.getNumber(),
        opRegister.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction12x(opcode, register.getNumber(), opRegister.getNumber()));
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

    Register op1Register = registerAllocator.getRegisterForImmediate(op1);
    if (op2 instanceof IntConstant && ((IntConstant) op2).getValue() == 0) {
      Opcode opcode = Opcode.valueOf("IF_" + operation + "Z");
      Label target = methodImplementationBuilder.getLabel(targetLabel);
      log.info(
          "{}-{} v{}, :{}",
          operation.toLowerCase(),
          opcode.name.toLowerCase(),
          op1Register.getNumber(),
          target);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21t(opcode, op1Register.getNumber(), target));
    } else {
      Opcode opcode = Opcode.valueOf("IF_" + operation);
      Register op2Register = registerAllocator.getRegisterForImmediate(op2);
      Label target = methodImplementationBuilder.getLabel(targetLabel);
      log.info(
          "{} v{}, v{} :{}",
          opcode.name.toLowerCase(),
          op1Register.getNumber(),
          op2Register.getNumber(),
          target);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction22t(
              opcode, op1Register.getNumber(), op2Register.getNumber(), target));
    }
  }

  private void generateIntBinopExpr(AbstractIntBinopExpr abstractIntBinopExpr, String operation) {
    Immediate op1 = abstractIntBinopExpr.getOp1();
    Register op1Register = registerAllocator.getRegisterForImmediate(op1);
    Immediate op2 = abstractIntBinopExpr.getOp2();
    Register op2Register = registerAllocator.getRegisterForImmediate(op2);

    Opcode opcode;
    if (operation.equals("CMP")) {
      if (op1.getType() == PrimitiveType.getLong() && op2.getType() == PrimitiveType.getLong()) {
        opcode = Opcode.CMP_LONG;
      } else {
        throw new IllegalArgumentException("Operands in CmpExpr are not long: " + op1.getType());
      }
    } else if (op1.getType() == PrimitiveType.getDouble()
        && op2.getType() == PrimitiveType.getDouble()) {
      opcode = Opcode.valueOf(operation + "_DOUBLE");
    } else if (op1.getType() == PrimitiveType.getFloat()
        && op2.getType() == PrimitiveType.getFloat()) {
      opcode = Opcode.valueOf(operation + "_FLOAT");
    } else {
      throw new IllegalArgumentException(
          "Operands in cmpg/cmpl expression are neither double nor float: "
              + op1.getType()
              + "/"
              + op2.getType());
    }
    Register targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    log.info(
        "{} v{}, v{}, v{}",
        opcode.name.toLowerCase(),
        targetRegister.getNumber(),
        op1Register.getNumber(),
        op2Register.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction23x(
            opcode,
            targetTmpRegister.getNumber(),
            op1Register.getNumber(),
            op2Register.getNumber()));
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  @Override
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    // TODO how to distinguish between invoke-direct and invoke-super?
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
        invokeOpcode, argumentRegisters, methodReference, methodReference.getReturnType());
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    MethodReference methodReference = buildMethodReference(methodSignature);
    String invokeOpcode = "INVOKE_INTERFACE";
    List<Register> argumentRegisters = getVirtualInvokeArgumentRegisters(expr);
    buildInvokeInstruction(
        invokeOpcode, argumentRegisters, methodReference, methodReference.getReturnType());
  }

  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {
    MethodSignature methodSignature = expr.getMethodSignature();
    MethodReference methodReference = buildMethodReference(methodSignature);
    String invokeOpcode = "INVOKE_STATIC";
    List<Register> argumentRegisters = getInvokeArgumentRegisters(expr);
    buildInvokeInstruction(
        invokeOpcode, argumentRegisters, methodReference, methodReference.getReturnType());
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {
    // TODO
  }

  private void buildInvokeInstruction(
      String opcode, List<Register> argumentRegisters, Reference reference, String returnType) {
    int argumentRegisterSize = DexUtil.getRegisterSizeCount(argumentRegisters);
    if (argumentRegisterSize <= 5) {
      Opcode opc = Opcode.valueOf(opcode.toUpperCase());
      logInvoke(opc.toString().toLowerCase() + "/range", argumentRegisters, reference, returnType);
      int[] argRegisterList = getInvokeFiveRegisters(argumentRegisters);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction35c(
              opc,
              argumentRegisters.size(),
              argRegisterList[0],
              argRegisterList[1],
              argRegisterList[2],
              argRegisterList[3],
              argRegisterList[4],
              reference));
    } else if (argumentRegisterSize <= 255) {
      Opcode opc = Opcode.valueOf(opcode.toUpperCase() + "_RANGE");
      List<Register> argRegisterList = getRegisterNextToEachOther(argumentRegisters);
      logInvoke(opc.toString().toLowerCase() + "/range", argumentRegisters, reference, returnType);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction3rc(
              opc, argRegisterList.get(0).getNumber(), argumentRegisterSize, reference));
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
      int[] argRegisterList = getInvokeFiveRegisters(argumentRegisters);
      Opcode opcode = Opcode.INVOKE_POLYMORPHIC;
      dexStmtVisitor.addInstruction(
          new BuilderInstruction45cc(
              opcode,
              argumentRegisters.size(),
              argRegisterList[0],
              argRegisterList[1],
              argRegisterList[2],
              argRegisterList[3],
              argRegisterList[4],
              methodReference,
              methodProtoReference));
    } else if (argumentRegisterSize <= 255) {
      Opcode opcode = Opcode.INVOKE_POLYMORPHIC_RANGE;
      List<Register> argRegisterList = getRegisterNextToEachOther(argumentRegisters);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction4rcc(
              opcode,
              argRegisterList.get(0).getNumber(),
              argumentRegisterSize,
              methodReference,
              methodProtoReference));
    } else {
      throw new RuntimeException(
          "Too many parameter registers for invoke instruction (> 255): " + argumentRegisterSize);
    }
    logInvoke(
        "invoke-polymorphic", argumentRegisters, methodReference, methodReference.getReturnType());
    if (targetRegister != null) {
      buildMoveResult(methodReference.getReturnType());
    }
  }

  private List<Register> getVirtualInvokeArgumentRegisters(
      AbstractInstanceInvokeExpr abstractInstanceInvokeExpr) {
    List<Register> argumentRegisters = getInvokeArgumentRegisters(abstractInstanceInvokeExpr);
    argumentRegisters.add(
        0, registerAllocator.getRegisterForImmediate(abstractInstanceInvokeExpr.getBase()));
    return argumentRegisters;
  }

  private List<Register> getInvokeArgumentRegisters(AbstractInvokeExpr abstractInvokeExpr) {
    return abstractInvokeExpr.getArgs().stream()
        .map(registerAllocator::getRegisterForImmediate)
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

  private int[] getInvokeFiveRegisters(List<Register> registers) {
    int[] packedRegisters = new int[5];
    packedRegisters[0] = !registers.isEmpty() ? registers.get(0).getNumber() : 0;
    packedRegisters[1] = registers.size() > 1 ? registers.get(1).getNumber() : 0;
    packedRegisters[2] = registers.size() > 2 ? registers.get(2).getNumber() : 0;
    packedRegisters[3] = registers.size() > 3 ? registers.get(3).getNumber() : 0;
    packedRegisters[4] = registers.size() > 4 ? registers.get(4).getNumber() : 0;
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

  private void logInvoke(
      String opcode, List<Register> argumentRegisters, Reference reference, String returnType) {
    int argumentRegisterSize = DexUtil.getRegisterSizeCount(argumentRegisters);
    log.info(
        String.format(
            "%s {%s}, %s->%s",
            opcode.toLowerCase().replace("_", "-"),
            argumentRegisterSize <= 5
                ? String.join(
                    ", ", argumentRegisters.stream().map(r -> "v" + r.getNumber()).toList())
                : "v"
                    + argumentRegisters.get(0).getNumber()
                    + " .. "
                    + "v"
                    + (argumentRegisters.get(0).getNumber() + argumentRegisterSize - 1),
            reference instanceof MethodReference methodReference
                ? methodReference.getDefiningClass()
                : returnType,
            reference instanceof MethodReference methodReference
                ? methodReference.getName()
                    + "("
                    + String.join("", methodReference.getParameterTypes())
                    + ")"
                    + methodReference.getReturnType()
                : returnType));
  }

  private void buildMoveResult(String methodReturnType) {
    if (targetRegister != null) {
      Opcode opcode;
      if (DexUtil.isObject(methodReturnType)) {
        opcode = Opcode.MOVE_RESULT_OBJECT;
      } else if (DexUtil.isWide(methodReturnType)) {
        opcode = Opcode.MOVE_RESULT_WIDE;
      } else {
        opcode = Opcode.MOVE_RESULT;
      }
      log.info("{} v{}", opcode.toString().toLowerCase(), targetRegister.getNumber());
      dexStmtVisitor.addInstruction(new BuilderInstruction11x(opcode, targetRegister.getNumber()));
    }
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    Immediate op = expr.getOp();
    Type type = expr.getType();
    Register register = registerAllocator.getRegisterForImmediate(op);
    if (type instanceof ReferenceType) {
      castObject(register, type);
    } else {
      castPrimitive(register, targetRegister, op.getType(), type);
    }
  }

  private void castObject(Register sourceRegister, Type castType) {
    String dexType = DexUtil.toDexType(castType);
    TypeReference castTypeReference = new ImmutableTypeReference(dexType);
    if (sourceRegister.equals(targetRegister)) {
      log.info("check-cast v{}, {}", targetRegister.getNumber(), castTypeReference.getType());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21c(
              Opcode.CHECK_CAST, targetRegister.getNumber(), castTypeReference));
    } else {
      Register tmpRegister = registerAllocator.getRegisterForType(sourceRegister.getType());
      generateMoveInstruction(tmpRegister, sourceRegister, castType);
      log.info("check-cast v{}, {}", tmpRegister.getNumber(), castTypeReference.getType());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21c(Opcode.CHECK_CAST, tmpRegister.getNumber(), castTypeReference));
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
      throw new RuntimeException("Type is not primitive" + castType);
    }

    if (DexUtil.isTypeSmaller(sourceTypeP, PrimitiveType.getInt())) {
      sourceTypeP = PrimitiveType.getInt();
    }
    if (castTypeP == PrimitiveType.getBoolean()) {
      castTypeP = PrimitiveType.getInt();
    }

    if (sourceTypeP == castTypeP) {
      if (targetR.getNumber() != sourceRegister.getNumber()) {
        generateMoveInstruction(targetR, sourceRegister, sourceTypeP);
      } else {
        log.info("nop");
        dexStmtVisitor.addInstruction(new BuilderInstruction10x(Opcode.NOP));
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
      log.info(
          "{} v{}, v{}",
          opcode1.toString().toLowerCase(),
          tmpRegister.getNumber(),
          sourceRegister.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction12x(opcode1, tmpRegister.getNumber(), sourceRegister.getNumber()));
      Opcode opcode2 =
          Opcode.valueOf(
              PrimitiveType.getInt().getName().toUpperCase()
                  + "_TO_"
                  + castTypeP.getName().toUpperCase());
      log.info(
          "{} v{}, v{}",
          opcode2.toString().toLowerCase(),
          targetR.getNumber(),
          tmpRegister.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction12x(opcode2, targetR.getNumber(), tmpRegister.getNumber()));

    } else {
      // Usual cast
      Opcode opcode =
          Opcode.valueOf(
              sourceTypeP.getName().toUpperCase() + "_TO_" + castTypeP.getName().toUpperCase());
      log.info(
          "{} v{}, v{}",
          opcode.toString().toLowerCase(),
          targetR.getNumber(),
          sourceRegister.getNumber());
      dexStmtVisitor.addInstruction(
          new BuilderInstruction12x(opcode, targetR.getNumber(), sourceRegister.getNumber()));
    }
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    Immediate op = expr.getOp();
    Register opRegister = registerAllocator.getRegisterForImmediate(op);
    Type type = expr.getCheckType();
    String dexType = DexUtil.toDexType(type);
    log.info(
        "instance-of v{}, v{}, {}", targetRegister.getNumber(), opRegister.getNumber(), dexType);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction22c(
            Opcode.INSTANCE_OF,
            this.targetRegister.getNumber(),
            opRegister.getNumber(),
            new ImmutableTypeReference(dexType)));
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    Immediate arraySize = expr.getSize();
    Register arraySizeRegister = registerAllocator.getRegisterForImmediate(arraySize);
    Type type = expr.getBaseType();
    int dimensions = 1;
    while (type instanceof ArrayType arrayType) {
      type = arrayType.getElementType();
      dimensions++;
    }
    ArrayType arrayType = ArrayType.createArrayType(type, dimensions);
    String dexType = DexUtil.toDexType(arrayType);
    log.info(
        "new-array v{}, v{}, {}",
        targetRegister.getNumber(),
        arraySizeRegister.getNumber(),
        dexType);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction22c(
            Opcode.NEW_ARRAY,
            this.targetRegister.getNumber(),
            arraySizeRegister.getNumber(),
            new ImmutableTypeReference(dexType)));
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {
    int dimensions = expr.getSizeCount();
    if (dimensions > 255) {
      throw new RuntimeException(
          "Too many dimensions (> 255) in JNewMultiArrayExpr: " + dimensions);
    }

    ArrayType baseType = expr.getBaseType();
    String dexType = DexUtil.toDexArrayType(dimensions, baseType);
    TypeReference arrayTypeReference = new ImmutableTypeReference(dexType);

    List<Register> sizeRegister = new ArrayList<>();
    for (int i = 0; i < dimensions; i++) {
      Immediate currentSize = expr.getSize(i);
      Register currentRegister = registerAllocator.getRegisterForImmediate(currentSize);
      sizeRegister.add(currentRegister);
    }
    buildInvokeInstruction("FILLED_NEW_ARRAY", sizeRegister, arrayTypeReference, dexType);
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    Type type = expr.getType();
    String dexType = DexUtil.toDexType(type);
    TypeReference typeReference = new ImmutableTypeReference(dexType);
    log.info("new-instance v{}, {}", targetRegister.getNumber(), typeReference.getType());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(
            Opcode.NEW_INSTANCE, this.targetRegister.getNumber(), typeReference));
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    Immediate array = expr.getOp();
    Register arrayRegister = registerAllocator.getRegisterForImmediate(array);
    Register targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    log.info("array-length v{}, v{}", this.targetRegister.getNumber(), arrayRegister.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction12x(
            Opcode.ARRAY_LENGTH, targetTmpRegister.getNumber(), arrayRegister.getNumber()));
    castTmpRegisterBackToTarget(targetTmpRegister);
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    Immediate op = expr.getOp();
    Register register = registerAllocator.getRegisterForImmediate(op);
    Type type = op.getType();
    Opcode opcode;
    Register targetTmpRegister;
    if (DexUtil.isTypeSmallerOrEqual(type, PrimitiveType.getInt())) {
      opcode = Opcode.NEG_INT;
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getInt());
    } else if (type == PrimitiveType.getFloat()) {
      opcode = Opcode.NEG_FLOAT;
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getFloat());
    } else if (type == PrimitiveType.getDouble()) {
      opcode = Opcode.NEG_DOUBLE;
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getDouble());
    } else if (type == PrimitiveType.getLong()) {
      opcode = Opcode.NEG_LONG;
      targetTmpRegister = getTargetTmpRegister(PrimitiveType.getLong());
    } else {
      throw new IllegalArgumentException("Unknown type in JNegExpr: " + type);
    }
    log.info(
        "{} v{}, v{}", opcode.name.toLowerCase(), targetRegister.getNumber(), register.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction12x(opcode, targetTmpRegister.getNumber(), register.getNumber()));
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
    if (valueType instanceof ReferenceType) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        log.info("move-object v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction12x(
                Opcode.MOVE_OBJECT, targetR.getNumber(), sourceRegister.getNumber()));
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        log.info("move-object/from16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction22x(
                Opcode.MOVE_OBJECT_FROM16, targetR.getNumber(), sourceRegister.getNumber()));
      } else {
        log.info("move-object/16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction32x(
                Opcode.MOVE_OBJECT_16, targetR.getNumber(), sourceRegister.getNumber()));
      }
    } else if (DexUtil.isWide(valueType)) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        log.info("move-wide v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction12x(
                Opcode.MOVE_WIDE, targetR.getNumber(), sourceRegister.getNumber()));
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        log.info("move-wide/from16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction22x(
                Opcode.MOVE_WIDE_FROM16, targetR.getNumber(), sourceRegister.getNumber()));
      } else {
        log.info("move-wide/16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction32x(
                Opcode.MOVE_WIDE_16, targetR.getNumber(), sourceRegister.getNumber()));
      }
    } else {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        log.info("move v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction12x(
                Opcode.MOVE, targetR.getNumber(), sourceRegister.getNumber()));
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        log.info("move/from16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction22x(
                Opcode.MOVE_FROM16, targetR.getNumber(), sourceRegister.getNumber()));
      } else {
        log.info("move/16 v{}, v{}", targetR.getNumber(), sourceRegister.getNumber());
        dexStmtVisitor.addInstruction(
            new BuilderInstruction32x(
                Opcode.MOVE_16, targetR.getNumber(), sourceRegister.getNumber()));
      }
    }
  }

  private Register getTargetTmpRegister(Type opType) {
    // if the target register has a smaller type than the operands
    // a tmp register is used and the result later cast back into the target register
    if (!(opType instanceof PrimitiveType)) {
      throw new IllegalArgumentException("Type is not primitive: " + opType);
    }
    PrimitiveType targetRegisterType = (PrimitiveType) targetRegister.getType();
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
}
