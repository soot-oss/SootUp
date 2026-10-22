package sootup.apk.backend;

import static sootup.apk.backend.Constants.JIMPLE_OBJECT_TYPE;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.*;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableTypeEncodedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.instructions.*;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.types.*;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootMethod;

public class DexMethodBuilder {

  private static final Logger log = LoggerFactory.getLogger(DexMethodBuilder.class);
  private View view;

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

  /**
   * Creates a dex ImmutableMethod for a SootMethod
   * @param sootMethod The SootUp method
   * @return The dex method
   */
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
        Set<Annotation> annotations = null; // TODO (not yet represented in SootUp?)
        String parameterName = null; // TODO (not yet represented in SootUp?)
        parameters.add(new ImmutableMethodParameter(dexParamType, annotations, parameterName));
      }
    }

    Type returnType = sootMethod.getReturnType();
    String dexReturnType = DexUtil.toDexType(returnType);

    Set<Annotation> annotations = createMethodAnnotations(sootMethod);

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

  /**
   * Creates dex method annotations
   *
   * @param m The jimple method
   * @return A list of annotations of this method
   */
  private Set<Annotation> createMethodAnnotations(SootMethod m) {
    Set<Annotation> annotations = new HashSet<>();
    if (m instanceof JavaSootMethod javaSootMethod) {
      var a = javaSootMethod.getAnnotations();
      for (var annotation : a) {
        List<AnnotationElement> annotationElements = new ArrayList<>();
        for (var entry : annotation.getValues().entrySet()) {
          AnnotationElement annotationElement =
              new ImmutableAnnotationElement(
                  entry.getKey(), DexUtil.buildEncodedValueForAnnotation(entry.getValue()));
          annotationElements.add(annotationElement);
        }
        ImmutableAnnotation ann =
            new ImmutableAnnotation(
                annotation.getVisibility() == AnnotationUsage.AnnotationUsageVisibility.RUNTIME
                    ? AnnotationVisibility.RUNTIME
                    : AnnotationVisibility.BUILD,
                DexUtil.toDexClassName(annotation.getAnnotation().getFullyQualifiedName()),
                annotationElements);
        annotations.add(ann);
      }
    }

    List<ClassType> exceptionSignatures = m.getExceptionSignatures();
    if (!exceptionSignatures.isEmpty()) {
      List<ImmutableEncodedValue> valueList = new ArrayList<>(exceptionSignatures.size());
      for (ClassType exceptionClass : exceptionSignatures) {
        valueList.add(
            new ImmutableTypeEncodedValue(
                DexUtil.toDexClassName(exceptionClass.getFullyQualifiedName())));
      }
      ImmutableArrayEncodedValue valueValue = new ImmutableArrayEncodedValue(valueList);
      ImmutableAnnotationElement valueElement = new ImmutableAnnotationElement("value", valueValue);
      Set<ImmutableAnnotationElement> elements = Collections.singleton(valueElement);
      ImmutableAnnotation ann =
          new ImmutableAnnotation(
              AnnotationVisibility.SYSTEM, "Ldalvik/annotation/Throws;", elements);
      annotations.add(ann);
    }

    return annotations;
  }

  /**
   * Creates a dex MethodImplementation for a SootMethod
   * @param sootMethod The SootMethod
   * @return The dex MethodImplementation
   */
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

      Deque<BasicBlock<?>> worklist =
          new ArrayDeque<>(List.of(controlFlowGraph.getStartingStmtBlock()));
      Set<BasicBlock<?>> inWorklist =
          new HashSet<>(List.of(controlFlowGraph.getStartingStmtBlock()));
      Set<BasicBlock<?>> visitedBlocks = new HashSet<>();
      Map<BasicBlock<?>, Map<Local, Register>> registerMapAtStartOfBlock = new HashMap<>();
      Map<BasicBlock<?>, Map<Local, Register>> registerMapAtEndOfBlock = new HashMap<>();
      Map<BasicBlock<?>, Map<Local, Register>> registerMapAtEndOfBlockExceptional = new HashMap<>();

      while (!worklist.isEmpty()) {
        currentBlock = worklist.pollFirst();
        if (currentBlock == null) {
          break;
        }
        inWorklist.remove(currentBlock);
        visitedBlocks.add(currentBlock);

        // Merge register maps of previous blocks
        Set<BasicBlock<?>> previousBlocks =
            controlFlowGraph.predecessors(currentBlock.getHead()).stream()
                .map(controlFlowGraph::getBlockOf)
                .collect(Collectors.toSet());

        if (previousBlocks.isEmpty()) {
          registerAllocator.setRegisterMap(new HashMap<>());
        } else {

          List<BasicBlock<?>> predecessors = new ArrayList<>();
          predecessors.addAll(currentBlock.getPredecessors());
          predecessors.addAll(currentBlock.getExceptionalPredecessors().values());
          Map<Local, Register> merged =
              mergeIncomingRegisterMaps(
                  new HashSet<>(predecessors),
                  registerMapAtEndOfBlock,
                  registerMapAtEndOfBlockExceptional,
                  registerMapAtStartOfBlock,
                  controlFlowGraph,
                  currentBlock);
          registerAllocator.setRegisterMap(merged);
          registerMapAtStartOfBlock.put(currentBlock, new HashMap<>(merged));
        }

        List<Stmt> stmts = new ArrayList<>(currentBlock.getStmts());

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
          dexStmtVisitor.newStmt();

          // if the last statement of the block is going to be processed (or the second to last stmt
          // if the last statement is a return stmt)
          // insert the current register map into the registerMapAtEndOfBlockExceptional (as the
          // return value of the last stmt of the block cannot flow to the exceptional successor)
          if (i == stmts.size() - 1
              || (stmts.size() > 1 && i == stmts.size() - 2)
                  && (stmts.get(stmts.size() - 1) instanceof JReturnStmt
                      || stmts.get(stmts.size() - 1) instanceof JReturnVoidStmt)) {
            if (!registerMapAtEndOfBlockExceptional.containsKey(currentBlock)) {
              registerMapAtEndOfBlockExceptional.put(
                  currentBlock, new HashMap<>(registerAllocator.getRegisterMap()));
            }
          }

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
              TypeReference castTypeReference =
                  new ImmutableTypeReference(DexUtil.toDexType(returnType));
              dexStmtVisitor.addInstruction(
                  new Instruction21c(Opcode.CHECK_CAST, register, castTypeReference), jReturnStmt);
            }
          }

          // process stmt via stmtVisitor
          if (stmt instanceof JAssignStmt jAssignStmt
              && jAssignStmt.getRightOp() instanceof JNewArrayExpr) {
            // stmts = dexStmtVisitor.newArrayInit(jAssignStmt, stmts);
            stmt.accept(dexStmtVisitor);
          } else {
            stmt.accept(dexStmtVisitor);
          }
        }

        // save the register map of current block
        registerMapAtEndOfBlock.put(currentBlock, registerAllocator.getRegisterMap());
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
              // if the successor has already been processed -> match the registers of the current
              // block to the successor block
              if (registerMapAtStartOfBlock.containsKey(b) && visitedBlocks.contains(b)) {
                mergeCurrentRegisterMapToSuccessor(
                    currentBlock,
                    b,
                    registerMapAtStartOfBlock,
                    registerMapAtEndOfBlock,
                    registerMapAtEndOfBlockExceptional);
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

              if (registerMapAtStartOfBlock.containsKey(block) && visitedBlocks.contains(block)) {
                mergeCurrentRegisterMapToSuccessor(
                    currentBlock,
                    block,
                    registerMapAtStartOfBlock,
                    registerMapAtEndOfBlock,
                    registerMapAtEndOfBlockExceptional);
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
                            // if this is the case, remove the number in order to get the real fully
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
                    if (registerMapAtEndOfBlock.containsKey(b) && visitedBlocks.contains(b)) {
                      mergeCurrentRegisterMapToSuccessor(
                          currentBlock,
                          b,
                          registerMapAtStartOfBlock,
                          registerMapAtEndOfBlock,
                          registerMapAtEndOfBlockExceptional);
                    }
                  });
        }
      }

      int parameterSizeCount =
          DexUtil.getRegisterSizeCount(sootMethod.getParameterTypes())
              + (sootMethod.isStatic() ? 0 : 1);

      // Adjust the registers to the required instruction formats
      RegisterAssigner registerAssigner = new RegisterAssigner(registerAllocator, this);
      registerAssigner.prepareRegisters(instructions.values(), instructionMap);

      for (Map.Entry<BasicBlock<?>, List<AbstractInstruction>> entry : instructions.entrySet()) {
        List<AbstractInstruction> insns =
            registerAssigner.finishRegs(entry.getValue(), instructionMap);
        entry.setValue(insns);
      }

      int registerCount = registerAllocator.getRegisterCount();

      MethodImplementationBuilder methodImplementationBuilder =
          new MethodImplementationBuilder(Math.max(registerCount, parameterSizeCount));

      LabelAssigner labelAssigner = new LabelAssigner(methodImplementationBuilder);

      this.addBuilderInstructions(methodImplementationBuilder, labelAssigner, dexStmtVisitor);

      if (labelAssigner.areLabelsNotYetPlaced()) {
        throw new RuntimeException("Not all reserved labels are placed");
      }

      return methodImplementationBuilder.getMethodImplementation();

    } catch (Exception e) {
      throw new RuntimeException("Error while processing method " + sootMethod, e);
    }
  }

  /**
   * Merge register maps at branching points
   * If a local is assigned to different registers at multiple predecessors (and is used later in the control flow graph),
   * move instructions are inserted to reconcile all predecessors to a common register
   */
  private Map<Local, Register> mergeIncomingRegisterMaps(
      Set<BasicBlock<?>> previousBlocks,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtEnd,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtEndExceptional,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtStart,
      ControlFlowGraph<?> controlFlowGraph,
      BasicBlock<?> newBlock) {

    Set<Map<Local, Register>> registerMaps;
    if ((newBlock.getStmts().get(0) instanceof JIdentityStmt jIdentityStmt
        && jIdentityStmt.getRightOp() instanceof JCaughtExceptionRef)) {
      registerMaps =
          previousBlocks.stream()
              .map(blockRegisterMapAtEndExceptional::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
    } else {
      registerMaps =
          previousBlocks.stream()
              .map(blockRegisterMapAtEnd::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
    }

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

      // if local is not used anywhere later in the control flow graph --> continue
      if (!isUsedLater(controlFlowGraph, newBlock.getHead(), local)) {
        continue;
      }

      List<Register> regs =
          registerMaps.stream().map(m -> m.get(local)).filter(Objects::nonNull).toList();

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
                    return intRegisters.stream()
                        .findFirst()
                        .orElseGet(
                            () ->
                                regs.stream()
                                    .filter(reg -> reg.getType().equals(objectType))
                                    .findFirst()
                                    .orElseThrow());
                  });

      result.put(local, newRegister);

      // move all previous register to a common register
      if (!(newBlock.getStmts().get(0) instanceof JIdentityStmt jIdentityStmt
          && jIdentityStmt.getRightOp() instanceof JCaughtExceptionRef)) {
        for (BasicBlock<?> block : previousBlocks) {
          Map<Local, Register> localRegisterMap = blockRegisterMapAtEnd.get(block);
          if (localRegisterMap == null) {
            continue;
          }
          Register old = localRegisterMap.get(local);
          if (old != null && !newRegister.equals(old)) {
            addMoves(block, old, newRegister);
          }
        }
      } else {
        for (BasicBlock<?> block : previousBlocks) {
          Map<Local, Register> localRegisterMap = blockRegisterMapAtEndExceptional.get(block);
          if (localRegisterMap == null) {
            continue;
          }
          addExceptionalMoves(local, block, newRegister, blockRegisterMapAtStart);
        }
      }
    }

    return result;
  }

  /**
   * Evaluates if a jimple local is used (before a new definition) in the control flow graph
   * Starting at a specific stmt
   * @param controlFlowGraph The control flow graph of the method
   * @param start The stmt to evaluate its successors
   * @param local The local to be evaluated
   * @return True if the local is used later
   */
  private boolean isUsedLater(ControlFlowGraph<?> controlFlowGraph, Stmt start, Local local) {
    Set<Stmt> visited = new HashSet<>();
    Deque<Stmt> worklist = new ArrayDeque<>();
    worklist.add(start);
    worklist.addAll(controlFlowGraph.getAllSuccessors(start));

    while (!worklist.isEmpty()) {
      Stmt stmt = worklist.removeFirst();

      if (!visited.add(stmt)) {
        continue;
      }

      if (stmt.getUses().stream().anyMatch(local::equals)) {
        return true;
      }

      if (stmt.getDef().isPresent() && stmt.getDef().get().equals(local)) {
        continue;
      }

      worklist.addAll(controlFlowGraph.getAllSuccessors(stmt));
    }

    return false;
  }

  /**
   * If a successor block has already been processed but the local is there used under a different register compared to the current block
   * Create move instructions from the current registers to the registers of the successor block
   */
  private void mergeCurrentRegisterMapToSuccessor(
      BasicBlock<?> cBlock,
      BasicBlock<?> successorBlock,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapStart,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapEnd,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapEndExceptional) {

    Map<Local, Register> registerMapCurrent;
    if (!(successorBlock.getStmts().get(0) instanceof JIdentityStmt jIdentityStmt
        && jIdentityStmt.getRightOp() instanceof JCaughtExceptionRef)) {
      registerMapCurrent = blockRegisterMapEnd.get(cBlock);
    } else {
      registerMapCurrent = blockRegisterMapEndExceptional.get(cBlock);
    }

    Map<Local, Register> registerMapSuccessor = blockRegisterMapStart.get(successorBlock);

    Set<Local> locals = registerMapCurrent.keySet();

    for (Local local : locals) {

      Register register = registerMapCurrent.get(local);
      Register registerSuccessor = registerMapSuccessor.get(local);

      if (registerSuccessor == null) {
        continue;
      }

      if (!(successorBlock.getStmts().get(0) instanceof JIdentityStmt jIdentityStmt
          && jIdentityStmt.getRightOp() instanceof JCaughtExceptionRef)) {
        if (register.equals(registerSuccessor)) {
          continue;
        }

        Type type = register.getType();
        Type successorType = registerSuccessor.getType();

        if (!type.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))
            && !successorType.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))
            && !type.equals(successorType)) {
          if (type instanceof PrimitiveType && successorType instanceof PrimitiveType.IntType) {
            registerSuccessor.setType(type);
          }
        }

        if (successorType.equals(view.getIdentifierFactory().getType(JIMPLE_OBJECT_TYPE))) {
          registerSuccessor.setType(type);
        }

        // a local has different registers but same type --> move previous registers to a new
        // register
        addMoves(cBlock, register, registerSuccessor);
      } else {
        addExceptionalMoves(local, cBlock, registerSuccessor, blockRegisterMapStart);
      }
    }
  }

  private void addMoves(BasicBlock<?> cBlock, Register register, Register registerSuccessor) {
    var i = instructions.get(cBlock);
    AbstractInstruction moveInstruction =
        generateMoveInstructionToSuccessor(register, registerSuccessor);
    if (i.get(i.size() - 1).getOpcode().name.startsWith("goto")
        || i.get(i.size() - 1).getOpcode().name.startsWith("if")) {
      if (i.size() > 1
          && (i.get(i.size() - 2).getOpcode().equals(Opcode.PACKED_SWITCH)
              || i.get(i.size() - 2).getOpcode().equals(Opcode.SPARSE_SWITCH))) {
        instructionMap.put(
            moveInstruction, instructionMap.get(instructions.get(cBlock).get(i.size() - 2)));
        i.add(i.size() - 2, moveInstruction);
      } else {
        instructionMap.put(
            moveInstruction, instructionMap.get(instructions.get(cBlock).get(i.size() - 1)));
        i.add(i.size() - 1, moveInstruction);
      }
    } else {
      instructionMap.put(
          moveInstruction, instructionMap.get(instructions.get(cBlock).get(i.size() - 1)));
      i.add(moveInstruction);
    }
  }

  private void addExceptionalMoves(
      Local local,
      BasicBlock<?> block,
      Register target,
      Map<BasicBlock<?>, Map<Local, Register>> blockRegisterMapAtStart) {
    var i = instructions.get(block);
    if (blockRegisterMapAtStart.get(block).containsKey(local)) {
      Register previous = blockRegisterMapAtStart.get(block).get(local);
      if (!previous.equals(target)) {
        AbstractInstruction moveInstruction = generateMoveInstructionToSuccessor(previous, target);
        instructionMap.put(moveInstruction, instructionMap.get(instructions.get(block).get(0)));
        i.add(0, moveInstruction);
      }
    }

    // loop at instructions of block
    for (var ins_index = 0; ins_index < i.size() - 1; ins_index++) {
      if (ins_index == i.size() - 2
          && instructions.get(block).get(i.size() - 1).getOpcode().name.startsWith("return")) {
        continue;
      }
      var ins = instructions.get(block).get(ins_index);
      Stmt stmt = instructionMap.get(ins);
      if (instructionMap.get(instructions.get(block).get(ins_index + 1)) != null
          && instructionMap.get(instructions.get(block).get(ins_index + 1)).equals(stmt)) {
        continue;
      }
      if (stmt != null && stmt.getDef().isPresent() && stmt.getDef().get().equals(local)) {
        // move to successor register
        List<Register> defRegisters = ins.getDefRegisters();
        for (Register r : defRegisters) {
          if (!r.equals(target)) {
            AbstractInstruction moveInstruction = generateMoveInstructionToSuccessor(r, target);
            instructionMap.put(moveInstruction, instructionMap.get(ins));
            i.add(ins_index, moveInstruction);
            ins_index++;
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
        return DexExprVisitor.generateMoveInstruction(
            target, previous, previous.getType(), false, null, null);
      } else {
        Opcode opcode =
            Opcode.valueOf(
                sourceType.getName().toUpperCase() + "_TO_" + targetType.getName().toUpperCase());
        return new Instruction12x(opcode, target, previous);
      }

    } else {
      return DexExprVisitor.generateMoveInstruction(
          target, previous, previous.getType(), false, null, null);
    }
  }

  /**
   * When all AbstractInstructions have been created, the instructions are converted to dex BuilderInstructions
   *  First, labels are reserved
   *  Instructions are converted into builderInstructions and labels are set at their corresponding instructions
   *  Finally, payloads and catch blocks are inserted
   */
  public List<BuilderInstruction> addBuilderInstructions(
      MethodImplementationBuilder methodImplementationBuilder,
      LabelAssigner labelAssigner,
      DexStmtVisitor dexStmtVisitor) {

    // Reserve labels
    LinkedHashMap<AbstractPayload, BuilderInstruction> payloadInstructions = new LinkedHashMap<>();
    for (AbstractPayload payload : payloads) {
      payload.setLabelAssigner(labelAssigner);
      payloadInstructions.put(payload, payload.getBuilderInstruction());
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

    tryBlocks.forEach(
        b -> {
          labelAssigner.getOrCreateLabel(b.startStmt);
          labelAssigner.getOrCreateLabelAfterStmt(b.endStmt);
          labelAssigner.getOrCreateLabel(b.catchStmt);
        });

    Map<Local, Integer> seenRegisters = new HashMap<>();

    Map<AbstractInstruction, Pair<Local, Register>> instructionRegisterMap =
        dexStmtVisitor.getInstructionRegisterMap();
    List<Pair<Local, Register>> parameterRegisterMap =
        dexStmtVisitor.getParameterInstructionRegisterList();
    for (var parameter : parameterRegisterMap) {
      addRegisterAssignmentDebugInfo(parameter, seenRegisters, methodImplementationBuilder);
    }

    // Process instructions
    List<BuilderInstruction> builderInstructions = new ArrayList<>();
    for (Map.Entry<BasicBlock<?>, List<AbstractInstruction>> entry : instructions.entrySet()) {
      currentBlock = entry.getKey();
      List<AbstractInstruction> instructionsOfBlock = entry.getValue();

      for (int i = 0; i < instructionsOfBlock.size(); i++) {
        AbstractInstruction instruction = instructionsOfBlock.get(i);
        instruction.setLabelAssigner(labelAssigner);

        Stmt currentStmt = instructionMap.get(instruction);

        if (labelAssigner.hasLabel(currentStmt)) {
          labelAssigner.setLabel(currentStmt);
        }

        if (instructionMap.containsKey(instruction)
            && instructionMap.get(instruction) != null
            && instructionMap
                .get(instruction)
                .getPositionInfo()
                .equals(StmtPositionInfo.getNoStmtPositionInfo())) {
          methodImplementationBuilder.addLineNumber(
              instructionMap.get(instruction).getPositionInfo().getStmtPosition().getFirstLine());
        }

        builderInstructions.add(instruction.getBuilderInstruction());
        methodImplementationBuilder.addInstruction(instruction.getBuilderInstruction());

        Pair<Local, Register> registerAssignment = instructionRegisterMap.get(instruction);
        if (registerAssignment != null) {
          addRegisterAssignmentDebugInfo(
              registerAssignment, seenRegisters, methodImplementationBuilder);
        }

        if (i + 1 >= instructionsOfBlock.size()
            || currentStmt != instructionMap.get(instructionsOfBlock.get(i + 1))) {
          if (labelAssigner.hasLabelAfterStmt(currentStmt)) {
            labelAssigner.setLabelAfterStmt(currentStmt);
          }
        }
      }
    }

    for (int registersLeft : seenRegisters.values()) {
      methodImplementationBuilder.addEndLocal(registersLeft);
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

    return builderInstructions;
  }

  private void addRegisterAssignmentDebugInfo(
      Pair<Local, Register> registerAssignment,
      Map<Local, Integer> seenRegisters,
      MethodImplementationBuilder builder) {
    Local local = registerAssignment.getLeft();
    StringReference localName = new ImmutableStringReference(local.getName());
    Register register = registerAssignment.getRight();
    String dexLocalType = DexUtil.toDexType(register.getType());
    int registerNumber = register.getNumber();

    Integer beforeRegister = seenRegisters.get(local);
    if (beforeRegister != null) {
      if (beforeRegister == registerNumber) {
        return;
      }
      builder.addEndLocal(beforeRegister);
    }
    builder.addStartLocal(
        registerNumber,
        localName,
        new ImmutableTypeReference(dexLocalType),
        new ImmutableStringReference(""));
    seenRegisters.put(local, registerNumber);
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

  protected View getView() {
    return view;
  }
}
