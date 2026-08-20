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
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.*;
import sootup.core.views.View;

public class DexMethodBuilder {

  private static final Logger log = LoggerFactory.getLogger(DexMethodBuilder.class);
  private final View view;

  private LinkedHashMap<BasicBlock<?>, List<AbstractInstruction>> instructions;
  private HashMap<AbstractInstruction, Stmt> instructionMap;

  private List<AbstractPayload> payloads;

  private RegisterAllocator registerAllocator;

  private BasicBlock<?> currentBlock;

  ArrayList<TryBlockStmts> tryBlocks;

  static class TryBlockStmts {
    String jimpleExceptionType;
    Stmt startStmt;
    Stmt endStmt;
    Stmt catchStmt;
  }

  public DexMethodBuilder(View view) {
    this.view = view;
    instructions = new LinkedHashMap<>();
    tryBlocks = new ArrayList<>();
    this.instructionMap = new HashMap<>();
    this.payloads = new ArrayList<>();
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
    payloads = new ArrayList<>();
    tryBlocks = new ArrayList<>();
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
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMapAtEnd = new HashMap<>();

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
              if (blockRegisterMapAtEnd.containsKey(b)) {
                log.info("{}", blockRegisterMapAtEnd.get(b).keySet());
              }
            });

        if (previousBlocks.isEmpty()) {
          registerAllocator.setRegisterMap(new HashMap<>());
        } else {

          List<BasicBlock<?>> predecessors = new ArrayList<>();
          predecessors.addAll(currentBlock.getPredecessors());
          predecessors.addAll(currentBlock.getExceptionalPredecessors().values());
          HashMap<Local, Register> merged =
              mergeIncomingRegisterMaps(new HashSet<>(predecessors), blockRegisterMapAtEnd);
          registerAllocator.setRegisterMap(merged);
          log.info("Locals at start of block:");
          registerAllocator
              .getRegisterMap()
              .forEach(
                  (key, value) ->
                      log.info("{}:{}:{}", key.getName(), value.getNumber(), value.getType()));
          blockRegisterMapAtStart.put(currentBlock, new HashMap<>(merged));
        }

        List<Stmt> stmts = new ArrayList<>(currentBlock.getStmts());
        if (sootMethod.getName().equals("<init>")) {
          stmts = fixInitMethod(stmts);
        }

        boolean manuallyInsertMonitorEnter =
            sootMethod.isSynchronized()
                && controlFlowGraph.getStartingStmtBlock().equals(currentBlock)
                && stmts.stream().noneMatch(Stmt::isJEnterMonitorStmt);
        boolean manuallyInsertMonitorExit =
            sootMethod.isSynchronized()
                && controlFlowGraph.getTailStmtBlocks().contains(currentBlock)
                && stmts.stream().noneMatch(Stmt::isJExitMonitorStmt);

        for (int i = 0; i < stmts.size(); i++) {
          Stmt stmt = stmts.get(i);
          log.info("Process stmt: {}", stmt);

          // insert a monitor-enter if method is synchronized
          // and method does not already begin with a JEnterMonitorStmt
          if (manuallyInsertMonitorEnter && !(stmt instanceof JIdentityStmt)) {
            Local t = sootMethod.getBody().getThisLocal();
            Register register = registerAllocator.getRegisterForImmediate(t, false, stmt);
            this.addInstruction(new Instruction11x(Opcode.MONITOR_ENTER, register), stmt);
            manuallyInsertMonitorEnter = false;
          }

          // insert a monitor-exit if method is synchronized
          // and tailing block does not already end with a JExitMonitorStmt
          if (manuallyInsertMonitorExit && i == stmts.size() - 1) {
            Local t = sootMethod.getBody().getThisLocal();
            Register register = registerAllocator.getRegisterForImmediate(t, false, stmt);
            this.addInstruction(new Instruction11x(Opcode.MONITOR_EXIT, register), stmt);
          }

          // Dex allows only one move-exception at the beginning of each catch-block
          // do not process further move-exception instructions
          // but adjust registerMap
          if (i > 0
              && stmt instanceof JIdentityStmt jIdentityStmt
              && jIdentityStmt.getRightOp() instanceof JCaughtExceptionRef
              && stmts.get(0) instanceof JIdentityStmt previousIdentityStmt
              && previousIdentityStmt.getRightOp() instanceof JCaughtExceptionRef) {
            Register previousRegister =
                registerAllocator.getRegisterForImmediate(
                    previousIdentityStmt.getLeftOp(), false, stmt);
            Local newLocal = jIdentityStmt.getLeftOp();
            registerAllocator.insertIntoRegisterMap(newLocal, previousRegister);
            continue;
          }

          // ensure that return stmts return a type equal to the method return type
          if (stmt instanceof JReturnStmt jReturnStmt) {
            Type returnType = sootMethod.getReturnType();
            if (!(returnType instanceof PrimitiveType)) {
              Register register =
                  registerAllocator.getRegisterForImmediate(
                      jReturnStmt.getOp(), false, jReturnStmt);
              // if (register.getType() != returnType || register.isTypeGuessed()) {
              TypeReference castTypeReference =
                  new ImmutableTypeReference(DexUtil.toDexType(returnType));
              dexStmtVisitor.addInstruction(
                  new Instruction21c(Opcode.CHECK_CAST, register, castTypeReference), jReturnStmt);
              // }
            }
          }

          // process stmt via stmtVisitor
          if (stmt instanceof JAssignStmt jAssignStmt
              && jAssignStmt.getRightOp() instanceof JNewArrayExpr) {
            stmts = dexStmtVisitor.newArrayInit(jAssignStmt, stmts);
          } else {
            stmt.accept(dexStmtVisitor);
          }
        }

        // save the register map of current block
        log.info("Locals at end of block:");
        registerAllocator
            .getRegisterMap()
            .forEach(
                (key, value) ->
                    log.info("{}:{}:{}", key.getName(), value.getNumber(), value.getType()));
        blockRegisterMapAtEnd.put(currentBlock, registerAllocator.getRegisterMap());
        registerAllocator.resetRegisterMap();

        // Add successors of the current block to the worklist
        List<? extends BasicBlock<?>> successors = currentBlock.getSuccessors();
        if (!successors.isEmpty()) {
          // if the first successor has already been processed -> add a goto instruction
          if (!(currentBlock.getStmts().get(currentBlock.getStmtCount() - 1) instanceof JGotoStmt)
              && visitedBlocks.contains(successors.get(0))) {
            this.addInstruction(new Instruction10t(Opcode.GOTO, successors.get(0).getHead()), null);
          }

          // if the last statement of the current block is a goto instruction
          // -> add all successors to the end of the worklist
          // else -> add the successors to the beginning of the worklist
          if (currentBlock.getStmts().get(currentBlock.getStmtCount() - 1) instanceof JGotoStmt) {
            for (BasicBlock<? extends BasicBlock<?>> b : successors) {
              if (!(visitedBlocks.contains(b) || inWorklist.contains(b))) {
                worklist.addLast(b);
                inWorklist.add(b);
              }
              if (blockRegisterMapAtStart.containsKey(b) && visitedBlocks.contains(b)) {
                mergeCurrentRegisterMapToSuccessor(
                    currentBlock, b, blockRegisterMapAtStart, blockRegisterMapAtEnd);
              }
            }
          } else {
            for (int i = successors.size() - 1; i >= 0; i--) {
              var block = successors.get(i);
              if (!visitedBlocks.contains(block)) {
                if (inWorklist.remove(block)) {
                  worklist.remove(block);
                }
                worklist.addFirst(block);
                inWorklist.add(block);
              }

              if (blockRegisterMapAtStart.containsKey(block) && visitedBlocks.contains(block)) {
                mergeCurrentRegisterMapToSuccessor(
                    currentBlock, block, blockRegisterMapAtStart, blockRegisterMapAtEnd);
              }
            }
          }
        }

        // Add exceptional successors of the current block to the worklist
        if (!currentBlock.getExceptionalSuccessors().isEmpty()) {
          List<Stmt> finalStmts = stmts;
          currentBlock
              .getExceptionalSuccessors()
              .forEach(
                  (e, b) -> {
                    TryBlockStmts tryBlockStmts = new TryBlockStmts();
                    tryBlockStmts.jimpleExceptionType = e.getFullyQualifiedName().matches(".*\\$\\d+$")
                            //the current apk.frontend makes each Exception class unique by adding a $<number> to the class name
                            //if this is the case, remove the number in oder to get the real fully qualified class name
                            ? e.getFullyQualifiedName().substring(0, e.getFullyQualifiedName().lastIndexOf('$'))
                            : e.getFullyQualifiedName();
                    tryBlockStmts.startStmt = currentBlock.getHead();
                    tryBlockStmts.endStmt = finalStmts.get(finalStmts.size() - 1);
                    tryBlockStmts.catchStmt = b.getHead();
                    tryBlocks.add(tryBlockStmts);
                    if (!(visitedBlocks.contains(b) || inWorklist.contains(b))) {
                      worklist.addLast(b);
                      inWorklist.add(b);
                    }
                    if (blockRegisterMapAtEnd.containsKey(b) && visitedBlocks.contains(b)) {
                      log.info("Merge current register map to successor");
                      mergeCurrentRegisterMapToSuccessor(
                          currentBlock, b, blockRegisterMapAtStart, blockRegisterMapAtEnd);
                    }
                  });
        }
      }

      int parameterSizeCount =
          DexUtil.getRegisterSizeCount(sootMethod.getParameterTypes())
              + (sootMethod.isStatic() ? 0 : 1);

      int registerCount =
          registerAllocator.getRegisterCount() > 16
              ? registerAllocator.getRegisterCount() + 16
              : registerAllocator.getRegisterCount();

      MethodImplementationBuilder methodImplementationBuilder =
          new MethodImplementationBuilder(Math.max(registerCount, parameterSizeCount));

      LabelAssigner labelAssigner = new LabelAssigner(methodImplementationBuilder);

      this.addBuilderInstructions(
          methodImplementationBuilder, labelAssigner, blockRegisterMapAtStart);

      labelAssigner.areLabelsNotYetPlaced();

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

    // Consider locals that are included in all predecessors
    Set<Local> locals =
        registerMaps.stream()
            .filter(registerMap -> !registerMap.isEmpty())
            .map(Map::keySet)
            .reduce(
                (set1, set2) -> {
                  Set<Local> intersection = new HashSet<>(set1);
                  intersection.retainAll(set2);
                  return intersection;
                })
            .orElse(Collections.emptySet());

    HashMap<Local, Register> result = new HashMap<>();

    for (Local local : locals) {

      if (local.getName().equals("$stack")) {
        continue;
      }

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

      /*if (regs.stream()
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
      }*/

      log.info("Merge local {} with type {}", local.getName(), type);

      if (first.getType().equals(view.getIdentifierFactory().getClassType("java.lang.Object"))) {
        first.setType(type);
      }
      result.put(local, first);

      // move all previous register to a common register
      for (BasicBlock<?> block : previousBlocks) {
        HashMap<Local, Register> localRegisterMap = blockRegisterMap.get(block);
        if (localRegisterMap == null) {
          log.info("local register map is null");
          continue;
        }
        Register old = localRegisterMap.get(local);
        if (old != null && !first.equals(old)) {
          var i = instructions.get(block);
          AbstractInstruction move = generateMoveInstruction(first, old, old.getType());
          if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
              || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
            if (i.size() > 1
                && (i.get(i.size() - 2).getOpcode().name.startsWith("packed-switch")
                    || i.get(i.size() - 2).getOpcode().name.startsWith("sparse-switch"))) {
              i.add(i.size() - 2, move);
            } else {
              i.add(i.size() - 1, move);
            }
          } else {
            i.add(move);
          }
        }
      }
    }

    return result;
  }

  private void mergeCurrentRegisterMapToSuccessor(
      BasicBlock<?> cBlock,
      BasicBlock<?> successorBlock,
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMapStart,
      Map<BasicBlock<?>, HashMap<Local, Register>> blockRegisterMapEnd) {

    HashMap<Local, Register> registerMapCurrent = blockRegisterMapEnd.get(cBlock);
    HashMap<Local, Register> registerMapSuccessor = blockRegisterMapStart.get(successorBlock);

    Set<Local> locals = registerMapCurrent.keySet();

    for (Local local : locals) {

      if (local.getName().equals("$stack")) {
        continue;
      }

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
          // log.error("Cannot merge {}", local);
          // continue;
        }
      }

      if (successorType.equals(view.getIdentifierFactory().getType("java.lang.Object"))) {
        registerSuccessor.setType(type);
      }

      // a local has different registers but same type --> move previous registers to a new register
      var i = instructions.get(currentBlock);
      AbstractInstruction move =
          generateMoveInstruction(registerSuccessor, register, register.getType());
      if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
          || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
        if (i.size() > 1
            && (i.get(i.size() - 2).getOpcode().name.startsWith("packed-switch")
                || i.get(i.size() - 2).getOpcode().name.startsWith("sparse-switch"))) {
          i.add(i.size() - 2, move);
        } else {
          i.add(i.size() - 1, move);
        }
      } else {
        i.add(move);
      }
    }
  }

  // Remove casts of p0 (this) until it is initialized
  private List<Stmt> fixInitMethod(List<Stmt> stmts) {
    stmts = new ArrayList<>(stmts);
    Local thisVariable = null;
    LinkedHashSet<Local> thisReferences = new LinkedHashSet<>();
    int targetIndex = -1;
    for (int i = 0; i < stmts.size(); i++) {
      Stmt stmt = stmts.get(i);
      if (stmt instanceof JIdentityStmt jIdentityStmt
          && jIdentityStmt.getRightOp() instanceof JThisRef) {
        thisVariable = jIdentityStmt.getLeftOp();
        thisReferences.add(thisVariable);
      } else if (stmt instanceof JAssignStmt jAssignStmt) {
        if (jAssignStmt.getRightOp() instanceof Local local && thisReferences.contains(local)) {
          thisReferences.add((Local) jAssignStmt.getLeftOp());
        } else if (jAssignStmt.getRightOp() instanceof JCastExpr jCastExpr
            && thisReferences.contains(jCastExpr.getOp())) {
          thisReferences.add((Local) jAssignStmt.getLeftOp());
        }
      }

      if ((stmt instanceof JInvokeStmt jInvokeStmt
              && jInvokeStmt.getInvokeExpr().isPresent()
              && jInvokeStmt.getInvokeExpr().get() instanceof JSpecialInvokeExpr expr
              && thisReferences.contains(expr.getBase())
              && expr.getMethodSignature().getName().equals("<init>"))
          || (stmt instanceof JAssignStmt jAssignStmt
              && jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr expr2
              && thisReferences.contains(expr2.getBase())
              && expr2.getMethodSignature().getName().equals("<init>"))) {
        targetIndex = i;
        break;
      }
    }

    if (targetIndex > 0) {

      Immediate finalThisVariable = thisVariable;
      for (int i = Math.min(targetIndex - 1, stmts.size() - 1); i >= 0; i--) {
        Stmt stmt = stmts.get(i);
        if (!(stmt instanceof JIdentityStmt)
            && !(stmt instanceof JInvokeStmt)
            && stmt.getUses().anyMatch(value -> value.equals(finalThisVariable))) {

          if (stmt instanceof JAssignStmt jAssignStmt) {
            if (jAssignStmt.getRightOp() instanceof JCastExpr jCastExpr) {
              JAssignStmt jAssignStmt1 =
                  new JAssignStmt(
                      jAssignStmt.getLeftOp(), jCastExpr.getOp(), jAssignStmt.getPositionInfo());
              stmts.set(i, jAssignStmt1);
            }
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
    log.info("Reserve labels for payloads");
    LinkedHashMap<AbstractPayload, BuilderInstruction> payloadInstructions = new LinkedHashMap<>();
    for (AbstractPayload payload : payloads) {
      payload.setLabelAssigner(labelAssigner);
      payloadInstructions.put(payload, payload.getBuilderInstruction());
    }
    log.info("Reserve labels for jumps");
    for (AbstractInstruction ins : instructions.values().stream().flatMap(List::stream).toList()) {
      if (ins instanceof Instruction10t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      } else if (ins instanceof Instruction21t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      } else if (ins instanceof Instruction22t instruction) {
        labelAssigner.getOrCreateLabel(instruction.getTargetStmt());
      }
    }
    log.info("Reserve labels for try blocks");
    tryBlocks.forEach(
        b -> {
          labelAssigner.getOrCreateLabel(b.startStmt);
          labelAssigner.getOrCreateLabelAfterStmt(b.endStmt);
          labelAssigner.getOrCreateLabel(b.catchStmt);
        });
    log.info("Process instructions");

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
        log.info("Original stmt: {}", instructionMap.get(instruction));

        Stmt currentStmt = instructionMap.get(instruction);

        if (labelAssigner.hasLabel(currentStmt)) {
          labelAssigner.setLabel(currentStmt);
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
              if ((!(instruction.getOpcode().name.contains("get")) || index > 0)
                  && !(instruction.getOpcode().name.startsWith("move"))) {
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

            if (instruction.getOpcode().name.startsWith("throw")
                || instruction.getOpcode().name.startsWith("return")
                || instruction.getOpcode().name.startsWith("if")
                || instruction.getOpcode().name.startsWith("packed-switch")
                || instruction.getOpcode().name.startsWith("sparse-switch")) {
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

            if (instruction.getOpcode().name.startsWith("move-object")) {
              instruction =
                  generateMoveInstructionObject(
                      instruction.getRegisters().get(0), instruction.getRegisters().get(1));
            } else if (instruction.getOpcode().name.startsWith("move-wide")) {
              instruction =
                  generateMoveInstructionWide(
                      instruction.getRegisters().get(0), instruction.getRegisters().get(1));
            } else {
              instruction =
                  generateMoveInstructionDefault(
                      instruction.getRegisters().get(0), instruction.getRegisters().get(1));
            }
            log.info("New instruction type {}", instruction.getRegisters().get(0).getType());
            log.info("New instruction type {}", instruction.getRegisters().get(1).getType());
            log.info("New instruction opcode {}", instruction.getOpcode());
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

        if (i + 1 >= instructionsOfBlock.size()
            || currentStmt != instructionMap.get(instructionsOfBlock.get(i + 1))) {
          log.info("test end label");
          if (labelAssigner.hasLabelAfterStmt(currentStmt)) {
            log.info("Has label true");
            labelAssigner.setLabelAfterStmt(currentStmt);
          }
        }
      }
    }

    // add payloads
    payloadInstructions.forEach(
        (payload, builderInstruction) -> {
          labelAssigner.setLabel(payload);
          payload.logSmali();
          builderInstructions.add(builderInstruction);
          methodImplementationBuilder.addInstruction(builderInstruction);
        });

    // add catch
    tryBlocks.forEach(
        b -> {
          if (b.jimpleExceptionType.equals("java.lang.Throwable$CatchAll")) {
            methodImplementationBuilder.addCatch(
                labelAssigner.getOrCreateLabel(b.startStmt),
                labelAssigner.getOrCreateLabelAfterStmt(b.endStmt),
                labelAssigner.getOrCreateLabel(b.catchStmt));
          } else {
            methodImplementationBuilder.addCatch(
                new ImmutableTypeReference(DexUtil.toDexClassName(b.jimpleExceptionType)),
                labelAssigner.getOrCreateLabel(b.startStmt),
                labelAssigner.getOrCreateLabelAfterStmt(b.endStmt),
                labelAssigner.getOrCreateLabel(b.catchStmt));
          }
        });

    log.info("Builder instructions created");

    return builderInstructions;
  }

  protected AbstractInstruction generateMoveInstruction(
      Register targetR, Register sourceRegister, Type valueType) {

    if (valueType instanceof ReferenceType) {
      return generateMoveInstructionObject(targetR, sourceRegister);
    } else if (DexUtil.isWide(valueType)) {
      return generateMoveInstructionWide(targetR, sourceRegister);
    } else {
      return generateMoveInstructionDefault(targetR, sourceRegister);
    }
  }

  protected AbstractInstruction generateMoveInstructionObject(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE_OBJECT, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_OBJECT_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_OBJECT_16, targetR, sourceRegister);
    }
  }

  protected AbstractInstruction generateMoveInstructionWide(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE_WIDE, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_WIDE_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_WIDE_16, targetR, sourceRegister);
    }
  }

  protected AbstractInstruction generateMoveInstructionDefault(
      Register targetR, Register sourceRegister) {
    if (sourceRegister.is4BitRegister() && targetR.is4BitRegister()) {
      return new Instruction12x(Opcode.MOVE, targetR, sourceRegister);
    } else if (sourceRegister.is8BitRegister() && targetR.is8BitRegister()) {
      return new Instruction22x(Opcode.MOVE_FROM16, targetR, sourceRegister);
    } else {
      return new Instruction32x(Opcode.MOVE_16, targetR, sourceRegister);
    }
  }

  protected void addInstruction(AbstractInstruction instruction, Stmt stmt) {
    if (stmt != null) {
      log.info(
          "Add instruction {} of stmt {} to block {}", instruction.getOpcode(), stmt, currentBlock);
    } else {
      log.info(
          "Add instruction {} of stmt null to block {}", instruction.getOpcode(), currentBlock);
    }

    instructionMap.put(instruction, stmt);
    instructions.computeIfAbsent(currentBlock, k -> new ArrayList<>()).add(instruction);
  }

  public void addPayload(AbstractPayload payload) {
    this.payloads.add(payload);
  }

  public void setRegisterAllocator(RegisterAllocator registerAllocator) {
    this.registerAllocator = registerAllocator;
  }

  public void setCurrentBlock(BasicBlock<?> block) {
    this.currentBlock = block;
  }
}
