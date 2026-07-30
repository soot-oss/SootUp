package sootup.apk.backend;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.views.View;

public class DexMethodBuilder {

  private static final Logger log = LoggerFactory.getLogger(DexMethodBuilder.class);
  private final View view;

  private List<AbstractInstruction> instructions;
  private HashMap<AbstractInstruction, Stmt> instructionMap;

  private RegisterAllocator registerAllocator;

  public DexMethodBuilder(View view) {
    this.view = view;
    instructions = new ArrayList<>();
    this.instructionMap = new HashMap<>();
  }

  public ImmutableMethod createMethod(SootMethod sootMethod) {
    String className =
        DexUtil.toDexClassName(sootMethod.getDeclaringClassType().getFullyQualifiedName());

    String methodName = sootMethod.getName();
    log.info("Creating method {}", methodName);
    if ((methodName.indexOf('<') >= 0 || methodName.indexOf('>') >= 0)
        && !"<init>".equals(methodName)
        && !"<clinit>".equals(methodName)) {
      throw new RuntimeException("Invalid method name: " + sootMethod.getSignature());
    }

    int accessFlags =
        sootMethod.getModifiers().stream()
            .mapToInt(MethodModifier::getBytecode)
            .reduce(0, (flagsBefore, newFlag) -> flagsBefore | newFlag);

    List<MethodParameter> parameters = null;
    if (sootMethod.getParameterCount() > 0) {
      parameters = new ArrayList<>();
      for (Type parameterType : sootMethod.getParameterTypes()) {
        String dexParamType = DexUtil.toDexType(parameterType);
        Set<Annotation> annotations = null; // TODO
        String parameterName = null; // TODO
        parameters.add(new ImmutableMethodParameter(dexParamType, annotations, parameterName));
      }
    }

    Type returnType = sootMethod.getReturnType();
    String dexReturnType = DexUtil.toDexType(returnType);

    Set<Annotation> annotations = null; // TODO

    MethodImplementation methodImplementation = createMethodImplementation(sootMethod);

    return new ImmutableMethod(
        className,
        methodName,
        parameters,
        dexReturnType,
        accessFlags,
        annotations,
        null,
        methodImplementation);
  }

  private MethodImplementation createMethodImplementation(SootMethod sootMethod) {
    instructions = new ArrayList<>();
    instructionMap = new HashMap<>();
    try {
      if (sootMethod.isAbstract() || sootMethod.isNative() || !sootMethod.hasBody()) {
        return null;
      }

      List<Stmt> stmts = sootMethod.getBody().getStmts();

      if (sootMethod.getName().equals("<init>")) {
        fixInitMethod(stmts);
      }

      DexConstantVisitor dexConstantVisitor = new DexConstantVisitor(this);
      registerAllocator = new RegisterAllocator(dexConstantVisitor);
      DexStmtVisitor dexStmtVisitor =
          new DexStmtVisitor(view, registerAllocator, dexConstantVisitor, this, sootMethod);

      for (Stmt stmt : stmts) {
        stmt.accept(dexStmtVisitor);
      }

      int parameterSizeCount = DexUtil.getRegisterSizeCount(sootMethod.getParameterTypes());
      if (!sootMethod.isStatic()) {
        parameterSizeCount += 1; // register for "this"
      }

      int registerCount =
          registerAllocator.getRegisterCount() > 16
              ? registerAllocator.getRegisterCount() + 16
              : registerAllocator.getRegisterCount();

      MethodImplementationBuilder methodImplementationBuilder =
          new MethodImplementationBuilder(Math.max(registerCount, parameterSizeCount));

      LabelAssigner labelAllocator = new LabelAssigner(methodImplementationBuilder);

      List<BuilderInstruction> instructions =
          this.addBuilderInstructions(methodImplementationBuilder, labelAllocator);

      return methodImplementationBuilder.getMethodImplementation();

    } catch (Exception e) {
      throw new RuntimeException("Error while processing method " + sootMethod, e);
    }
  }

