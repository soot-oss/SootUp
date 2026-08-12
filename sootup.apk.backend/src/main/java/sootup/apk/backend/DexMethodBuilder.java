package sootup.apk.backend;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.*;
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
import sootup.core.types.PrimitiveType;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.views.View;

public class DexMethodBuilder {

  private static final Logger log = LoggerFactory.getLogger(DexMethodBuilder.class);
  private final View view;

  private LinkedHashMap<BasicBlock<?>, List<AbstractInstruction>> instructions;
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

      DexConstantVisitor dexConstantVisitor = new DexConstantVisitor(this);
      registerAllocator = new RegisterAllocator(dexConstantVisitor);
      DexStmtVisitor dexStmtVisitor =
          new DexStmtVisitor(view, registerAllocator, dexConstantVisitor, this, sootMethod);

      ControlFlowGraph<?> controlFlowGraph = sootMethod.getBody().getControlFlowGraph();
      Collection<? extends BasicBlock<?>> blocks = controlFlowGraph.getBlocks();
      log.info("BLOCKS: {}", blocks.size());

      Deque<BasicBlock<?>> worklist =
          new ArrayDeque<>(List.of(controlFlowGraph.getStartingStmtBlock()));
      Set<BasicBlock<?>> inWorklist =
          new HashSet<>(List.of(controlFlowGraph.getStartingStmtBlock()));
      Set<BasicBlock<?>> visitedBlocks = new HashSet<>();
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMapAtStart = new HashMap<>();
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMap = new HashMap<>();

      while (!worklist.isEmpty()) {
        currentBlock = worklist.pollFirst();
        if (currentBlock == null) {
          break;
        }
        inWorklist.remove(currentBlock);
        visitedBlocks.add(currentBlock);

        log.info("BLOCK {}", currentBlock);

        Set<BasicBlock<?>> previousBlocks =
            controlFlowGraph.predecessors(currentBlock.getHead()).stream()
                .map(controlFlowGraph::getBlockOf)
                .collect(Collectors.toSet());

        log.info("Previous blocks {}", previousBlocks.size());
        log.info("Previous locals:");
        previousBlocks.forEach(
            b -> {
              if (blockRegisterMap.containsKey(b)) {
                log.info("{}", blockRegisterMap.get(b).keySet());
              }
            });

        if (previousBlocks.isEmpty()) {
          registerAllocator.setRegisterMap(new HashMap<>());
        } else {
          HashMap<Local, Register> merged =
              mergeIncomingRegisterMaps(
                  new HashSet<>(currentBlock.getPredecessors()), blockRegisterMap);
          registerAllocator.setRegisterMap(merged);
          log.info("Locals at start of block:");
          registerAllocator.getRegisterMap().keySet().forEach(k -> log.info("{}", k.getName()));
          blockRegisterMapAtStart.put(currentBlock, new HashMap<>(merged));
        }

        List<Stmt> stmts = new ArrayList<>(currentBlock.getStmts());
        if (sootMethod.getName().equals("<init>")) {
          stmts = fixInitMethod(stmts);
        }
        for (Stmt stmt : stmts) {
          log.info("Process stmt: {}", stmt);
          stmt.accept(dexStmtVisitor);
        }
        log.info("Locals at end of block:");
        registerAllocator.getRegisterMap().keySet().forEach(k -> log.info("{}", k.getName()));
        blockRegisterMap.put(currentBlock, registerAllocator.getRegisterMap());
        registerAllocator.resetRegisterMap();

        List<? extends BasicBlock<?>> successors = currentBlock.getSuccessors();
        log.info("Successor count: {}", successors.size());

        if (!successors.isEmpty()) {

          // if the first successor has already been processed -> add a goto instruction
          if (!(currentBlock.getStmts().get(currentBlock.getStmtCount() - 1) instanceof JGotoStmt)
              && visitedBlocks.contains(successors.get(0))) {
            this.addInstruction(new Instruction10t(Opcode.GOTO, successors.get(0).getHead()), null);
          }

          // if the last statement of the current block is a goto instruction -> add all successors
          // to the end of the worklist
          if (currentBlock.getStmts().get(currentBlock.getStmtCount() - 1) instanceof JGotoStmt) {
            for (BasicBlock<? extends BasicBlock<?>> b : successors) {
              if (!(visitedBlocks.contains(b) || inWorklist.contains(b))) {
                worklist.addLast(b);
                inWorklist.add(b);
              }
            }
          } else {
            // add the successors to the beginning of the worklist
            for (int i = successors.size() - 1; i >= 0; i--) {
              var block = successors.get(i);
              if (!visitedBlocks.contains(block)) {
                if (inWorklist.remove(block)) {
                  worklist.remove(block);
                }
                worklist.addFirst(block);
                inWorklist.add(block);
              }
            }
          }

          // mergeBlocks
          successors.forEach(
              s -> {
                if (blockRegisterMap.containsKey(s) && visitedBlocks.contains(s)) {
                  log.info("Merge current register map to successor");
                  mergeCurrentRegisterMapToSuccessor(currentBlock, s, blockRegisterMap);
                }
              });
        }
      }

