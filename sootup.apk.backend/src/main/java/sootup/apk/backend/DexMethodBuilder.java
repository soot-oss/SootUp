package sootup.apk.backend;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.*;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31t;
import org.jf.dexlib2.builder.instruction.BuilderPackedSwitchPayload;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
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

  private Map<BasicBlock<?>, List<AbstractInstruction>> instructions;
  private HashMap<AbstractInstruction, Stmt> instructionMap;

  private final List<SwitchPayload> switchPayloads;

  private RegisterAllocator registerAllocator;

  private BasicBlock<?> currentBlock;

  public DexMethodBuilder(View view) {
    this.view = view;
    instructions = new LinkedHashMap<>();
    this.instructionMap = new HashMap<>();
    this.switchPayloads = new ArrayList<>();
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
    instructions = new LinkedHashMap<>();
    instructionMap = new HashMap<>();
    try {
      if (sootMethod.isAbstract() || sootMethod.isNative() || !sootMethod.hasBody()) {
        return null;
      }

      ControlFlowGraph<?> controlFlowGraph = sootMethod.getBody().getControlFlowGraph();
      Collection<? extends BasicBlock<?>> blocks = controlFlowGraph.getBlocks();
      log.info("BLOCKS: {}", blocks.size());

      DexConstantVisitor dexConstantVisitor = new DexConstantVisitor(this);
      registerAllocator = new RegisterAllocator(dexConstantVisitor);
      DexStmtVisitor dexStmtVisitor =
          new DexStmtVisitor(view, registerAllocator, dexConstantVisitor, this, sootMethod);

      Queue<BasicBlock<?>> worklist = new ArrayDeque<>(controlFlowGraph.getBlocks());
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMap = new HashMap<>();

      while (!worklist.isEmpty()) {
        currentBlock = worklist.poll();

        log.info("BLOCK {}", currentBlock.toString());

        Set<BasicBlock<?>> previousBlocks =
            controlFlowGraph.predecessors(currentBlock.getHead()).stream()
                .map(controlFlowGraph::getBlockOf)
                .collect(Collectors.toSet());

        if (previousBlocks.isEmpty()) {
          registerAllocator.setRegisterMap(new HashMap<>());
        } else {
          HashMap<Local, Register> merged =
              mergeIncomingRegisterMaps(previousBlocks, blockRegisterMap);
          registerAllocator.setRegisterMap(merged);
        }

        List<Stmt> stmts = currentBlock.getStmts();
        if (sootMethod.getName().equals("<init>")) {
          stmts = fixInitMethod(stmts);
        }
        for (Stmt stmt : stmts) {
          log.info("Process stmt: {}", stmt);
          stmt.accept(dexStmtVisitor);
        }
        blockRegisterMap.put(currentBlock, registerAllocator.getRegisterMap());
        registerAllocator.resetRegisterMap();
      }

      for (var b : blocks) {
        log.info("Instructions of block {}", b.toString());
        for (var s : instructions.get(b)) {
          log.info("{}", s.getOpcode());
        }
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

  // merge register maps at branching points
  private HashMap<Local, Register> mergeIncomingRegisterMaps(
      Set<BasicBlock<?>> previousBlocks,
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMap) {

    Set<HashMap<Local, Register>> registerMaps =
        previousBlocks.stream().map(blockRegisterMap::get).collect(Collectors.toSet());

    HashMap<Local, Register> result = new HashMap<>();
    Set<Local> locals = new HashSet<>();

    for (BasicBlock<?> block : previousBlocks) {
      HashMap<Local, Register> registerMap = blockRegisterMap.get(block);
      if (registerMap != null) {
        locals.addAll(registerMap.keySet());
      }
    }

    for (Local local : locals) {

      List<Register> regs =
          registerMaps.stream().map(m -> m.get(local)).filter(Objects::nonNull).toList();

      Register first = regs.get(0);

      // if a local has the same register in all registerMaps, there is nothing to do
      if (regs.stream().allMatch(r -> r.equals(first))) {
        result.put(local, first);
        continue;
      }

      // a local has different types in different registerMap
      if (!regs.stream().allMatch(r -> r.getType().equals(first.getType()))) {
        throw new RuntimeException("Cannot merge " + local);
      }

      // a local has different registers but same type --> move previous registers to a new register
      Register merged = registerAllocator.getRegisterForImmediate(local, false);
      for (BasicBlock<?> block : previousBlocks) {
        HashMap<Local, Register> localRegisterMap = blockRegisterMap.get(block);
        if (localRegisterMap == null) {
          continue;
        }
        Register old = localRegisterMap.get(local);
        if (old != null && !merged.equals(old)) {
          AbstractInstruction move = generateMoveInstruction(merged, old, old.getType());
          instructionMap.put(move, null);
          List<AbstractInstruction> prevInstr =
              instructions.computeIfAbsent(block, k -> new ArrayList<>());
          if (!prevInstr.isEmpty()
              && (prevInstr.get(prevInstr.size() - 1).getOpcode().name.startsWith("goto")
                  || prevInstr.get(prevInstr.size() - 1).getOpcode().name.startsWith("if"))) {
            prevInstr.add(prevInstr.size() - 1, move);
          } else {
            prevInstr.add(move);
          }
        }
      }
      result.put(local, merged);
    }

    return result;
  }

  // Remove all instructions that reference to p0 (this) until it is initialized
  private List<Stmt> fixInitMethod(List<Stmt> stmts) {
    stmts = new ArrayList<>(stmts);
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
    return stmts;
  }

  public void addBuilderInstructionsExampleSwitch(
      MethodImplementationBuilder builder, LabelAssigner labelAssigner) {
    Label payload = builder.getLabel("1");
    Label case0 = builder.getLabel("2");
    Label case1 = builder.getLabel("3");


    // switch
    builder.addInstruction(new BuilderInstruction31t(Opcode.PACKED_SWITCH, 0, payload));

    // default path
    builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

    // case blocks
    builder.addLabel("0");
    builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

    builder.addLabel("1");
    builder.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));

    // payload must still be inside the method
    builder.addLabel("2");
    builder.addInstruction(new BuilderPackedSwitchPayload(0, Arrays.asList(case0, case1)));
  }

  public List<BuilderInstruction> addBuilderInstructions(
      MethodImplementationBuilder methodImplementationBuilder, LabelAssigner labelAssigner) {

    log.info("ADD BUILDER INSTRUCTIONS");
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
    }

    List<BuilderInstruction> builderInstructions = new ArrayList<>();

    Set<Register> usedRegisters = new HashSet<>();

    LinkedHashMap<SwitchPayload, BuilderInstruction> payloadInstructions = new LinkedHashMap<>();
    for (SwitchPayload switchPayload : switchPayloads) {
      switchPayload.setLabelAssigner(labelAssigner);
      payloadInstructions.put(switchPayload, switchPayload.getBuilderInstruction());
    }

    for (Map.Entry<BasicBlock<?>, List<AbstractInstruction>> entry : instructions.entrySet()) {
      currentBlock = entry.getKey();
      List<AbstractInstruction> instructionsOfBlock = entry.getValue();

      int tmpIndx = 0;
      for (int i = 0; i < instructionsOfBlock.size(); i++) {
        AbstractInstruction instruction = instructionsOfBlock.get(i);
        instruction.setLabelAssigner(labelAssigner);
        log.info("Original instruction: {}", instruction.getOpcode());

        if (labelAssigner.hasLabel(instructionMap.get(instruction))) {
          log.info("Set label at instruction {}", instruction.getOpcode().name);
          labelAssigner.setLabel(instructionMap.get(instruction));
        }

        if (!tmpRegisters.isEmpty()) {
          List<Register> registers = instruction.getRegisters();
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

            if (instructionsOfBlock.size() > i + 1
                && (instructionsOfBlock.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT)
                    || instructionsOfBlock.get(i + 1).getOpcode().equals(Opcode.MOVE_RESULT_OBJECT)
                    || instructionsOfBlock
                        .get(i + 1)
                        .getOpcode()
                        .equals(Opcode.MOVE_RESULT_WIDE))) {
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
    }

    payloadInstructions.forEach(
        (switchPayload, builderInstruction) -> {
          log.info("Set label for payload");
          labelAssigner.setLabel(switchPayload);
          switchPayload.logSmali();
          builderInstructions.add(builderInstruction);
          methodImplementationBuilder.addInstruction(builderInstruction);
        });

    log.info("Builder instructions created");

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
    instructions.computeIfAbsent(currentBlock, k -> new ArrayList<>()).add(instruction);
  }

  public void addSwitchPayload(SwitchPayload switchPayload) {
    this.switchPayloads.add(switchPayload);
  }

  public void setRegisterAllocator(RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }
}
