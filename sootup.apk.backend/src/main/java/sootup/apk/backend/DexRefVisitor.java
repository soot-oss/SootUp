package sootup.apk.backend;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction23x;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractRefVisitor;
import sootup.core.types.ArrayType;

public class DexRefVisitor extends AbstractRefVisitor {

  private static final Logger log = LoggerFactory.getLogger(DexRefVisitor.class);

  RegisterAllocator registerAllocator;
  DexStmtVisitor dexStmtVisitor;

  String operation;
  Register register;

  public DexRefVisitor(DexStmtVisitor dexStmtVisitor, RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
    this.dexStmtVisitor = dexStmtVisitor;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setRegister(Register register) {
    this.register = register;
  }

  @Override
  public void caseStaticFieldRef(@NonNull JStaticFieldRef ref) {
    String dexType = DexUtil.toDexType(ref.getType());
    FieldReference fieldReference =
        new ImmutableFieldReference(
            DexUtil.toDexClassName(ref.getClass().getName()), ref.toString(), dexType);
    Opcode opcode = getRefOpcode("S", operation, dexType);
    log.info(
        "{} v{}, {}->{}:{}",
        opcode.toString().toLowerCase(),
        register.getNumber(),
        fieldReference.getDefiningClass(),
        fieldReference.getName(),
        fieldReference.getType());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction21c(opcode, register.getNumber(), fieldReference));
  }

  @Override
  public void caseInstanceFieldRef(@NonNull JInstanceFieldRef ref) {
    String dexType = DexUtil.toDexType(ref.getType());
    FieldReference fieldReference =
        new ImmutableFieldReference(
            DexUtil.toDexClassName(ref.getClass().getName()), ref.toString(), dexType);
    Local instance = ref.getBase();
    Register instanceRegister = registerAllocator.getRegisterForImmediate(instance);
    Opcode opcode = getRefOpcode("I", operation, dexType);
    log.info(
        "{} v{}, v{}, {}->{}:{}",
        opcode.toString().toLowerCase(),
        register.getNumber(),
        instanceRegister.getNumber(),
        fieldReference.getDefiningClass(),
        fieldReference.getName(),
        fieldReference.getType());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction22c(
            opcode, register.getNumber(), instanceRegister.getNumber(), fieldReference));
  }

  @Override
  public void caseArrayRef(@NonNull JArrayRef ref) {
    Local array = ref.getBase();
    Register arrayRegister = registerAllocator.getRegisterForImmediate(array);
    Immediate index = ref.getIndex();
    Register indexRegister = registerAllocator.getRegisterForImmediate(index);

    ArrayType arrayType = (ArrayType) array.getType();
    String dexType =
        arrayType.getDimension() > 1
            ? DexUtil.toDexType(ArrayType.createArrayType(arrayType.getBaseType(), 1))
            : DexUtil.toDexType(arrayType.getBaseType());

    Opcode opcode = getRefOpcode("A", operation, dexType);
    log.info(
        "{} v{}, v{}, v{}",
        opcode.toString().toLowerCase(),
        register.getNumber(),
        arrayRegister.getNumber(),
        indexRegister.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction23x(
            opcode, register.getNumber(), arrayRegister.getNumber(), indexRegister.getNumber()));
  }

  @Override
  public void caseParameterRef(@NonNull JParameterRef ref) {
    // e.g. r1 := @parameter0
    // TODO
  }

  @Override
  public void caseCaughtExceptionRef(@NonNull JCaughtExceptionRef ref) {
    log.info("move-exception v{}", register.getNumber());
    dexStmtVisitor.addInstruction(
        new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, register.getNumber()));
  }

  @Override
  public void caseThisRef(@NonNull JThisRef ref) {
    // e.g. p0 = "this"
    // TODO
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
}
