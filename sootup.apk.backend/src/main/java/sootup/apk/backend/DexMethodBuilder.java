package sootup.apk.backend;

import static sootup.apk.backend.Constants.JIMPLE_OBJECT_TYPE;

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
        && !methodName.equals(Constants.DEX_INIT_METHOD)
        && !methodName.equals(Constants.DEX_CLINIT_METHOD)) {
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
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtStart = new HashMap<>();
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtEnd = new HashMap<>();

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
          Map<Local, Register> merged =
              mergeIncomingRegisterMaps(
                  new HashSet<>(predecessors),
                  blockRegisterMapAtEnd,
                  controlFlowGraph,
                  currentBlock);
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
        if (sootMethod.getName().equals(Constants.DEX_INIT_METHOD)) {
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
          dexStmtVisitor.newStmt();

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
                    tryBlockStmts.jimpleExceptionType =
                        e.getFullyQualifiedName().matches(".*\\$\\d+$")
                            // the current apk.frontend makes each Exception class unique by adding
                            // a $<number> to the class name
                            // if this is the case, remove the number in oder to get the real fully
                            // qualified class name
                            ? e.getFullyQualifiedName()
                                .substring(0, e.getFullyQualifiedName().lastIndexOf('$'))
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
  private Map<Local, Register> mergeIncomingRegisterMaps(
      Set<BasicBlock<?>> previousBlocks,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtEnd,
      ControlFlowGraph<?> controlFlowGraph,
      BasicBlock<?> newBlock) {

    Set<Map<Local, Register>> registerMaps =
        previousBlocks.stream()
            .map(blockRegisterMapAtEnd::get)
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

      if (local.getName().equals(Constants.JIMPLE_STACK_LOCAL)) {
        continue;
      }

      // if local is not used anywhere later in the control flow graph --> continue
      if (!isUsedLater(controlFlowGraph, newBlock.getHead(), local)) {
        continue;
      }

      List<Register> regs =
          registerMaps.stream().map(m -> m.get(local)).filter(Objects::nonNull).toList();

      // if a local has the same register in all registerMaps, there is nothing to do
      if (regs.stream().allMatch(r -> r.equals(regs.get(0)))) {
        result.put(local, regs.get(0));
        continue;
      }

      // a local has different types in different registerMap
      Type objectType = view.getIdentifierFactory().getClassType(JIMPLE_OBJECT_TYPE);

      Register newRegister =
          regs.stream()
              .filter(
                  reg ->
                      !reg.getType().equals(PrimitiveType.getInt())
                          && !reg.getType().equals(objectType))
              .findFirst()
              .orElseGet(
                  () -> {
                    List<Register> intRegisters =
                        regs.stream()
                            .filter(reg -> reg.getType().equals(PrimitiveType.getInt()))
                            .toList();
                    if (!intRegisters.isEmpty()
                        && intRegisters.stream().allMatch(Register::isPotentialNullValue)) {
                      return regs.stream()
                          .filter(reg -> reg.getType().equals(objectType))
                          .findFirst()
                          .orElseGet(() -> intRegisters.stream().findFirst().orElseThrow());
                    }
                    return intRegisters.stream()
                        .findFirst()
                        .orElseGet(
                            () ->
                                regs.stream()
                                    .filter(reg -> reg.getType().equals(objectType))
                                    .findFirst()
                                    .orElseThrow());
                  });

      log.info("Merge local {} with type {}", local.getName(), newRegister.getType());

      result.put(local, newRegister);

      // move all previous register to a common register
      for (BasicBlock<?> block : previousBlocks) {
        Map<Local, Register> localRegisterMap = blockRegisterMapAtEnd.get(block);
        if (localRegisterMap == null) {
          log.info("local register map is null");
          continue;
        }
        Register old = localRegisterMap.get(local);
        if (old != null && !newRegister.equals(old)) {
          var i = instructions.get(block);
          // change0ToNull(local, old, newRegister.getType(), block, blockRegisterMapAtStart);
          AbstractInstruction moveInstruction;
          if (old.isPotentialNullValue() && !(newRegister.getType() instanceof PrimitiveType)) {
            moveInstruction = new Instruction11n(Opcode.CONST_4, newRegister, 0);
          } else {
            moveInstruction = generateMoveInstructionToSuccessor(old, newRegister);
          }
          if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
              || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
            if (i.size() > 1
                && (i.get(i.size() - 2).getOpcode().equals(Opcode.PACKED_SWITCH)
                    || i.get(i.size() - 2).getOpcode().equals(Opcode.SPARSE_SWITCH))) {
              i.add(i.size() - 2, moveInstruction);
            } else {
              i.add(i.size() - 1, moveInstruction);
            }
          } else {
            i.add(moveInstruction);
          }
        }
      }
    }

    return result;
  }

  boolean isUsedLater(ControlFlowGraph<?> controlFlowGraph, Stmt start, Local variable) {
    Set<Stmt> visited = new HashSet<>();
    Deque<Stmt> worklist = new ArrayDeque<>();
    worklist.add(start);
    worklist.addAll(controlFlowGraph.getAllSuccessors(start));

    while (!worklist.isEmpty()) {
      Stmt stmt = worklist.removeFirst();

      if (!visited.add(stmt)) {
        continue;
      }

      if (stmt.getUses().anyMatch(variable::equals)) {
        return true;
      }

      worklist.addAll(controlFlowGraph.getAllSuccessors(stmt));
    }

    return false;
  }

  private void mergeCurrentRegisterMapToSuccessor(
      BasicBlock<?> cBlock,
      BasicBlock<?> successorBlock,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapStart,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapEnd) {

    Map<Local, Register> registerMapCurrent = blockRegisterMapEnd.get(cBlock);
    Map<Local, Register> registerMapSuccessor = blockRegisterMapStart.get(successorBlock);

    Set<Local> locals = registerMapCurrent.keySet();

    for (Local local : locals) {

      if (local.getName().equals(Constants.JIMPLE_STACK_LOCAL)) {
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

      if (!type.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))
          && !successorType.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))
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

      if (successorType.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))) {
        registerSuccessor.setType(type);
      }

      // Successor register is currently handled as int = 0. However, it turned out to be Object
      // null
      if (registerSuccessor.isPotentialNullValue()
          && !(register.getType() instanceof PrimitiveType)) {
        Register newRegister = registerAllocator.getRegisterForType(register.getType());
        newRegister.setNumber(registerSuccessor.getNumber());
        newRegister.setIsPotentialNullValue(true);
        AbstractInstruction instruction = new Instruction11n(Opcode.CONST_4, newRegister, 0);
        List<BasicBlock<?>> predecessors = new ArrayList<>();
        predecessors.addAll(successorBlock.getPredecessors());
        predecessors.addAll(successorBlock.getExceptionalPredecessors().values());
        for (BasicBlock<?> p : predecessors) {
          var i = instructions.get(p);
          if (i != null && !i.isEmpty()) {
            if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
                || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
              if (i.size() > 1
                  && (i.get(i.size() - 2).getOpcode().equals(Opcode.PACKED_SWITCH)
                      || i.get(i.size() - 2).getOpcode().equals(Opcode.SPARSE_SWITCH))) {
                i.add(i.size() - 2, instruction);
              } else {
                i.add(i.size() - 1, instruction);
              }
            } else {
              i.add(instruction);
            }
          }
          registerSuccessor.setType(register.getType());
          changeFollowingRegistersToNewType(
              successorBlock,
              local,
              registerSuccessor,
              null,
              blockRegisterMapStart,
              blockRegisterMapEnd);
        }
      }

      // a local has different registers but same type --> move previous registers to a new register
      var i = instructions.get(currentBlock);
      AbstractInstruction moveInstruction =
          generateMoveInstructionToSuccessor(register, registerSuccessor);
      if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
          || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
        if (i.size() > 1
            && (i.get(i.size() - 2).getOpcode().equals(Opcode.PACKED_SWITCH)
                || i.get(i.size() - 2).getOpcode().equals(Opcode.SPARSE_SWITCH))) {
          i.add(i.size() - 2, moveInstruction);
        } else {
          i.add(i.size() - 1, moveInstruction);
        }
      } else {
        i.add(moveInstruction);
      }
    }
  }

  public void changeFollowingRegistersToNewType(
      BasicBlock<?> currentBlock,
      Local local,
      Register newRegister,
      AbstractInstruction startAtIns,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapStart,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapEnd) {

    Queue<BasicBlock<?>> successorsToChange = new LinkedList<>();
    List<BasicBlock<?>> successorsChanged = new ArrayList<>();
    successorsToChange.add(currentBlock);

    outerLoop:
    while (!successorsToChange.isEmpty()) {
      BasicBlock<?> succ = successorsToChange.poll();

      if (successorsChanged.contains(succ)) {
        continue;
      }
      successorsChanged.add(succ);

      if (startAtIns != null && local != null) {
        blockRegisterMapStart.get(succ).put(local, newRegister);
      }

      boolean start = startAtIns == null;

      List<AbstractInstruction> ins = instructions.get(succ);
      ListIterator<AbstractInstruction> iterator = ins.listIterator();
      while (iterator.hasNext()) {
        AbstractInstruction in = iterator.next();
        if (!start && in.equals(startAtIns)) {
          start = true;
        }
        if (!start) {
          continue;
        }
        if (in.getDefRegisters().contains(newRegister)) {
          continue outerLoop;
        }
        if (in.getRegisters().contains(newRegister)) {
          in.changeRegister(newRegister, newRegister);
        }
        if (in.getOpcode().name.toLowerCase().startsWith("move")
            && in.getUseRegisters().contains(newRegister)) {
          List<Register> reg = in.getRegisters();
          Register targetRegister = reg.get(0);
          targetRegister.setType(newRegister.getType());
          AbstractInstruction moveInstruction =
              generateMoveInstruction(targetRegister, newRegister, newRegister.getType());
          iterator.set(moveInstruction);
          changeFollowingRegistersToNewType(
              currentBlock, null, targetRegister, in, blockRegisterMapStart, blockRegisterMapEnd);
        }
      }
      if (local != null) {
        if (blockRegisterMapEnd.get(succ).get(local).equals(newRegister)) {
          blockRegisterMapEnd.get(succ).put(local, newRegister);
          if (instructions != null && !instructions.isEmpty()) {
            for (var successor : succ.getSuccessors()) {
              if (blockRegisterMapStart.get(successor) != null
                  && (blockRegisterMapStart.get(successor).containsKey(local)
                      || blockRegisterMapStart.get(successor).get(local).equals(newRegister))) {
                successorsToChange.add(successor);
              }
            }
            for (var successor : succ.getExceptionalSuccessors().values()) {
              if (blockRegisterMapStart.get(successor) != null
                  && (blockRegisterMapStart.get(successor).containsKey(local)
                      || blockRegisterMapStart.get(successor).get(local).equals(newRegister))) {
                successorsToChange.add(successor);
              }
            }
          }
        }
      } else if (blockRegisterMapEnd.get(currentBlock).containsValue(newRegister)) {
        for (var successor : succ.getSuccessors()) {
          if (blockRegisterMapStart.get(successor).containsValue(newRegister)) {
            successorsToChange.add(successor);
          }
        }
        for (var successor : succ.getExceptionalSuccessors().values()) {
          if (blockRegisterMapStart.get(successor).containsValue(newRegister)) {
            successorsToChange.add(successor);
          }
        }
      }
    }
  }

  private AbstractInstruction generateMoveInstructionToSuccessor(
      Register previous, Register target) {
    if (target.getType() instanceof PrimitiveType targetType
        && previous.getType() instanceof PrimitiveType sourceType
        && target.getType() != previous.getType()) {
      if (DexUtil.isTypeSmaller(sourceType, PrimitiveType.getInt())) {
        sourceType = PrimitiveType.getInt();
      }
      if (targetType == PrimitiveType.getBoolean()) {
        targetType = PrimitiveType.getInt();
      }
      if (sourceType.equals(targetType)) {
        return generateMoveInstruction(target, previous, previous.getType());
      } else {
        Opcode opcode =
            Opcode.valueOf(
                sourceType.getName().toUpperCase() + "_TO_" + targetType.getName().toUpperCase());
        return new Instruction12x(opcode, target, previous);
      }

    } else {
      return generateMoveInstruction(target, previous, previous.getType());
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
              && expr.getMethodSignature().getName().equals(Constants.DEX_INIT_METHOD))
          || (stmt instanceof JAssignStmt jAssignStmt
              && jAssignStmt.getRightOp() instanceof JSpecialInvokeExpr expr2
              && thisReferences.contains(expr2.getBase())
              && expr2.getMethodSignature().getName().equals(Constants.DEX_INIT_METHOD))) {
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
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMap) {

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
      Set<Integer> usedRegisterNumbers =
          blockRegisterMap.getOrDefault(currentBlock, new HashMap<>()).values().stream()
              .map(Register::getNumber)
              .collect(Collectors.toSet());
      log.info("Locals at start of block:");
      blockRegisterMap
          .getOrDefault(currentBlock, new HashMap<>())
          .forEach(
              (key, value) ->
                  log.info("{}:{}:{}", key.getName(), value.getNumber(), value.getType()));
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
                  && instruction.getOpcode().equals(Opcode.CHECK_CAST))) {

            LinkedHashMap<Register, Register> registerHashMap = new LinkedHashMap<>();

            log.info("Tmp registers needed: {}", registers.size());
            for (int index = 0; index < registers.size(); index++) {
              Register r = registers.get(index);
              log.info("Register {} with type {}", r.getNumber(), r.getType());
              log.info("Tmp index: {}", tmpIndx);
              Register tmpRegister = tmpRegisters.get(tmpIndx);
              tmpRegister.setType(r.getType());
              tmpRegister.setIsTypeGuessed(r.isTypeGuessed());
              instruction.changeRegister(r, tmpRegister);
              if ((!(instruction.getOpcode().name.contains("get")) || index > 0)
                  && !(instruction.getOpcode().name.startsWith("move"))) {
                if (usedRegisterNumbers.contains(r.getNumber()) || r.isParameter()) {
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
            usedRegisterNumbers.addAll(
                instruction.getRegisters().stream()
                    .map(Register::getNumber)
                    .collect(Collectors.toSet()));

            if (instruction.getOpcode().name.startsWith("throw")
                || instruction.getOpcode().name.startsWith("return")
                || instruction.getOpcode().name.startsWith("if")
                || instruction.getOpcode().equals(Opcode.PACKED_SWITCH)
                || instruction.getOpcode().equals(Opcode.SPARSE_SWITCH)) {
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
                  usedRegisterNumbers.addAll(
                      move.getRegisters().stream()
                          .map(Register::getNumber)
                          .collect(Collectors.toSet()));
                });
            usedRegisterNumbers.addAll(
                registerHashMap.values().stream()
                    .map(Register::getNumber)
                    .collect(Collectors.toSet()));

          } else if (instruction instanceof Instruction21c instruction21c
              && instruction.getOpcode().equals(Opcode.CHECK_CAST)) {
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
            usedRegisterNumbers.addAll(
                instruction.getRegisters().stream()
                    .map(Register::getNumber)
                    .collect(Collectors.toSet()));

            TypeReference typeReference = (TypeReference) instruction21c.getReference();
            AbstractInstruction move2 =
                generateMoveInstruction(
                    originalRegister,
                    tmpRegister,
                    sootup.apk.frontend.Util.DexUtil.toSootType(typeReference.getType(), 0));
            builderInstructions.add(move2.getBuilderInstruction());
            methodImplementationBuilder.addInstruction(move2.getBuilderInstruction());
            usedRegisterNumbers.addAll(
                move2.getRegisters().stream().map(Register::getNumber).collect(Collectors.toSet()));

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
            usedRegisterNumbers.addAll(
                instruction.getRegisters().stream()
                    .map(Register::getNumber)
                    .collect(Collectors.toSet()));
          } else {
            builderInstructions.add(instruction.getBuilderInstruction());
            methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
            usedRegisterNumbers.addAll(
                instruction.getRegisters().stream()
                    .map(Register::getNumber)
                    .collect(Collectors.toSet()));
          }

        } else {
          builderInstructions.add(instruction.getBuilderInstruction());
          methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());
          usedRegisterNumbers.addAll(
              instruction.getRegisters().stream()
                  .map(Register::getNumber)
                  .collect(Collectors.toSet()));
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
    List<Register> defRegisters = instruction.getDefRegisters();
    for (Register reg : defRegisters) {
      reg.addDef(instruction);
    }
    List<Register> useRegisters = instruction.getUseRegisters();
    for (Register reg : useRegisters) {
      reg.addUse(instruction);
    }
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