  // Remove all instructions that reference to p0 (this) until it is initialized
  private void fixInitMethod(List<Stmt> stmts) {
    int targetIndex = -1;
    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      if ((stmt instanceof JInvokeStmt jInvokeStmt
              && jInvokeStmt.getInvokeExpr().isPresent()
              && jInvokeStmt.getInvokeExpr().get() instanceof JSpecialInvokeExpr expr
              && expr.getMethodSignature().getName().equals("<init>"))
          || (stmt instanceof JAssignStmt jAssignStmt
              && jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr expr2
              && expr2.getMethodSignature().getName().equals("<init>"))) {
        targetIndex = i;
      }
    }
    if (targetIndex > 0) {
      Local thisVariable = null;
      for (int i = 0; i < targetIndex; i++) {
        Stmt stmt = stmts.get(i);
        if (stmt instanceof JIdentityStmt jIdentityStmt
            && jIdentityStmt.getRightOp() instanceof JThisRef) {
          thisVariable = jIdentityStmt.getLeftOp();
        }
      }

      Stmt constructorCall = stmts.get(targetIndex);
      if (constructorCall instanceof JInvokeStmt jInvokeStmt
          && jInvokeStmt.getInvokeExpr().isPresent()
          && jInvokeStmt.getInvokeExpr().get() instanceof JSpecialInvokeExpr expr) {
        JSpecialInvokeExpr newInvoke =
            Jimple.newSpecialInvokeExpr(thisVariable, expr.getMethodSignature(), expr.getArgs());
        JInvokeStmt jInvokeStmt1 = new JInvokeStmt(newInvoke, constructorCall.getPositionInfo());
        stmts.set(targetIndex, jInvokeStmt1);
      } else if (constructorCall instanceof JAssignStmt jAssignStmt
          && jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr expr) {
        JSpecialInvokeExpr newInvoke =
            Jimple.newSpecialInvokeExpr(thisVariable, expr.getMethodSignature(), expr.getArgs());
        JAssignStmt jAssignStmt1 =
            new JAssignStmt(jAssignStmt.getLeftOp(), newInvoke, jAssignStmt.getPositionInfo());
        stmts.set(targetIndex, jAssignStmt1);
      }

      if (thisVariable != null) {
        Immediate finalThisVariable = thisVariable;
        for (int i = Math.min(targetIndex - 1, stmts.size() - 1); i >= 0; i--) {
          Stmt stmt = stmts.get(i);
          if (!(stmt instanceof JIdentityStmt)
              && stmt.getUses().anyMatch(value -> value.equals(finalThisVariable))) {
            stmts.remove(i);
          }
        }
      }
    }
  }

  public List<BuilderInstruction> addBuilderInstructions(
      MethodImplementationBuilder methodImplementationBuilder, LabelAssigner labelAssigner) {

    List<Register> sortedRegisters =
        registerAllocator.getRegisters().stream()
            .sorted(Comparator.comparing(Register::isParameter))
            .collect(Collectors.toCollection(ArrayList::new));

    List<Register> tmpRegisters = new ArrayList<>();

    if (registerAllocator.getRegisterCount() > 16) {
      for (int i = 0; i < 16; i++) {
        tmpRegisters.add(new Register(i, UnknownType.getInstance(), false, true));
      }
    }

    List<Register> allRegisters =
        Stream.concat(tmpRegisters.stream(), sortedRegisters.stream()).toList();

    int registerIndex = 0;
    for (Register r : allRegisters) {
      r.setNumber(registerIndex);
      registerIndex += r.getSize();
      log.info("Register {} with size {}", r.getNumber(), r.getSize());
    }

    List<BuilderInstruction> builderInstructions = new ArrayList<>();

    Set<Register> usedRegisters = new HashSet<>();

    int tmpIndx = 0;
    for (int i = 0; i < instructions.size(); i++) {
      AbstractInstruction instruction = instructions.get(i);
      instruction.setLabelAssigner(labelAssigner);

      if (labelAssigner.hasLabel(instructionMap.get(instruction))) {
        labelAssigner.addLabel(instructionMap.get(instruction));
      }

      if (!tmpRegisters.isEmpty()) {
        List<Register> registers = instruction.getRegisters();
        log.info("{}", instruction.getOpcode().name);
        if (!(instruction instanceof Instruction3rc
            || instruction instanceof Instruction4rcc
            || (instruction instanceof Instruction12x
                && instruction.getOpcode().name.startsWith("move"))
            || instruction instanceof Instruction22x
            || instruction instanceof Instruction32x
            || instruction instanceof Instruction11x
            || instruction instanceof Instruction21c
                && instruction.getOpcode().name.equals("check-cast"))) {

          HashMap<Register, Register> registerHashMap = new HashMap<>();

          for (Register r : registers) {
            Register tmpRegister = tmpRegisters.get(tmpIndx);
            tmpRegister.setType(r.getType());
            instruction.changeRegister(r, tmpRegister);
            if (!instruction.getOpcode().name.contains("get")) {
              if (usedRegisters.contains(r) || r.isParameter()) {
                AbstractInstruction move =
                    generateMoveInstruction(tmpRegisters.get(tmpIndx), r, r.getType());
                builderInstructions.add(move.getBuilderInstruction());
                methodImplementationBuilder.addInstruction(move.getBuilderInstruction());
              }
            }
            registerHashMap.put(tmpRegisters.get(tmpIndx), r);
            tmpIndx += DexUtil.isWide(r.getType()) ? 2 : 1;
          }

          builderInstructions.add(instruction.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
          usedRegisters.addAll(instruction.getRegisters());

          if (instruction.getOpcode().name.startsWith("if")) {
            registerHashMap.clear();
          }

          if (instructions.size() > i + 1
              && (instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT)
                  || instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT_OBJECT)
                  || instructions.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT_WIDE))) {
            continue;
          }

          registerHashMap.forEach(
              (tmpRegister, originalRegister) -> {
                AbstractInstruction move =
                    generateMoveInstruction(
                        originalRegister, tmpRegister, originalRegister.getType());
                builderInstructions.add(move.getBuilderInstruction());
                methodImplementationBuilder.addInstruction(move.getBuilderInstruction());
                usedRegisters.addAll(move.getRegisters());
              });
          tmpIndx = 0;
          usedRegisters.addAll(registerHashMap.values());
        } else if (instruction instanceof Instruction21c instruction21c
            && instruction.getOpcode().name.equals("check-cast")) {
          Register tmpRegister = tmpRegisters.get(tmpIndx);
          Register originalRegister = registers.get(0);
          tmpRegister.setType(originalRegister.getType());
          instruction.changeRegister(originalRegister, tmpRegister);
          AbstractInstruction move =
              generateMoveInstruction(
                  tmpRegisters.get(tmpIndx), originalRegister, originalRegister.getType());
          builderInstructions.add(move.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(move.getBuilderInstruction());
          builderInstructions.add(instruction.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
          usedRegisters.addAll(instruction.getRegisters());

          TypeReference typeReference = (TypeReference) instruction21c.getReference();
          AbstractInstruction move2 =
              generateMoveInstruction(
                  originalRegister,
                  tmpRegister,
                  sootup.apk.frontend.Util.DexUtil.toSootType(typeReference.getType(), 0));
          builderInstructions.add(move2.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(move2.getBuilderInstruction());
          usedRegisters.addAll(move2.getRegisters());

        } else if ((instruction instanceof Instruction12x
                && instruction.getOpcode().name.startsWith("move"))
            || instruction instanceof Instruction22x
            || instruction instanceof Instruction32x) {
          instruction =
              generateMoveInstruction(
                  instruction.getRegisters().get(0),
                  instruction.getRegisters().get(1),
                  instruction.getRegisters().get(0).getType());
          builderInstructions.add(instruction.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
          usedRegisters.addAll(instruction.getRegisters());
        } else {
          builderInstructions.add(instruction.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
          usedRegisters.addAll(instruction.getRegisters());
        }

      } else {
        builderInstructions.add(instruction.getBuilderInstruction());
        methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
        usedRegisters.addAll(instruction.getRegisters());
      }
    }
    return builderInstructions;
  }

  protected AbstractInstruction generateMoveInstruction(
      Register targetR, Register sourceRegister, Type valueType) {

    if (valueType instanceof ReferenceType) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        return new Instruction12x(Opcode.MOVE_OBJECT, targetR, sourceRegister);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        return new Instruction22x(Opcode.MOVE_OBJECT_FROM16, targetR, sourceRegister);
      } else {
        return new Instruction32x(Opcode.MOVE_OBJECT_16, targetR, sourceRegister);
      }
    } else if (DexUtil.isWide(valueType)) {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        return new Instruction12x(Opcode.MOVE_WIDE, targetR, sourceRegister);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        return new Instruction22x(Opcode.MOVE_WIDE_FROM16, targetR, sourceRegister);
      } else {
        return new Instruction32x(Opcode.MOVE_WIDE_16, targetR, sourceRegister);
      }
    } else {
      if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
        return new Instruction12x(Opcode.MOVE, targetR, sourceRegister);
      } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
        return new Instruction22x(Opcode.MOVE_FROM16, targetR, sourceRegister);
      } else {
        return new Instruction32x(Opcode.MOVE_16, targetR, sourceRegister);
      }
    }
  }

  protected void addInstruction(AbstractInstruction instruction, Stmt stmt) {
    instructionMap.put(instruction, stmt);
    instructions.add(instruction);
  }

  public void setRegisterAllocator(RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }
}
