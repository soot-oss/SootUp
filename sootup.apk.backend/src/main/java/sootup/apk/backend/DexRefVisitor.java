package sootup.apk.backend;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.Instruction11x;
import sootup.apk.backend.instructions.Instruction21c;
import sootup.apk.backend.instructions.Instruction22c;
import sootup.apk.backend.instructions.Instruction23x;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.visitor.AbstractRefVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;

public class DexRefVisitor extends AbstractRefVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexRefVisitor.class);

  private final RegisterAllocator registerAllocator;
  private final DexStmtVisitor dexStmtVisitor;

  private Stmt currentStmt;
  private String operation;
  private Register targetRegister;
  private Immediate immediate;

  public DexRefVisitor(DexStmtVisitor dexStmtVisitor, RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
    this.dexStmtVisitor = dexStmtVisitor;
  }

  public void setCurrentStmt(Stmt stmt) {
    this.currentStmt = stmt;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setTargetRegister(Register targetRegister) {
    this.targetRegister = targetRegister;
  }

  public void setImmediate(Immediate immediate) {
    this.immediate = immediate;
  }

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    String dexType = DexUtil.toDexType(ref.getType());
    FieldSignature fieldSignature = ref.getFieldSignature();
    FieldReference fieldReference =
        new ImmutableFieldReference(
            DexUtil.toDexClassName(fieldSignature.getDeclClassType().getFullyQualifiedName()),
            fieldSignature.getName(),
            dexType);
    Opcode opcode = getRefOpcode("S", operation, dexType);
    log.info(operation);
    if (operation.startsWith("GET")) {
      fixObjectType(ref.getType());
    }
    dexStmtVisitor.addInstruction(
        new Instruction21c(opcode, targetRegister, fieldReference), currentStmt);
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    String dexType = DexUtil.toDexType(ref.getType());
    FieldSignature fieldSignature = ref.getFieldSignature();
    FieldReference fieldReference =
        new ImmutableFieldReference(
            DexUtil.toDexClassName(fieldSignature.getDeclClassType().getFullyQualifiedName()),
            fieldSignature.getName(),
            dexType);
    Local instance = ref.getBase();
    Register instanceRegister = registerAllocator.getRegisterForImmediate(instance, false);
    Opcode opcode = getRefOpcode("I", operation, dexType);
    log.info(operation);
    if (operation.startsWith("GET")) {
      fixObjectType(ref.getType());
    }
    dexStmtVisitor.addInstruction(
        new Instruction22c(opcode, targetRegister, instanceRegister, fieldReference), currentStmt);
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    Local array = ref.getBase();
    Register arrayRegister = registerAllocator.getRegisterForImmediate(array, false);
    Immediate index = ref.getIndex();
    Register indexRegister = registerAllocator.getRegisterForImmediate(index, false);

    ArrayType arrayType = (ArrayType) array.getType();
    if (operation.startsWith("GET")) {
      fixObjectType(arrayType);
    }
    String dexType =
        arrayType.getDimension() > 1
            ? DexUtil.toDexType(ArrayType.createArrayType(arrayType.getBaseType(), 1))
            : DexUtil.toDexType(arrayType.getBaseType());

    Opcode opcode = getRefOpcode("A", operation, dexType);
    dexStmtVisitor.addInstruction(
        new Instruction23x(opcode, targetRegister, arrayRegister, indexRegister), currentStmt);
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    registerAllocator.getRegisterForParameter(immediate);
  }

  @Override
  public void caseCaughtExceptionRef(@NonNull JCaughtExceptionRef ref) {
    dexStmtVisitor.addInstruction(
        new Instruction11x(Opcode.MOVE_EXCEPTION, targetRegister), currentStmt);
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    registerAllocator.getRegisterForParameter(immediate);
  }

  @Override
  public void defaultCaseRef(Ref ref) {
    throw new RuntimeException("Unknown ref: " + ref.getClass());
  }

  private Opcode getRefOpcode(String scope, String operation, String dexType) {
    String checkType = dexType.startsWith("L") || dexType.startsWith("[") ? "L" : dexType;
    return switch (checkType) {
      case "Z" -> Opcode.valueOf(scope + operation.toUpperCase() + "_BOOLEAN");
      case "I", "F" -> Opcode.valueOf(scope + operation.toUpperCase());
      case "B" -> Opcode.valueOf(scope + operation.toUpperCase() + "_BYTE");
      case "C" -> Opcode.valueOf(scope + operation.toUpperCase() + "_CHAR");
      case "S" -> Opcode.valueOf(scope + operation.toUpperCase() + "_SHORT");
      case "J", "D" -> Opcode.valueOf(scope + operation.toUpperCase() + "_WIDE");
      case "L" -> Opcode.valueOf(scope + operation.toUpperCase() + "_OBJECT");
      default -> throw new IllegalArgumentException("Unknown dex type: " + dexType);
    };
  }

  private void fixObjectType(Type defaultType) {

    log.info("Target register type: {}", targetRegister.getType());
    log.info("Target register guessed: {}", targetRegister.isTypeGuessed());
    log.info("Is assign: {}", currentStmt.isJAssignStmt());
    log.info("New type: {}", defaultType);
    if (targetRegister.getType().toString().equals("java.lang.Object")
        || targetRegister.isTypeGuessed()) {

      if (targetRegister.getType() != defaultType && currentStmt.isJAssignStmt()) {
        targetRegister =
            registerAllocator.getRegisterForValueWithNewType(
                currentStmt.asJAssignStmt().getLeftOp(), defaultType, false);
      } else {
        targetRegister.setType(defaultType);
      }
      targetRegister.setIsTypeGuessed(true);
    }
  }
}