      for (var b : blocks) {
        log.info("Instructions of block {}", b.toString());
        if (!instructions.containsKey(b)) {
          log.error("Block {} not processed!", b);
          continue;
        }
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

      this.addBuilderInstructions(
          methodImplementationBuilder, labelAllocator, blockRegisterMapAtStart);

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
        previousBlocks.stream()
            .map(blockRegisterMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

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
      Type objectType = view.getIdentifierFactory().getClassType("java.lang.Object");

      Type type =
          regs.stream()
              .map(Register::getType)
              .filter(t -> !t.equals(objectType))
              .filter(
                  t -> {
                    if (t.equals(PrimitiveType.getInt())) {
                      return regs.stream()
                          .map(Register::getType)
                          .noneMatch(other -> !other.equals(t) && other instanceof PrimitiveType);
                    }
                    return true;
                  })
              .findFirst()
              .orElse(objectType);

      if (regs.stream()
          .filter(
              r ->
                  !r.getType().equals(view.getIdentifierFactory().getClassType("java.lang.Object")))
          .anyMatch(
              r ->
                  !r.getType().equals(type)
                      && !(type instanceof PrimitiveType
                          && r.getType().equals(PrimitiveType.getInt())))) {

        log.error(
            "Cannot merge {} because of conflicting type {} with new type {}",
            local,
            regs.stream()
                .filter(
                    r ->
                        !r.getType()
                            .equals(view.getIdentifierFactory().getClassType("java.lang.Object")))
                .filter(
                    r ->
                        !r.getType().equals(type)
                            && !(type instanceof PrimitiveType
                                && r.getType().equals(PrimitiveType.getInt())))
                .findFirst()
                .get()
                .getType(),
            type);
        continue;
      }

      log.info("Merge local {} with type {}", local.getName(), type);

      if (first.getType().equals(view.getIdentifierFactory().getClassType("java.lang.Object"))) {
        first.setType(type);
      }
      result.put(local, first);

      // a local has different registers but same type --> move previous registers to a new register
      // change all other registers to first
      for (BasicBlock<?> block : previousBlocks) {
        HashMap<Local, Register> localRegisterMap = blockRegisterMap.get(block);
        if (localRegisterMap == null) {
          continue;
        }
        Register old = localRegisterMap.get(local);
        if (old != null && !first.equals(old)) {
          var i = instructions.get(block);
          for (AbstractInstruction instruction : i) {
            instruction.changeRegister(old, first);
          }
        }
      }
    }

    return result;
  }

