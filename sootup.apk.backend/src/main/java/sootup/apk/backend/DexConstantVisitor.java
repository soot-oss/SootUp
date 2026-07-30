package sootup.apk.backend;

import java.util.List;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.AbstractConstantVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.java.core.JavaIdentifierFactory;

public class DexConstantVisitor extends AbstractConstantVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexConstantVisitor.class);

  private Stmt currentStmt;
  private Register targetRegister;

  private final DexMethodBuilder dexMethodBuilder;

  public DexConstantVisitor(DexMethodBuilder dexMethodBuilder) {
    this.dexMethodBuilder = dexMethodBuilder;
  }

  public void setCurrentStmt(Stmt stmt) {
    this.currentStmt = stmt;
  }

  public void setTargetRegister(Register targetRegister) {
    this.targetRegister = targetRegister;
  }

  @Override
  public void caseBooleanConstant(@NonNull BooleanConstant constant) {
    fixObjectType(PrimitiveType.getBoolean());
    int value = constant.getValue() ? 1 : 0;
    dexMethodBuilder.addInstruction(
        new Instruction11n(Opcode.CONST_4, targetRegister, value), currentStmt);
  }

  @Override
  public void caseDoubleConstant(@NonNull DoubleConstant constant) {
    fixObjectType(PrimitiveType.getDouble());
    double value = constant.getValue();
    long bits = Double.doubleToLongBits(value);
    dexMethodBuilder.addInstruction(
        new Instruction51l(Opcode.CONST_WIDE, targetRegister, bits), currentStmt);
  }

  @Override
  public void caseFloatConstant(@NonNull FloatConstant constant) {
    fixObjectType(PrimitiveType.getFloat());
    float value = constant.getValue();
    int bits = Float.floatToIntBits(value);
    dexMethodBuilder.addInstruction(
        new Instruction31i(Opcode.CONST, targetRegister, bits), currentStmt);
  }

  @Override
  public void caseIntConstant(@NonNull IntConstant constant) {
    fixObjectType(PrimitiveType.getInt());
    int value = constant.getValue();
    if (DexUtil.inSigned4Bit(value)) {
      dexMethodBuilder.addInstruction(
          new Instruction11n(Opcode.CONST_4, targetRegister, value), currentStmt);
    } else if (DexUtil.inSigned16Bit(value)) {
      dexMethodBuilder.addInstruction(
          new Instruction21s(Opcode.CONST_16, targetRegister, value), currentStmt);
    } else {
      dexMethodBuilder.addInstruction(
          new Instruction31i(Opcode.CONST, targetRegister, value), currentStmt);
    }
  }

  @Override
  public void caseLongConstant(@NonNull LongConstant constant) {
    fixObjectType(PrimitiveType.getLong());
    log.info("Generate long constant {}", constant.getValue());
    log.info("TargetRegister type {}", targetRegister.getType());
    long value = constant.getValue();
    if (DexUtil.inSigned16Bit(value)) {
      dexMethodBuilder.addInstruction(
          new Instruction21s(Opcode.CONST_WIDE_16, targetRegister, (int) value), currentStmt);
    } else if (DexUtil.inSigned32Bit(value)) {
      dexMethodBuilder.addInstruction(
          new Instruction31i(Opcode.CONST_WIDE_32, targetRegister, (int) value), currentStmt);
    } else {
      dexMethodBuilder.addInstruction(
          new Instruction51l(Opcode.CONST_WIDE, targetRegister, value), currentStmt);
    }
  }

  @Override
  public void caseNullConstant(@NonNull NullConstant constant) {
    fixObjectType(NullType.getInstance());
    dexMethodBuilder.addInstruction(
        new Instruction21s(Opcode.CONST_16, targetRegister, 0), currentStmt);
  }

  @Override
  public void caseStringConstant(@NonNull StringConstant constant) {
    fixObjectType(JavaIdentifierFactory.getInstance().getClassType("java.lang.String"));
    dexMethodBuilder.addInstruction(
        new Instruction21c(
            Opcode.CONST_STRING, targetRegister, new ImmutableStringReference(constant.getValue())),
        currentStmt);
    new ImmutableStringReference(constant.getValue());
    // const-string/jumbo --> if application has > 65,535 strings total
  }

  @Override
  public void caseEnumConstant(@NonNull EnumConstant constant) {
    ClassType type = (ClassType) constant.getType();
    String name = DexUtil.toDexClassName(type.getFullyQualifiedName());
    String value = constant.getValue();
    FieldReference enumRef = new ImmutableFieldReference(name, value, name);
    dexMethodBuilder.addInstruction(
        new Instruction21c(Opcode.SGET_OBJECT, targetRegister, enumRef), currentStmt);
  }

  @Override
  public void caseClassConstant(@NonNull ClassConstant constant) {
    fixObjectType(JavaIdentifierFactory.getInstance().getClassType(constant.getValue()));
    TypeReference referencedClass =
        new ImmutableTypeReference(DexUtil.toDexClassName(constant.getValue()));
    dexMethodBuilder.addInstruction(
        new Instruction21c(Opcode.CONST_CLASS, targetRegister, referencedClass), currentStmt);
  }

  @Override
  public void caseMethodHandle(@NonNull MethodHandle handle) {
    if (handle.getReferenceSignature() instanceof MethodSignature signature) {
      int methodHandleType =
          switch (handle.getKind()) {
            case REF_INVOKE_CONSTRUCTOR -> MethodHandleType.INVOKE_CONSTRUCTOR;
            case REF_INVOKE_INTERFACE -> MethodHandleType.INVOKE_INTERFACE;
            case REF_INVOKE_SPECIAL -> MethodHandleType.INVOKE_DIRECT;
            case REF_INVOKE_STATIC -> MethodHandleType.INVOKE_STATIC;
            case REF_INVOKE_VIRTUAL -> MethodHandleType.INVOKE_INSTANCE;
            case REF_GET_FIELD -> MethodHandleType.INSTANCE_GET;
            case REF_GET_FIELD_STATIC -> MethodHandleType.STATIC_GET;
            case REF_PUT_FIELD -> MethodHandleType.INSTANCE_PUT;
            case REF_PUT_FIELD_STATIC -> MethodHandleType.STATIC_PUT;
          };
      String dexClassName =
          DexUtil.toDexClassName(signature.getDeclClassType().getFullyQualifiedName());
      String methodName = signature.getName();
      List<String> parameters =
          signature.getParameterTypes().stream().map(DexUtil::toDexType).toList();
      String returnType = DexUtil.toDexType(signature.getType());
      Reference reference =
          new ImmutableMethodReference(dexClassName, methodName, parameters, returnType);
      dexMethodBuilder.addInstruction(
          new Instruction21c(
              Opcode.CONST_METHOD_HANDLE,
              targetRegister,
              new ImmutableMethodHandleReference(methodHandleType, reference)),
          currentStmt);
    } else {
      throw new IllegalArgumentException(
          "ReferenceSignature of methodHandle is no methodSignature "
              + handle.getReferenceSignature().getClass());
    }
  }

  @Override
  public void caseMethodType(@NonNull MethodType methodType) {
    List<String> parameters =
        methodType.getParameterTypes().stream().map(DexUtil::toDexType).toList();
    Type returnType = methodType.getReturnType();
    String dexReturnType = DexUtil.toDexType(returnType);
    dexMethodBuilder.addInstruction(
        new Instruction21c(
            Opcode.CONST_METHOD_TYPE,
            targetRegister,
            new ImmutableMethodProtoReference(parameters, dexReturnType)),
        currentStmt);
  }

  @Override
  public void defaultCaseConstant(@NonNull Constant constant) {
    throw new IllegalArgumentException("Unknown Constant " + constant.getType());
  }

  private void fixObjectType(Type defaultType) {
    if (targetRegister.getType().toString().equals("java.lang.Object")
        || targetRegister.isTypeGuessed()) {
      log.info("Set target register {} to type {}", targetRegister.getNumber(), defaultType);
      targetRegister.setType(defaultType);
      targetRegister.setIsTypeGuessed(true);
    }
  }
}
