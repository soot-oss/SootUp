package sootup.apk.backend;

import java.util.List;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.visitor.AbstractConstantVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;

public class DexConstantVisitor extends AbstractConstantVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexConstantVisitor.class);

  DexBuilder dexBuilder;
  DexStmtVisitor dexStmtVisitor;
  Register targetRegister;

  public DexConstantVisitor(DexBuilder dexBuilder, DexStmtVisitor dexStmtVisitor) {
    this.dexBuilder = dexBuilder;
    this.dexStmtVisitor = dexStmtVisitor;
  }

  public void setTargetRegister(Register targetRegister) {
    this.targetRegister = targetRegister;
  }

  @Override
  public void caseBooleanConstant(@NonNull BooleanConstant constant) {
    int value = constant.getValue() ? 1 : 0;
    logNewConst("const/4", targetRegister.getNumber(), Integer.toHexString(value), false);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction11n(Opcode.CONST_4, targetRegister.getNumber(), value));
  }

  @Override
  public void caseDoubleConstant(@NonNull DoubleConstant constant) {
    double value = constant.getValue();
    long bits = Double.doubleToLongBits(value);
    logNewConst("const-wide", targetRegister.getNumber(), Long.toHexString(bits), true);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction51l(Opcode.CONST_WIDE, targetRegister.getNumber(), bits));
  }

  @Override
  public void caseFloatConstant(@NonNull FloatConstant constant) {
    float value = constant.getValue();
    int bits = Float.floatToIntBits(value);
    logNewConst("const", targetRegister.getNumber(), Integer.toHexString(bits), false);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction31i(Opcode.CONST, targetRegister.getNumber(), bits));
  }

  @Override
  public void caseIntConstant(@NonNull IntConstant constant) {
    int value = constant.getValue();
    if (DexUtil.inSigned4Bit(value)) {
      logNewConst("const/4", targetRegister.getNumber(), Integer.toHexString(value), false);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction11n(Opcode.CONST_4, targetRegister.getNumber(), value));
    } else if (DexUtil.inSigned16Bit(value)) {
      logNewConst("const/16", targetRegister.getNumber(), Integer.toHexString(value), false);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21s(Opcode.CONST_16, targetRegister.getNumber(), value));
    } else {
      logNewConst("const", targetRegister.getNumber(), Integer.toHexString(value), false);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction31i(Opcode.CONST, targetRegister.getNumber(), value));
    }
  }

  @Override
  public void caseLongConstant(@NonNull LongConstant constant) {
    long value = constant.getValue();
    if (DexUtil.inSigned16Bit(value)) {
      logNewConst("const-wide/16", targetRegister.getNumber(), Long.toHexString(value), true);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21s(Opcode.CONST_WIDE_16, targetRegister.getNumber(), (int) value));
    } else if (DexUtil.inSigned32Bit(value)) {
      logNewConst("const-wide/32", targetRegister.getNumber(), Long.toHexString(value), true);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction31i(Opcode.CONST_WIDE_32, targetRegister.getNumber(), (int) value));
    } else {
      logNewConst("const-wide", targetRegister.getNumber(), Long.toHexString(value), true);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction51l(Opcode.CONST_WIDE, targetRegister.getNumber(), value));
    }
  }

  @Override
  public void caseNullConstant(@NonNull NullConstant constant) {
    logNewConst("const/16", targetRegister.getNumber(), Integer.toHexString(0), false);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21s(Opcode.CONST_16, targetRegister.getNumber(), 0));
  }

  @Override
  public void caseStringConstant(@NonNull StringConstant constant) {
    log.info("const-string v{}, \"{}\"", targetRegister.getNumber(), constant.getValue());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(
            Opcode.CONST_STRING,
            targetRegister.getNumber(),
            new ImmutableStringReference(constant.getValue())));
    dexBuilder.internStringReference(constant.getValue());
    // const-string/jumbo --> if application has > 65,535 strings total
  }

  @Override
  public void caseEnumConstant(@NonNull EnumConstant constant) {
    ClassType type = (ClassType) constant.getType();
    String name = DexUtil.toDexClassName(type.getFullyQualifiedName());
    String value = constant.getValue();
    log.info("sget-object {}, {};->{}:{}", targetRegister, name, value, name);
    FieldReference enumRef = new ImmutableFieldReference(name, value, name);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(Opcode.SGET_OBJECT, targetRegister.getNumber(), enumRef));
  }

  @Override
  public void caseClassConstant(@NonNull ClassConstant constant) {
    TypeReference referencedClass =
        new ImmutableTypeReference(DexUtil.toDexClassName(constant.getValue()));
    log.info("const-class v{}, 0x{}", targetRegister, referencedClass.getType());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(Opcode.CONST_CLASS, targetRegister.getNumber(), referencedClass));
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
      log.info(
          "const-method-handle {}, {}, {}->{}({}){}",
          targetRegister.getNumber(),
          MethodHandleType.toString(methodHandleType).toLowerCase(),
          dexClassName,
          methodName,
          "(" + String.join("", parameters) + ")",
          returnType);
      dexStmtVisitor.addInstruction(
          new BuilderInstruction21c(
              Opcode.CONST_METHOD_HANDLE,
              targetRegister.getNumber(),
              new ImmutableMethodHandleReference(methodHandleType, reference)));
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
    log.info(
        "const-method-type {}, ({}){}",
        targetRegister.getNumber(),
        String.join("", parameters),
        dexReturnType);
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(
            Opcode.CONST_METHOD_TYPE,
            targetRegister.getNumber(),
            new ImmutableMethodProtoReference(parameters, dexReturnType)));
  }

  @Override
  public void defaultCaseConstant(@NonNull Constant constant) {
    throw new IllegalArgumentException("Unknown Constant " + constant.getType());
  }

  private void logNewConst(String range, int register, String value, boolean isWide) {
    log.info("{}{} v{}, 0x{}", range, isWide ? "L" : "", register, value);
  }
}