  private void mergeCurrentRegisterMapToSuccessor(
      BasicBlock<?> cBlock,
      BasicBlock<?> successorBlock,
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMap) {

    HashMap<Local, Register> registerMapCurrent = blockRegisterMap.get(cBlock);
    HashMap<Local, Register> registerMapSuccessor = blockRegisterMap.get(successorBlock);

    Set<Local> locals = registerMapCurrent.keySet();

    for (Local local : locals) {

      Register register = registerMapCurrent.get(local);
      Register registerSuccessor = registerMapSuccessor.get(local);

      if (registerSuccessor == null) {
        log.error("Local {} not included in successor", local);
        continue;
      }

      if (register == registerSuccessor) {
        continue;
      }

      Type type = register.getType();
      Type successorType = registerSuccessor.getType();

      if (!type.equals(view.getIdentifierFactory().getType("java.lang.Object"))
          && !successorType.equals(view.getIdentifierFactory().getType("java.lang.Object"))
          && !type.equals(successorType)) {
        if (type instanceof PrimitiveType.IntType && successorType instanceof PrimitiveType) {

        } else if (type instanceof PrimitiveType
            && successorType instanceof PrimitiveType.IntType) {
          registerSuccessor.setType(type);
        } else {
          log.error("Cannot merge {}", local);
          continue;
        }
      }

      if (successorType.equals(view.getIdentifierFactory().getType("java.lang.Object"))) {
        registerSuccessor.setType(type);
      }

      // a local has different registers but same type --> move previous registers to a new register
      // change all other registers to successor
      var i = instructions.get(currentBlock);
      for (AbstractInstruction instruction : i) {
        log.info("Change register according to successor for local {}", local);
        instruction.changeRegister(register, registerSuccessor);
      }
    }
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

      if (thisVariable != null) {
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

  public List<BuilderInstruction> addBuilderInstructions(
      MethodImplementationBuilder methodImplementationBuilder,
      LabelAssigner labelAssigner,
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMap) {

    log.info("ADD BUILDER INSTRUCTIONS");

    // Allocate registers
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

    // Reserve labels
    LinkedHashMap<SwitchPayload, BuilderInstruction> payloadInstructions = new LinkedHashMap<>();
    for (SwitchPayload switchPayload : switchPayloads) {
      switchPayload.setLabelAssigner(labelAssigner);
      payloadInstructions.put(switchPayload, switchPayload.getBuilderInstruction());
    }
    for (AbstractInstruction ins : instructions.values().stream().flatMap(List::stream).toList()) {
      if (ins instanceof Instruction10t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      } else if (ins instanceof Instruction21t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      } else if (ins instanceof Instruction22t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      }
    }

    // Process instructions
    List<BuilderInstruction> builderInstructions = new ArrayList<>();
    for (Map.Entry<BasicBlock<?>, List<AbstractInstruction>> entry : instructions.entrySet()) {
      currentBlock = entry.getKey();
      Set<Register> usedRegisters =
          new HashSet<>(blockRegisterMap.getOrDefault(currentBlock, new HashMap<>()).values());
      List<AbstractInstruction> instructionsOfBlock = entry.getValue();

      for (int i = 0; i < instructionsOfBlock.size(); i++) {
        AbstractInstruction instruction = instructionsOfBlock.get(i);
        instruction.setLabelAssigner(labelAssigner);
        log.info("Original instruction: {}", instruction.getOpcode());

        if (labelAssigner.hasLabel(instructionMap.get(instruction))) {
          labelAssigner.setLabel(instructionMap.get(instruction));
        }

        if (!tmpRegisters.isEmpty()) {
          List<Register> registers = instruction.getRegisters();
          int tmpIndx = 0;
          if (!(instruction instanceof Instruction3rc
              || instruction instanceof Instruction4rcc
              || (instruction instanceof Instruction12x
                  && instruction.getOpcode().name.startsWith("move"))
              || instruction instanceof Instruction22x
              || instruction instanceof Instruction32x
              || instruction instanceof Instruction11x
              || instruction instanceof Instruction21c
                  && instruction.getOpcode().name.equals("check-cast"))) {

            LinkedHashMap<Register, Register> registerHashMap = new LinkedHashMap<>();

            log.info("Tmp registers needed: {}", registers.size());
            for (int index = 0; index < registers.size(); index++) {
              Register r = registers.get(index);
              log.info("Tmp index: {}", tmpIndx);
              Register tmpRegister = tmpRegisters.get(tmpIndx);
              tmpRegister.setType(r.getType());
              tmpRegister.setIsTypeGuessed(r.isTypeGuessed());
              instruction.changeRegister(r, tmpRegister);
              if (!(instruction.getOpcode().name.contains("get")) || index > 0) {
                if (usedRegisters.contains(r) || r.isParameter()) {
                  AbstractInstruction move = generateMoveInstruction(tmpRegister, r, r.getType());
                  builderInstructions.add(move.getBuilderInstruction());
                  methodImplementationBuilder.addInstruction(move.getBuilderInstruction());
                } else {
                  log.info("no move because register is not used");
                }
              } else {
                log.info(
                    "no move because of opcode {} or index {}",
                    instruction.getOpcode().name.contains("get"),
                    index);
              }
              registerHashMap.put(tmpRegister, r);
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

            // reverse list of registers in order to insert the targetRegister at the end
            // ensures that the targetRegister is not overwritten
            List<Map.Entry<Register, Register>> reverseRegisters =
                new ArrayList<>(registerHashMap.entrySet());
            Collections.reverse(reverseRegisters);

            // move tmpRegisters back to their original registers
            reverseRegisters.forEach(
                r -> {
                  Register tmpRegister = r.getKey();
                  Register originalRegister = r.getValue();
                  AbstractInstruction move =
                      generateMoveInstruction(
                          originalRegister, tmpRegister, originalRegister.getType());
                  builderInstructions.add(move.getBuilderInstruction());
                  methodImplementationBuilder.addInstruction(move.getBuilderInstruction());
                  usedRegisters.addAll(move.getRegisters());
                });
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

    // add payloads
    payloadInstructions.forEach(
        (switchPayload, builderInstruction) -> {
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
    if (stmt != null) {
      log.info("Add instruction {} of stmt {}", instruction.getOpcode(), stmt.toString());
    } else {
      log.info("Add instruction {} of stmt null", instruction.getOpcode());
    }

    instructionMap.put(instruction, stmt);
    instructions.computeIfAbsent(currentBlock, k -> new ArrayList<>()).add(instruction);
  }

  public void addSwitchPayload(SwitchPayload switchPayload) {
    this.switchPayloads.add(switchPayload);
  }

  public void setRegisterAllocator(RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }

  public void setCurrentBlock(BasicBlock<?> block) {
    this.currentBlock = block;
  }
}
