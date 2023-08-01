package sootup.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Jan Martin Persch, Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.objectweb.asm.tree.AbstractInsnNode.FIELD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.FRAME;
import static org.objectweb.asm.tree.AbstractInsnNode.IINC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INT_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.INVOKE_DYNAMIC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.JUMP_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LABEL;
import static org.objectweb.asm.tree.AbstractInsnNode.LDC_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.LINE;
import static org.objectweb.asm.tree.AbstractInsnNode.LOOKUPSWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.MULTIANEWARRAY_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TABLESWITCH_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.TYPE_INSN;
import static org.objectweb.asm.tree.AbstractInsnNode.VAR_INSN;

import com.google.common.base.Suppliers;
import com.google.common.collect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.*;
import sootup.core.frontend.BodySource;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.AbstractUnopExpr;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;
import sootup.core.views.View;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * A {@link BodySource} that can read Java bytecode
 *
 * @author Andreas Dann
 */
public class AsmMethodSource extends JSRInlinerAdapter implements BodySource {

  // private static final String METAFACTORY_SIGNATURE =
  // "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite "
  // +
  // "metafactory(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,"
  // + ""
  // +
  // "java.lang.invoke.MethodType,java.lang.invoke.MethodHandle,java.lang.invoke.MethodType)>";
  // private static final String ALT_METAFACTORY_SIGNATURE =
  // "<java.lang.invoke.LambdaMetafactory: java.lang.invoke.CallSite "
  // + "altMetafactory(java.lang.invoke.MethodHandles$Lookup,"
  // + "java.lang.String,java.lang.invoke.MethodType,java.lang.Object[])>";

  /* -state fields- */
  private int nextLocal;
  private List<JavaLocal> locals;
  private LinkedListMultimap<BranchingStmt, LabelNode> stmtsThatBranchToLabel;
  private Map<AbstractInsnNode, Stmt> insnToStmt;

  @Nonnull private final Map<Stmt, Stmt> replacedStmt = new HashMap<>();

  private OperandStack operandStack;
  private Map<LabelNode, Stmt> trapHandler;

  private int currentLineNumber = -1;
  private int maxLineNumber = 0;

  @Nullable private JavaClassType declaringClass;

  private final View<?> view;
  private final List<BodyInterceptor> bodyInterceptors;

  @Nonnull private final Set<LabelNode> inlineExceptionLabels = new HashSet<>();

  @Nonnull
  private final Map<LabelNode, AbstractDefinitionStmt<Local, JCaughtExceptionRef>>
      inlineExceptionHandlers = new HashMap<>();

  @Nonnull private final Map<LabelNode, Stmt> labelsToStmt = new HashMap<>();

  private final JavaIdentifierFactory identifierFactory;
  private final Supplier<MethodSignature> lazyMethodSignature;

  AsmMethodSource(
      int access,
      @Nonnull String name,
      @Nonnull String desc,
      @Nonnull String signature,
      @Nonnull String[] exceptions,
      View<?> view,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    super(AsmUtil.SUPPORTED_ASM_OPCODE, null, access, name, desc, signature, exceptions);
    this.bodyInterceptors = bodyInterceptors;
    this.view = view;

    identifierFactory = (JavaIdentifierFactory) view.getIdentifierFactory();
    lazyMethodSignature =
        Suppliers.memoize(
            () -> {
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              return identifierFactory.getMethodSignature(declaringClass, name, retType, sigTypes);
            });
  }

  @Override
  @Nonnull
  public MethodSignature getSignature() {
    return lazyMethodSignature.get();
  }

  void setDeclaringClass(@Nonnull ClassType declaringClass) {
    this.declaringClass = (JavaClassType) declaringClass;
  }

  StmtPositionInfo getStmtPositionInfo() {
    return currentLineNumber > 0
        ? new SimpleStmtPositionInfo(currentLineNumber)
        : StmtPositionInfo.createNoStmtPositionInfo();
  }

  @Override
  @Nonnull
  public Body resolveBody(@Nonnull Iterable<MethodModifier> modifierIt) {

    /* initialize */
    nextLocal = maxLocals;
    locals =
        new NonIndexOutofBoundsArrayList<>(
            maxLocals
                + Math.max((maxLocals / 2), 5)); // [ms] initial capacity is just roughly estimated.
    stmtsThatBranchToLabel = LinkedListMultimap.create();
    insnToStmt = new LinkedHashMap<>(instructions.size());
    operandStack = new OperandStack(this, instructions.size());
    trapHandler = new LinkedHashMap<>(tryCatchBlocks.size());

    /* retrieve all trap handlers */
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      // reserve space/ insert labels in datastructure  - necessary?! its useless if not assigned
      // later.. -> check the meaning of that containsKey() check
      trapHandler.put(tc.handler, null);
    }

    /* build body (add stmts, locals, traps, etc.) */
    final MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder bodyBuilder = Body.builder(graph);
    bodyBuilder.setModifiers(AsmUtil.getMethodModifiers(access));

    final List<Stmt> preambleStmts = buildPreambleLocals(bodyBuilder);

    /* convert instructions */
    try {
      convert();
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert " + lazyMethodSignature.get(), e);
    }

    // collect used Locals
    Set<Local> bodyLocals =
        locals.stream()
            .filter(Objects::nonNull)
            // [ms] find out why some Local indices are not assigned(null)
            // ms -> guess because of dword values i.e. +=2 ?
            .collect(Collectors.toCollection(LinkedHashSet::new));
    bodyBuilder.setLocals(bodyLocals);

    // add converted insn as stmts into the graph
    try {
      arrangeStmts(graph, preambleStmts, bodyBuilder);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert " + lazyMethodSignature.get(), e);
    }

    // propagate position information
    if (graph.getNodes().size() > 0) {
      Position firstStmtPos = graph.getStartingStmt().getPositionInfo().getStmtPosition();
      bodyBuilder.setPosition(
          new FullPosition(
              firstStmtPos.getFirstLine(),
              firstStmtPos.getFirstCol(),
              maxLineNumber,
              Integer.MAX_VALUE));
    } else {
      bodyBuilder.setPosition(NoPositionInformation.getInstance());
    }

    /* clean up for gc */
    locals = null;
    stmtsThatBranchToLabel = null;
    insnToStmt = null;
    operandStack = null;

    bodyBuilder.setMethodSignature(lazyMethodSignature.get());

    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      try {
        bodyInterceptor.interceptBody(bodyBuilder, view);
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to apply " + bodyInterceptor + " to " + lazyMethodSignature.get(), e);
      }
    }
    return bodyBuilder.build();
  }

  @Override
  public Object resolveAnnotationsDefaultValue() {
    return resolveAnnotationsInDefaultValue(this.annotationDefault);
  }

  private Object resolveAnnotationsInDefaultValue(Object a) {
    if (a instanceof AnnotationNode) {
      return AsmUtil.createAnnotationUsage(Collections.singletonList((AnnotationNode) a));
    }

    if (a instanceof ArrayList) {
      List<Object> list = new ArrayList<>();
      ((ArrayList) a).forEach(e -> list.add(resolveAnnotationsInDefaultValue(e)));
      return list;
    }
    return AsmUtil.convertAnnotationValue(a);
  }

  @Nonnull
  private JavaLocal getOrCreateLocal(int idx) {
    if (idx >= maxLocals) {
      throw new IllegalArgumentException("Invalid local index: " + idx);
    }
    JavaLocal local = locals.get(idx);
    if (local == null) {
      String name = determineLocalName(idx);
      local = JavaJimple.newLocal(name, UnknownType.getInstance(), Collections.emptyList());
      locals.set(idx, local);
    }
    return local;
  }

  @Nonnull
  private String determineLocalName(int idx) {
    String name;
    if (localVariables != null) {
      name = null;
      for (LocalVariableNode lvn : localVariables) {
        if (lvn.index == idx) {
          name = lvn.name;
          break;
        }
      }
      /* normally for try-catch blocks */
      if (name == null) {
        name = "l" + idx;
      }
    } else {
      name = "l" + idx;
    }
    return name;
  }

  void setStmt(@Nonnull AbstractInsnNode insn, @Nonnull Stmt stmt) {
    Stmt overwrittenStmt = insnToStmt.put(insn, stmt);
    if (overwrittenStmt != null) {
      throw new IllegalArgumentException(
          insn.getOpcode() + " already has an associated Stmt: " + overwrittenStmt);
    }
  }

  void mergeStmts(@Nonnull AbstractInsnNode insn, @Nonnull Stmt stmt) {
    Stmt initiallyAssignedStmt = insnToStmt.put(insn, stmt);
    if (initiallyAssignedStmt != null) {
      Stmt merged = StmtContainer.getOrCreate(initiallyAssignedStmt, stmt);
      insnToStmt.put(insn, merged);
    }
  }

  @Nonnull
  Local newStackLocal() {
    int idx = nextLocal++;
    JavaLocal l =
        JavaJimple.newLocal("$stack" + idx, UnknownType.getInstance(), Collections.emptyList());
    locals.set(idx, l);
    return l;
  }

  @SuppressWarnings("unchecked")
  <A extends Stmt> A getStmt(@Nonnull AbstractInsnNode insn) {
    return (A) insnToStmt.get(insn);
  }

  private void addReadOperandAssignments() {
    addReadOperandAssignments_internal(
        (opValue, operand) -> {
          if (opValue instanceof Local) {
            return true;
          }
          int op = operand.insn.getOpcode();
          return op != GETFIELD && op != GETSTATIC && (op < IALOAD || op > SALOAD);
        });
  }

  private void addReadOperandAssignments(@Nonnull Local local) {
    addReadOperandAssignments_internal(
        (opValue, operand) -> {
          if (!opValue.equivTo(local)) {
            boolean noRef = true;
            for (Value use : opValue.getUses()) {
              if (use.equivTo(local)) {
                noRef = false;
                break;
              }
            }
            return noRef;
          }
          return false;
        });
  }

  private void addReadOperandAssignments_internal(BiFunction<Value, Operand, Boolean> func) {
    // determine which Operand(s) from the stack needs explicit assignments in Jimple
    for (Operand operand : operandStack.getStack()) {
      final Value opValue = operand.value;
      if (operand == Operand.DWORD_DUMMY || operand.stackLocal != null) {
        continue;
      }
      if (func.apply(opValue, operand)) {
        continue;
      }

      Local stackLocal = newStackLocal();
      operand.stackLocal = stackLocal;
      JAssignStmt<Local, ?> asssignStmt =
          Jimple.newAssignStmt(stackLocal, opValue, getStmtPositionInfo());

      setStmt(operand.insn, asssignStmt);
      operand.updateUsages();
    }
  }

  private void convertGetFieldInsn(@Nonnull FieldInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    Type type;
    if (out == null) {
      JavaClassType declClass = identifierFactory.getClassType(AsmUtil.toQualifiedName(insn.owner));
      type = AsmUtil.toJimpleType(insn.desc);
      JFieldRef val;
      FieldSignature ref;
      if (insn.getOpcode() == GETSTATIC) {
        ref = identifierFactory.getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
      } else {
        Operand base = operandStack.popLocal();
        ref = identifierFactory.getFieldSignature(insn.name, declClass, type);
        val = Jimple.newInstanceFieldRef((Local) base.stackOrValue(), ref);
        frame.setIn(base);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      type = ((JFieldRef) opr.value).getFieldSignature().getType();
      if (insn.getOpcode() == GETFIELD) {
        frame.mergeIn(currentLineNumber, operandStack.pop());
      }
    }
    operandStack.push(type, opr);
  }

  private void convertPutFieldInsn(@Nonnull FieldInsnNode insn) {
    boolean notInstance = insn.getOpcode() != PUTFIELD;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr, rvalue;
    Type type;
    if (out == null) {
      JavaClassType declClass = identifierFactory.getClassType(AsmUtil.toQualifiedName(insn.owner));
      type = AsmUtil.toJimpleType(insn.desc);

      JFieldRef val;
      FieldSignature ref;
      rvalue = operandStack.popImmediate(type);
      if (notInstance) {
        ref = identifierFactory.getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
        frame.setIn(rvalue);
      } else {
        Operand base = operandStack.popLocal();
        ref = identifierFactory.getFieldSignature(insn.name, declClass, type);
        val = Jimple.newInstanceFieldRef((Local) base.stackOrValue(), ref);
        frame.setIn(rvalue, base);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
      JAssignStmt<JFieldRef, ?> as =
          Jimple.newAssignStmt(val, rvalue.stackOrValue(), getStmtPositionInfo());
      setStmt(insn, as);
      rvalue.addUsageInStmt(as);
    } else {
      opr = out[0];
      type = ((JFieldRef) opr.value).getFieldSignature().getType();
      rvalue = operandStack.pop(type);
      if (notInstance) {
        /* PUTSTATIC only needs one operand on the stack, the rvalue */
        frame.mergeIn(currentLineNumber, rvalue);
      } else {
        /* PUTFIELD has a rvalue and a base */
        frame.mergeIn(currentLineNumber, rvalue, operandStack.pop());
      }
    }
    /*
     * in case any static field or array is read from, and the static constructor or the field this instruction writes to,
     * modifies that field, write out any previous read from field/array
     */
    addReadOperandAssignments();
  }

  private void convertFieldInsn(@Nonnull FieldInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GETSTATIC || op == GETFIELD) {
      convertGetFieldInsn(insn);
    } else {
      convertPutFieldInsn(insn);
    }
  }

  private void convertIincInsn(@Nonnull IincInsnNode insn) {
    Local local = getOrCreateLocal(insn.var);
    addReadOperandAssignments(local);
    if (!insnToStmt.containsKey(insn)) {
      JAddExpr add = Jimple.newAddExpr(local, IntConstant.getInstance(insn.incr));
      setStmt(insn, Jimple.newAssignStmt(local, add, getStmtPositionInfo()));
    }
  }

  private void convertConstInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Value v;
      if (op == ACONST_NULL) {
        v = NullConstant.getInstance();
      } else if (op >= ICONST_M1 && op <= ICONST_5) {
        v = IntConstant.getInstance(op - ICONST_0);
      } else if (op == LCONST_0 || op == LCONST_1) {
        v = LongConstant.getInstance(op - LCONST_0);
      } else if (op >= FCONST_0 && op <= FCONST_2) {
        v = FloatConstant.getInstance(op - FCONST_0);
      } else if (op == DCONST_0 || op == DCONST_1) {
        v = DoubleConstant.getInstance(op - DCONST_0);
      } else {
        throw new UnsupportedOperationException("Unknown constant opcode: " + op);
      }
      opr = new Operand(insn, v, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
    }
    if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertArrayLoadInsn(@Nonnull InsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Operand indx = operandStack.popImmediate();
      Operand base = operandStack.popImmediate();
      JArrayRef ar =
          JavaJimple.getInstance()
              .newArrayRef((Local) base.stackOrValue(), (Immediate) indx.stackOrValue());
      opr = new Operand(insn, ar, this);
      frame.setIn(indx, base);
      frame.setOut(opr);
    } else {
      opr = out[0];
      frame.mergeIn(currentLineNumber, operandStack.pop(), operandStack.pop());
    }
    int op = insn.getOpcode();
    if (op == DALOAD || op == LALOAD) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertArrayStoreInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LASTORE || op == DASTORE;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (!insnToStmt.containsKey(insn)) {
      Operand valueOp = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      Operand indexOp = operandStack.popImmediate();
      Operand baseOp = operandStack.popLocal();
      JArrayRef ar =
          JavaJimple.getInstance()
              .newArrayRef((Local) baseOp.stackOrValue(), (Immediate) indexOp.stackOrValue());
      JAssignStmt<JArrayRef, ?> as =
          Jimple.newAssignStmt(ar, valueOp.stackOrValue(), getStmtPositionInfo());
      frame.setIn(valueOp, indexOp, baseOp);
      setStmt(insn, as);
      valueOp.addUsageInStmt(as);

    } else {
      frame.mergeIn(
          currentLineNumber,
          dword ? operandStack.popDual() : operandStack.pop(),
          operandStack.pop(),
          operandStack.pop());
    }
  }

  /*
   * Following version is more complex, using stack frames as opposed to simply swapping
   */
  /*
   * StackFrame frame = getFrame(insn); Operand[] out = frame.out(); Operand dup, dup2 = null, dupd, dupd2 = null; if (out ==
   * null) { dupd = popImmediate(); dup = new Operand(insn, dupd.stackOrValue()); if (dword) { dupd2 = peek(); if (dupd2 ==
   * DWORD_DUMMY) { pop(); dupd2 = dupd; } else { dupd2 = popImmediate(); } dup2 = new Operand(insn, dupd2.stackOrValue());
   * frame.out(dup, dup2); frame.in(dupd, dupd2); } else { frame.out(dup); frame.in(dupd); } } else { dupd = pop(); dup =
   * out[0]; if (dword) { dupd2 = pop(); if (dupd2 == DWORD_DUMMY) dupd2 = dupd; dup2 = out[1]; frame.mergeIn(dupd, dupd2); }
   * else { frame.mergeIn(dupd); } }
   */

  private void convertDupInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();

    // Get the top stack value which we need in either case
    Operand dupd = operandStack.popImmediate();
    Operand dupd2 = null;

    // Some instructions allow operands that take two registers
    boolean dword = op == DUP2 || op == DUP2_X1 || op == DUP2_X2;
    if (dword) {
      if (operandStack.peek() == Operand.DWORD_DUMMY) {
        operandStack.pop();
        dupd2 = dupd;
      } else {
        dupd2 = operandStack.popImmediate();
      }
    }

    if (op == DUP) {
      // val -> val, val
      operandStack.push(dupd);
      operandStack.push(dupd);
    } else if (op == DUP_X1) {
      // val2, val1 -> val1, val2, val1
      // value1, value2 must not be of type double or long
      Operand o2 = operandStack.popImmediate();
      operandStack.push(dupd);
      operandStack.push(o2);
      operandStack.push(dupd);
    } else if (op == DUP_X2) {
      // value3, value2, value1 -> value1, value3, value2, value1
      Operand o2 = operandStack.popImmediate();
      Operand o3 =
          operandStack.peek() == Operand.DWORD_DUMMY
              ? operandStack.pop()
              : operandStack.popImmediate();
      operandStack.push(dupd);
      operandStack.push(o3);
      operandStack.push(o2);
      operandStack.push(dupd);
    } else if (op == DUP2) {
      // value2, value1 -> value2, value1, value2, value1
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    } else if (op == DUP2_X1) {
      // value3, value2, value1 -> value2, value1, value3, value2, value1
      // Attention: value2 may be
      Operand o2 = operandStack.popImmediate();
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(o2);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    } else if (op == DUP2_X2) {
      // (value4, value3), (value2, value1) -> (value2, value1), (value4, value3),
      // (value2, value1)
      Operand o2 = operandStack.popImmediate();
      Operand o2h =
          operandStack.peek() == Operand.DWORD_DUMMY
              ? operandStack.pop()
              : operandStack.popImmediate();
      operandStack.push(dupd2);
      operandStack.push(dupd);
      operandStack.push(o2h);
      operandStack.push(o2);
      operandStack.push(dupd2);
      operandStack.push(dupd);
    }
  }

  private void convertBinopInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword =
        op == DADD
            || op == LADD
            || op == DSUB
            || op == LSUB
            || op == DMUL
            || op == LMUL
            || op == DDIV
            || op == LDIV
            || op == DREM
            || op == LREM
            || op == LSHL
            || op == LSHR
            || op == LUSHR
            || op == LAND
            || op == LOR
            || op == LXOR
            || op == LCMP
            || op == DCMPL
            || op == DCMPG;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Operand op2 =
          (dword && op != LSHL && op != LSHR && op != LUSHR)
              ? operandStack.popImmediateDual()
              : operandStack.popImmediate();
      Operand op1 = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      Immediate v1 = (Immediate) op1.stackOrValue();
      Immediate v2 = (Immediate) op2.stackOrValue();
      AbstractBinopExpr binop;
      if (op >= IADD && op <= DADD) {
        binop = Jimple.newAddExpr(v1, v2);
      } else if (op >= ISUB && op <= DSUB) {
        binop = Jimple.newSubExpr(v1, v2);
      } else if (op >= IMUL && op <= DMUL) {
        binop = Jimple.newMulExpr(v1, v2);
      } else if (op >= IDIV && op <= DDIV) {
        binop = Jimple.newDivExpr(v1, v2);
      } else if (op >= IREM && op <= DREM) {
        binop = Jimple.newRemExpr(v1, v2);
      } else if (op >= ISHL && op <= LSHL) {
        binop = Jimple.newShlExpr(v1, v2);
      } else if (op >= ISHR && op <= LSHR) {
        binop = Jimple.newShrExpr(v1, v2);
      } else if (op >= IUSHR && op <= LUSHR) {
        binop = Jimple.newUshrExpr(v1, v2);
      } else if (op >= IAND && op <= LAND) {
        binop = Jimple.newAndExpr(v1, v2);
      } else if (op >= IOR && op <= LOR) {
        binop = Jimple.newOrExpr(v1, v2);
      } else if (op >= IXOR && op <= LXOR) {
        binop = Jimple.newXorExpr(v1, v2);
      } else if (op == LCMP) {
        binop = Jimple.newCmpExpr(v1, v2);
      } else if (op == FCMPL || op == DCMPL) {
        binop = Jimple.newCmplExpr(v1, v2);
      } else if (op == FCMPG || op == DCMPG) {
        binop = Jimple.newCmpgExpr(v1, v2);
      } else {
        throw new UnsupportedOperationException("Unknown binop: " + op);
      }
      opr = new Operand(insn, binop, this);
      op1.addUsageInExpr(binop);
      op2.addUsageInExpr(binop);

      frame.setIn(op2, op1);
      frame.setOut(opr);
    } else {
      opr = out[0];
      if (dword) {
        if (op != LSHL && op != LSHR && op != LUSHR) {

          frame.mergeIn(currentLineNumber, operandStack.popDual(), operandStack.popDual());
        } else {
          frame.mergeIn(currentLineNumber, operandStack.pop(), operandStack.popDual());
        }
      } else {
        frame.mergeIn(currentLineNumber, operandStack.pop(), operandStack.pop());
      }
    }
    if (dword && op < LCMP) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertUnopInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LNEG || op == DNEG;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Operand op1 = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      Value v1 = op1.stackOrValue();
      AbstractUnopExpr unop;
      if (op >= INEG && op <= DNEG) {
        unop = Jimple.newNegExpr((Immediate) v1);
      } else if (op == ARRAYLENGTH) {
        unop = Jimple.newLengthExpr((Immediate) v1);
      } else {
        throw new UnsupportedOperationException("Unknown unop: " + op);
      }
      op1.addUsageInExpr(unop);
      opr = new Operand(insn, unop, this);
      frame.setIn(op1);
      frame.setOut(opr);
    } else {
      opr = out[0];
      frame.mergeIn(currentLineNumber, dword ? operandStack.popDual() : operandStack.pop());
    }
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertPrimCastInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean tod = op == I2L || op == I2D || op == F2L || op == F2D || op == D2L || op == L2D;
    boolean fromd = op == D2L || op == L2D || op == D2I || op == L2I || op == D2F || op == L2F;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Type totype;
      switch (op) {
        case I2L:
        case F2L:
        case D2L:
          totype = PrimitiveType.getLong();
          break;
        case L2I:
        case F2I:
        case D2I:
          totype = PrimitiveType.getInt();
          break;
        case I2F:
        case L2F:
        case D2F:
          totype = PrimitiveType.getFloat();
          break;
        case I2D:
        case L2D:
        case F2D:
          totype = PrimitiveType.getDouble();
          break;
        case I2B:
          totype = PrimitiveType.getByte();
          break;
        case I2S:
          totype = PrimitiveType.getShort();
          break;
        case I2C:
          totype = PrimitiveType.getChar();
          break;
        default:
          throw new IllegalStateException("Unknown prim cast op: " + op);
      }
      Operand val = fromd ? operandStack.popImmediateDual() : operandStack.popImmediate();
      JCastExpr cast = Jimple.newCastExpr((Immediate) val.stackOrValue(), totype);
      opr = new Operand(insn, cast, this);
      val.addUsageInExpr(cast);
      frame.setIn(val);
      frame.setOut(opr);
    } else {
      opr = out[0];
      frame.mergeIn(currentLineNumber, fromd ? operandStack.popDual() : operandStack.pop());
    }
    if (tod) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertReturnInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LRETURN || op == DRETURN;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (!insnToStmt.containsKey(insn)) {
      Operand val = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      JReturnStmt ret = Jimple.newReturnStmt((Immediate) val.stackOrValue(), getStmtPositionInfo());
      frame.setIn(val);
      setStmt(insn, ret);
      val.addUsageInStmt(ret);
    } else {
      final Operand operand = dword ? operandStack.popDual() : operandStack.pop();
      frame.mergeIn(currentLineNumber, operand);
    }
  }

  private void convertInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    if (op == NOP) {
      /*
       * We can ignore NOP instructions, but for completeness, we handle them
       */
      if (!insnToStmt.containsKey(insn)) {
        insnToStmt.put(insn, Jimple.newNopStmt(getStmtPositionInfo()));
      }
    } else if (op >= ACONST_NULL && op <= DCONST_1) {
      convertConstInsn(insn);
    } else if (op >= IALOAD && op <= SALOAD) {
      convertArrayLoadInsn(insn);
    } else if (op >= IASTORE && op <= SASTORE) {
      convertArrayStoreInsn(insn);
    } else if (op == POP) {
      operandStack.popImmediate();
    } else if (op == POP2) {
      operandStack.popImmediate();
      if (operandStack.peek() == Operand.DWORD_DUMMY) {
        operandStack.pop();
      } else {
        operandStack.popImmediate();
      }
    } else if (op >= DUP && op <= DUP2_X2) {
      convertDupInsn(insn);
    } else if (op == SWAP) {
      Operand o1 = operandStack.popImmediate();
      Operand o2 = operandStack.popImmediate();
      operandStack.push(o1);
      operandStack.push(o2);
    } else if ((op >= IADD && op <= DREM)
        || (op >= ISHL && op <= LXOR)
        || (op >= LCMP && op <= DCMPG)) {
      convertBinopInsn(insn);
    } else if ((op >= INEG && op <= DNEG) || op == ARRAYLENGTH) {
      convertUnopInsn(insn);
    } else if (op >= I2L && op <= I2S) {
      convertPrimCastInsn(insn);
    } else if (op >= IRETURN && op <= ARETURN) {
      convertReturnInsn(insn);
    } else if (op == RETURN) {
      if (!insnToStmt.containsKey(insn)) {
        setStmt(insn, Jimple.newReturnVoidStmt(getStmtPositionInfo()));
      }
    } else if (op == ATHROW) {
      StackFrame frame = operandStack.getOrCreateStackframe(insn);
      Operand opr;
      if (!insnToStmt.containsKey(insn)) {
        opr = operandStack.popImmediate();
        JThrowStmt ts = Jimple.newThrowStmt((Immediate) opr.stackOrValue(), getStmtPositionInfo());
        frame.setIn(opr);
        frame.setOut(opr);
        setStmt(insn, ts);
        opr.addUsageInStmt(ts);
      } else {
        opr = operandStack.pop();
        frame.mergeIn(currentLineNumber, opr);
      }
      operandStack.push(opr);
    } else if (op == MONITORENTER || op == MONITOREXIT) {
      StackFrame frame = operandStack.getOrCreateStackframe(insn);
      if (!insnToStmt.containsKey(insn)) {
        Operand opr = operandStack.popStackConst();
        AbstractOpStmt ts =
            op == MONITORENTER
                ? Jimple.newEnterMonitorStmt((Immediate) opr.stackOrValue(), getStmtPositionInfo())
                : Jimple.newExitMonitorStmt((Immediate) opr.stackOrValue(), getStmtPositionInfo());
        frame.setIn(opr);
        setStmt(insn, ts);
        opr.addUsageInStmt(ts);
      } else {
        frame.mergeIn(currentLineNumber, operandStack.pop());
      }
    } else {
      throw new UnsupportedOperationException("Unknown insn op: " + op);
    }
  }

  private void convertIntInsn(@Nonnull IntInsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Value v;
      if (op == BIPUSH || op == SIPUSH) {
        v = IntConstant.getInstance(insn.operand);
      } else {
        // assert(op == NEWARRAY)
        Type type;
        switch (insn.operand) {
          case T_BOOLEAN:
            type = PrimitiveType.getBoolean();
            break;
          case T_CHAR:
            type = PrimitiveType.getChar();
            break;
          case T_FLOAT:
            type = PrimitiveType.getFloat();
            break;
          case T_DOUBLE:
            type = PrimitiveType.getDouble();
            break;
          case T_BYTE:
            type = PrimitiveType.getByte();
            break;
          case T_SHORT:
            type = PrimitiveType.getShort();
            break;
          case T_INT:
            type = PrimitiveType.getInt();
            break;
          case T_LONG:
            type = PrimitiveType.getLong();
            break;
          default:
            throw new UnsupportedOperationException("Unknown NEWARRAY type!");
        }
        Operand size = operandStack.popImmediate();
        JNewArrayExpr anew =
            JavaJimple.getInstance().newNewArrayExpr(type, (Immediate) size.stackOrValue());
        size.addUsageInExpr(anew);
        frame.setIn(size);
        v = anew;
      }
      opr = new Operand(insn, v, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      if (op == NEWARRAY) {
        frame.mergeIn(currentLineNumber, operandStack.pop());
      }
    }
    operandStack.push(opr);
  }

  private void convertJumpInsn(@Nonnull JumpInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GOTO) {
      if (!insnToStmt.containsKey(insn)) {
        BranchingStmt gotoStmt = Jimple.newGotoStmt(getStmtPositionInfo());
        stmtsThatBranchToLabel.put(gotoStmt, insn.label);
        setStmt(insn, gotoStmt);
      }
      return;
    }
    /* must be ifX insn */
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (!insnToStmt.containsKey(insn)) {
      Operand val = operandStack.popImmediate();
      Immediate v = (Immediate) val.stackOrValue();
      AbstractConditionExpr cond;
      boolean isCmp = false;
      Operand val1 = null;

      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        isCmp = true;
        val1 = operandStack.popImmediate();
        Immediate v1 = (Immediate) val1.stackOrValue();
        switch (op) {
          case IF_ICMPEQ:
          case IF_ACMPEQ:
            cond = Jimple.newEqExpr(v1, v);
            break;
          case IF_ICMPNE:
          case IF_ACMPNE:
            cond = Jimple.newNeExpr(v1, v);
            break;
          case IF_ICMPLT:
            cond = Jimple.newLtExpr(v1, v);
            break;
          case IF_ICMPGE:
            cond = Jimple.newGeExpr(v1, v);
            break;
          case IF_ICMPGT:
            cond = Jimple.newGtExpr(v1, v);
            break;
          case IF_ICMPLE:
            cond = Jimple.newLeExpr(v1, v);
            break;
          default:
            throw new UnsupportedOperationException("Unknown if op: " + op);
        }
        val1.addUsageInExpr(cond);
        val.addUsageInExpr(cond);
        frame.setIn(val, val1);
      } else {
        switch (op) {
          case IFEQ:
            cond = Jimple.newEqExpr(v, IntConstant.getInstance(0));
            break;
          case IFNE:
            cond = Jimple.newNeExpr(v, IntConstant.getInstance(0));
            break;
          case IFLT:
            cond = Jimple.newLtExpr(v, IntConstant.getInstance(0));
            break;
          case IFGE:
            cond = Jimple.newGeExpr(v, IntConstant.getInstance(0));
            break;
          case IFGT:
            cond = Jimple.newGtExpr(v, IntConstant.getInstance(0));
            break;
          case IFLE:
            cond = Jimple.newLeExpr(v, IntConstant.getInstance(0));
            break;
          case IFNULL:
            cond = Jimple.newEqExpr(v, NullConstant.getInstance());
            break;
          case IFNONNULL:
            cond = Jimple.newNeExpr(v, NullConstant.getInstance());
            break;
          default:
            throw new UnsupportedOperationException("Unknown if op: " + op);
        }
        val.addUsageInExpr(cond);
        frame.setIn(val);
      }
      BranchingStmt ifStmt = Jimple.newIfStmt(cond, getStmtPositionInfo());
      stmtsThatBranchToLabel.put(ifStmt, insn.label);
      setStmt(insn, ifStmt);
      if (isCmp) {
        val1.addUsageInStmt(ifStmt);
      }

      val.addUsageInStmt(ifStmt);
    } else {
      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        frame.mergeIn(currentLineNumber, operandStack.pop(), operandStack.pop());
      } else {
        frame.mergeIn(currentLineNumber, operandStack.pop());
      }
    }
  }

  private void convertLdcInsn(@Nonnull LdcInsnNode insn) {
    Object val = insn.cst;
    boolean dword = val instanceof Long || val instanceof Double;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Value v = toSootValue(val);
      opr = new Operand(insn, v, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
    }
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private Immediate toSootValue(@Nonnull Object val) throws UnsupportedOperationException {
    Immediate v;
    if (val instanceof Integer) {
      v = IntConstant.getInstance((Integer) val);
    } else if (val instanceof Float) {
      v = FloatConstant.getInstance((Float) val);
    } else if (val instanceof Long) {
      v = LongConstant.getInstance((Long) val);
    } else if (val instanceof Double) {
      v = DoubleConstant.getInstance((Double) val);
    } else if (val instanceof String) {
      v = JavaJimple.getInstance().newStringConstant(val.toString());
    } else if (val instanceof org.objectweb.asm.Type) {
      org.objectweb.asm.Type t = (org.objectweb.asm.Type) val;
      if (t.getSort() == org.objectweb.asm.Type.METHOD) {
        List<Type> paramTypes =
            AsmUtil.toJimpleSignatureDesc(((org.objectweb.asm.Type) val).getDescriptor());
        Type returnType = paramTypes.remove(paramTypes.size() - 1);
        v = JavaJimple.getInstance().newMethodType(paramTypes, returnType);
      } else {
        v =
            JavaJimple.getInstance()
                .newClassConstant(((org.objectweb.asm.Type) val).getDescriptor());
      }
    } else if (val instanceof Handle) {
      Handle h = (Handle) val;
      if (MethodHandle.isMethodRef(h.getTag())) {
        v =
            JavaJimple.getInstance()
                .newMethodHandle(toMethodSignature((Handle) val), ((Handle) val).getTag());
      } else {
        v =
            JavaJimple.getInstance()
                .newMethodHandle(toSootFieldRef((Handle) val), ((Handle) val).getTag());
      }
    } else {
      throw new UnsupportedOperationException("Unknown constant type: " + val.getClass());
    }
    return v;
  }

  private JFieldRef toSootFieldRef(Handle methodHandle) {
    String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
    JavaClassType bsmCls = identifierFactory.getClassType(bsmClsName);
    Type t = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc()).get(0);
    int kind = methodHandle.getTag();
    FieldSignature fieldSignature =
        identifierFactory.getFieldSignature(methodHandle.getName(), bsmCls, t);
    if (kind == MethodHandle.Kind.REF_GET_FIELD_STATIC.getValue()
        || kind == MethodHandle.Kind.REF_PUT_FIELD_STATIC.getValue()) {
      return Jimple.newStaticFieldRef(fieldSignature);
    } else {
      Operand base = operandStack.popLocal();
      return Jimple.newInstanceFieldRef((Local) base.stackOrValue(), fieldSignature);
    }
  }

  private MethodSignature toMethodSignature(Handle methodHandle) {
    String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
    JavaClassType bsmCls = identifierFactory.getClassType(bsmClsName);
    List<Type> bsmSigTypes = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc());
    Type returnType = bsmSigTypes.remove(bsmSigTypes.size() - 1);
    return JavaIdentifierFactory.getInstance()
        .getMethodSignature(bsmCls, methodHandle.getName(), returnType, bsmSigTypes);
  }

  private void convertLookupSwitchInsn(@Nonnull LookupSwitchInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (insnToStmt.containsKey(insn)) {
      frame.mergeIn(currentLineNumber, operandStack.pop());
      return;
    }
    Operand key = operandStack.popImmediate();

    List<IntConstant> keys = new ArrayList<>(insn.keys.size());
    for (Integer i : insn.keys) {
      keys.add(IntConstant.getInstance(i));
    }
    JSwitchStmt lookupSwitchStmt =
        Jimple.newLookupSwitchStmt((Immediate) key.stackOrValue(), keys, getStmtPositionInfo());

    // uphold insertion order!
    stmtsThatBranchToLabel.putAll(lookupSwitchStmt, insn.labels);
    stmtsThatBranchToLabel.put(lookupSwitchStmt, insn.dflt);

    frame.setIn(key);
    setStmt(insn, lookupSwitchStmt);
    key.addUsageInStmt(lookupSwitchStmt);
  }

  private void convertMethodInsn(@Nonnull MethodInsnNode insn) {

    int op = insn.getOpcode();
    boolean isInstance = op != INVOKESTATIC;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    Type returnType;
    if (out == null) {
      String clsName = AsmUtil.toQualifiedName(insn.owner);
      if (clsName.charAt(0) == '[') {
        clsName = "java.lang.Object";
      }
      JavaClassType cls = identifierFactory.getClassType(AsmUtil.toQualifiedName(clsName));
      List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(insn.desc);
      returnType = sigTypes.remove((sigTypes.size() - 1));
      MethodSignature methodSignature =
          identifierFactory.getMethodSignature(cls, insn.name, returnType, sigTypes);
      int nrArgs = sigTypes.size();
      final Operand[] args;
      List<Immediate> argList = Collections.emptyList();
      if (!isInstance) {
        args = nrArgs == 0 ? null : new Operand[nrArgs];
        if (args != null) {
          argList = new ArrayList<>(nrArgs);
        }
      } else {
        args = new Operand[nrArgs + 1];
        if (nrArgs != 0) {
          argList = new ArrayList<>(nrArgs);
        }
      }
      while (nrArgs-- != 0) {
        args[nrArgs] = operandStack.popImmediate(sigTypes.get(nrArgs));
        argList.add((Immediate) args[nrArgs].stackOrValue());
      }
      if (argList.size() > 1) {
        Collections.reverse(argList);
      }
      if (isInstance) {
        args[args.length - 1] = operandStack.popLocal();
      }
      AbstractInvokeExpr invoke;
      if (!isInstance) {
        invoke = Jimple.newStaticInvokeExpr(methodSignature, argList);
      } else {
        Operand baseOperand = args[args.length - 1];
        Local base = (Local) baseOperand.stackOrValue();

        AbstractInstanceInvokeExpr iinvoke;
        switch (op) {
          case INVOKESPECIAL:
            iinvoke = Jimple.newSpecialInvokeExpr(base, methodSignature, argList);
            break;
          case INVOKEVIRTUAL:
            iinvoke = Jimple.newVirtualInvokeExpr(base, methodSignature, argList);
            break;
          case INVOKEINTERFACE:
            iinvoke = Jimple.newInterfaceInvokeExpr(base, methodSignature, argList);
            break;
          default:
            throw new UnsupportedOperationException("Unknown invoke op:" + op);
        }

        invoke = iinvoke;
        baseOperand.addUsageInExpr(invoke);
      }
      if (args != null) {
        for (int i = 0; i < sigTypes.size(); i++) {
          args[i].addUsageInExpr(invoke);
        }
        frame.setIn(args);
      }
      opr = new Operand(insn, invoke, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      AbstractInvokeExpr expr = (AbstractInvokeExpr) opr.value;
      List<Type> types = expr.getMethodSignature().getParameterTypes();
      Operand[] oprs;
      int nrArgs = types.size();
      // TODO: check equivalent to isInstance?
      boolean isInstanceMethod = expr instanceof AbstractInstanceInvokeExpr;
      if (!isInstanceMethod) {
        oprs = nrArgs == 0 ? null : new Operand[nrArgs];
      } else {
        oprs = new Operand[nrArgs + 1];
      }
      if (oprs != null) {
        while (nrArgs-- != 0) {
          oprs[nrArgs] = operandStack.pop(types.get(nrArgs));
        }
        if (isInstanceMethod) {
          oprs[oprs.length - 1] = operandStack.pop();
        }

        frame.mergeIn(currentLineNumber, oprs);
      }
      returnType = expr.getMethodSignature().getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (returnType != VoidType.getInstance()) {
      operandStack.push(opr);
    } else if (!insnToStmt.containsKey(insn)) {
      JInvokeStmt stmt =
          Jimple.newInvokeStmt((AbstractInvokeExpr) opr.value, getStmtPositionInfo());
      setStmt(insn, stmt);
      opr.addUsageInStmt(stmt);
    }
    /*
     * assign all read ops in case the method modifies any of the fields
     */
    addReadOperandAssignments();
  }

  private void convertInvokeDynamicInsn(@Nonnull InvokeDynamicInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    Type returnType;
    if (out == null) {
      // convert info on bootstrap method
      MethodSignature bsmMethodRef = toMethodSignature(insn.bsm);
      List<Immediate> bsmMethodArgs = new ArrayList<>(insn.bsmArgs.length);
      for (Object bsmArg : insn.bsmArgs) {
        bsmMethodArgs.add(toSootValue(bsmArg));
      }

      // create ref to actual method
      JavaClassType bclass =
          identifierFactory.getClassType(JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME);

      // Generate parameters & returnType & parameterTypes
      List<Type> types = AsmUtil.toJimpleSignatureDesc(insn.desc);
      int nrArgs = types.size() - 1;
      List<Type> parameterTypes = new ArrayList<>(nrArgs);
      List<Immediate> methodArgs = new ArrayList<>(nrArgs);

      Operand[] args = new Operand[nrArgs];
      // Beware: Call stack is FIFO, Jimple is linear

      for (int i = nrArgs - 1; i >= 0; i--) {
        parameterTypes.add(types.get(i));
        args[i] = operandStack.popImmediate(types.get(i));
        methodArgs.add((Immediate) args[i].stackOrValue());
      }
      if (methodArgs.size() > 1) {
        Collections.reverse(methodArgs); // Call stack is FIFO, Jimple is linear
        Collections.reverse(parameterTypes);
      }
      returnType = types.get(types.size() - 1);

      // we always model invokeDynamic method refs as static method references
      // of methods on the type SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME
      MethodSignature methodSig =
          identifierFactory.getMethodSignature(bclass, insn.name, returnType, parameterTypes);

      JDynamicInvokeExpr indy =
          Jimple.newDynamicInvokeExpr(
              bsmMethodRef, bsmMethodArgs, methodSig, insn.bsm.getTag(), methodArgs);
      for (int i = 0; i < types.size() - 1; i++) {
        args[i].addUsageInExpr(indy);
      }

      frame.setIn(args);
      opr = new Operand(insn, indy, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      AbstractInvokeExpr expr = (AbstractInvokeExpr) opr.value;
      List<Type> types = expr.getMethodSignature().getParameterTypes();
      Operand[] oprs;
      int nrArgs = types.size() - 1;
      final boolean isStaticInvokeExpr = expr instanceof JStaticInvokeExpr;
      if (isStaticInvokeExpr) {
        oprs = (nrArgs <= 0) ? null : new Operand[nrArgs];
      } else {
        oprs = (nrArgs < 0) ? null : new Operand[nrArgs + 1];
      }
      if (oprs != null) {
        while (nrArgs-- > 0) {
          oprs[nrArgs] = operandStack.pop(types.get(nrArgs));
        }
        if (!isStaticInvokeExpr) {
          oprs[oprs.length - 1] = operandStack.pop();
        }
        frame.mergeIn(currentLineNumber, oprs);
      }
      returnType = expr.getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (!(returnType instanceof VoidType)) {
      operandStack.push(opr);
    } else if (!insnToStmt.containsKey(insn)) {
      JInvokeStmt stmt =
          Jimple.newInvokeStmt((AbstractInvokeExpr) opr.value, getStmtPositionInfo());
      setStmt(insn, stmt);
      opr.addUsageInStmt(stmt);
    }
    /*
     * assign all read ops in case the method modifies any of the fields
     */
    addReadOperandAssignments();
  }

  // private @Nonnull MethodRef toSootMethodRef(@Nonnull Handle methodHandle) {
  // String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
  // JavaClassType bsmCls = view.getIdentifierFactory().getClassSignature(bsmClsName);
  // List<Type> bsmSigTypes = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc(), view);
  // Type returnType = bsmSigTypes.remove(bsmSigTypes.size() - 1);
  // MethodSignature methodSignature =
  // view.getIdentifierFactory().getMethodSignature(methodHandle.getName(), bsmCls,
  // returnType, bsmSigTypes);
  // boolean isStatic = methodHandle.getTag() == MethodHandle.Kind.REF_INVOKE_STATIC.getValue();
  // return Jimple.createSymbolicMethodRef(methodSignature, isStatic);
  // }
  //
  // private JFieldRef toSootFieldRef(Handle methodHandle) {
  // String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
  // JavaClassType bsmCls = view.getIdentifierFactory().getClassSignature(bsmClsName);
  //
  // Type t = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc(), view).get(0);
  // int kind = methodHandle.getTag();
  // boolean isStatic = kind == MethodHandle.Kind.REF_GET_FIELD_STATIC.getValue()
  // || kind == MethodHandle.Kind.REF_PUT_FIELD_STATIC.getValue();
  //
  // FieldSignature fieldSignature =
  // view.getIdentifierFactory().getFieldSignature(methodHandle.getName(), bsmCls, t);
  // return Jimple.createSymbolicFieldRef(fieldSignature, isStatic);
  // }

  private void convertMultiANewArrayInsn(@Nonnull MultiANewArrayInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      ArrayType t = (ArrayType) AsmUtil.toJimpleType(insn.desc);
      int dims = insn.dims;
      Operand[] sizes = new Operand[dims];
      Immediate[] sizeVals = new Immediate[dims];
      while (dims-- != 0) {
        sizes[dims] = operandStack.popImmediate();
        sizeVals[dims] = (Immediate) sizes[dims].stackOrValue();
      }
      JNewMultiArrayExpr nm = Jimple.newNewMultiArrayExpr(t, Arrays.asList(sizeVals));
      for (int i = 0; i < dims; i++) {
        sizes[i].addUsageInExpr(nm);
      }
      frame.setIn(sizes);
      opr = new Operand(insn, nm, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      int dims = insn.dims;
      Operand[] sizes = new Operand[dims];
      while (dims-- != 0) {
        sizes[dims] = operandStack.pop();
      }
      frame.mergeIn(currentLineNumber, sizes);
    }
    operandStack.push(opr);
  }

  private void convertTableSwitchInsn(@Nonnull TableSwitchInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (insnToStmt.containsKey(insn)) {
      frame.mergeIn(currentLineNumber, operandStack.pop());
      return;
    }
    Operand key = operandStack.popImmediate();
    JSwitchStmt tableSwitchStmt =
        Jimple.newTableSwitchStmt(
            (Immediate) key.stackOrValue(), insn.min, insn.max, getStmtPositionInfo());

    // uphold insertion order!
    stmtsThatBranchToLabel.putAll(tableSwitchStmt, insn.labels);
    stmtsThatBranchToLabel.put(tableSwitchStmt, insn.dflt);

    frame.setIn(key);
    setStmt(insn, tableSwitchStmt);
    key.addUsageInStmt(tableSwitchStmt);
  }

  private void convertTypeInsn(@Nonnull TypeInsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      Expr val;
      if (op == NEW) {
        val = Jimple.newNewExpr(AsmUtil.toJimpleClassType(insn.desc));
      } else {
        Operand op1 = operandStack.popImmediate();
        Value v1 = op1.stackOrValue();
        switch (op) {
          case ANEWARRAY:
            {
              JNewArrayExpr expr =
                  JavaJimple.getInstance()
                      .newNewArrayExpr(AsmUtil.arrayTypetoJimpleType(insn.desc), (Immediate) v1);
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          case CHECKCAST:
            {
              JCastExpr expr =
                  Jimple.newCastExpr((Immediate) v1, AsmUtil.toJimpleClassType(insn.desc));
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          case INSTANCEOF:
            {
              JInstanceOfExpr expr =
                  Jimple.newInstanceOfExpr((Immediate) v1, AsmUtil.toJimpleClassType(insn.desc));
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          default:
            throw new UnsupportedOperationException("Unknown type op: " + op);
        }
        op1.addUsageInExpr(val);
        frame.setIn(op1);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      if (op != NEW) {
        frame.mergeIn(currentLineNumber, operandStack.pop());
      }
    }
    operandStack.push(opr);
  }

  private void convertVarLoadInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LLOAD || op == DLOAD;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      opr = new Operand(insn, getOrCreateLocal(insn.var), this);
      frame.setOut(opr);
    } else {
      opr = out[0];
    }
    if (dword) {
      operandStack.pushDual(opr);
    } else {
      operandStack.push(opr);
    }
  }

  private void convertVarStoreInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LSTORE || op == DSTORE;
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    Operand opr = dword ? operandStack.popDual() : operandStack.pop();
    Local local = getOrCreateLocal(insn.var);
    if (!insnToStmt.containsKey(insn)) {
      AbstractDefinitionStmt<Local, ?> as =
          Jimple.newAssignStmt(local, opr.stackOrValue(), getStmtPositionInfo());
      frame.setIn(opr);
      setStmt(insn, as);
      opr.addUsageInStmt(as);
    } else {
      frame.mergeIn(currentLineNumber, opr);
    }
    addReadOperandAssignments(local);
  }

  private void convertVarInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    if (op >= ILOAD && op <= ALOAD) {
      convertVarLoadInsn(insn);
    } else if (op >= ISTORE && op <= ASTORE) {
      convertVarStoreInsn(insn);
    } else if (op == RET) {
      /* we handle it, even though it should be removed */
      if (!insnToStmt.containsKey(insn)) {
        setStmt(insn, Jimple.newRetStmt(getOrCreateLocal(insn.var), getStmtPositionInfo()));
      }
    } else {
      throw new UnsupportedOperationException("Unknown var op: " + op);
    }
  }

  private void convertLabel(@Nonnull LabelNode ln) {
    // only do it for Labels which are referring to a traphandler
    if (!trapHandler.containsKey(ln)) {
      return;
    }

    // We create a nop statement as a placeholder so that we can jump
    // somewhere from the real exception handler in case this is inline
    // code
    if (inlineExceptionLabels.contains(ln)) {
      if (!insnToStmt.containsKey(ln)) {
        JNopStmt nop = Jimple.newNopStmt(getStmtPositionInfo());
        setStmt(ln, nop);
      }
      return;
    }

    StackFrame frame = operandStack.getOrCreateStackframe(ln);
    Operand[] out = frame.getOut();
    Operand opr;
    if (out == null) {
      JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
      Local stack = newStackLocal();
      AbstractDefinitionStmt<Local, JCaughtExceptionRef> as =
          Jimple.newIdentityStmt(stack, ref, getStmtPositionInfo());
      opr = new Operand(ln, ref, this);
      opr.stackLocal = stack;
      frame.setOut(opr);
      setStmt(ln, as);
      opr.addUsageInStmt(as);
    } else {
      opr = out[0];
    }
    operandStack.push(opr);
  }

  private void convertLine(@Nonnull LineNumberNode ln) {
    currentLineNumber = ln.line;
    if (currentLineNumber > maxLineNumber) {
      maxLineNumber = currentLineNumber;
    }
  }

  /* Conversion */
  private void addEdges(
      @Nonnull Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges,
      @Nonnull ArrayDeque<BranchedInsnInfo> conversionWorklist,
      @Nonnull AbstractInsnNode branchingInsn, /*  branching instruction node */
      @Nonnull
          AbstractInsnNode
              tgt, /* "default" targets i.e. LabelNode or fallsthrough "target" of if  */
      @Nonnull List<LabelNode> tgts /* other branch target(s) */) {
    Operand[] stackss = operandStack.getStack().toArray(new Operand[0]);
    /* iterate over possible following/successing instructions which is: combined(tgt, tgts) */
    int i = 0;
    int lastIdx = tgts.size();
    outer_loop:
    do {
      BranchedInsnInfo edge = edges.get(branchingInsn, tgt);
      if (edge == null) {
        // [ms] check why this edge could be already there
        edge = new BranchedInsnInfo(tgt, operandStack.getStack());
        edge.addToPrevStack(stackss);
        edges.put(branchingInsn, tgt, edge);
        conversionWorklist.add(edge);
        continue;
      }
      for (List<Operand> stackTemp : edge.getOperandStacks()) {
        if (stackTemp.size() == stackss.length) {
          int j = 0;
          while (j < stackss.length && stackTemp.get(j).equivTo(stackss[j])) {
            j++;
          }
          if (j == stackss.length) {
            continue outer_loop;
          }
        }
      }
      final LinkedList<Operand[]> prevStacks = edge.getPrevStacks();
      for (Operand[] ps : prevStacks) {
        if (Arrays.equals(ps, stackss)) {
          continue outer_loop;
        }
      }
      edge.addOperandStack(operandStack.getStack());
      edge.addToPrevStack(stackss);
      conversionWorklist.add(edge);
    } while (i < lastIdx && (tgt = tgts.get(i++)) != null);
  }

  private void convert() {
    ArrayDeque<BranchedInsnInfo> worklist = new ArrayDeque<>();

    indexInlineExceptionHandlers();

    // If this label is reachable through an exception and through normal
    // code, we have to split the exceptional case (with the exception on
    // the stack) from the normal fall-through case without anything on the
    // stack.
    for (LabelNode handlerNode : trapHandler.keySet()) {
      if (inlineExceptionLabels.contains(handlerNode)) {
        // Catch the exception
        JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
        Local local = newStackLocal();
        AbstractDefinitionStmt<Local, JCaughtExceptionRef> as =
            Jimple.newIdentityStmt(local, ref, getStmtPositionInfo());

        Operand opr = new Operand(handlerNode, ref, this);
        opr.stackLocal = local;

        worklist.add(new BranchedInsnInfo(handlerNode, Collections.singletonList(opr)));

        // Save the statements
        inlineExceptionHandlers.put(handlerNode, as);
      } else {
        worklist.add(new BranchedInsnInfo(handlerNode, new ArrayList<>()));
      }
    }
    worklist.add(new BranchedInsnInfo(instructions.getFirst(), Collections.emptyList()));
    Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges = HashBasedTable.create(1, 1);

    do {
      BranchedInsnInfo edge = worklist.pollLast();
      AbstractInsnNode insn = edge.getInsn();
      operandStack.setOperandStack(
          new ArrayList<>(edge.getOperandStacks().get(edge.getOperandStacks().size() - 1)));
      do {
        int type = insn.getType();
        if (type == FIELD_INSN) {
          convertFieldInsn((FieldInsnNode) insn);
        } else if (type == IINC_INSN) {
          convertIincInsn((IincInsnNode) insn);
        } else if (type == INSN) {
          convertInsn((InsnNode) insn);
          int op = insn.getOpcode();
          if ((op >= IRETURN && op <= RETURN) || op == ATHROW) {
            break;
          }
        } else if (type == INT_INSN) {
          convertIntInsn((IntInsnNode) insn);
        } else if (type == LDC_INSN) {
          convertLdcInsn((LdcInsnNode) insn);
        } else if (type == JUMP_INSN) {
          JumpInsnNode jmp = (JumpInsnNode) insn;
          convertJumpInsn(jmp);
          int op = jmp.getOpcode();
          if (op == JSR) {
            throw new UnsupportedOperationException("JSR!");
          }
          if (op != GOTO) {
            /* ifX opcode, i.e. two successors */
            AbstractInsnNode next = insn.getNext();
            addEdges(edges, worklist, insn, next, Collections.singletonList(jmp.label));
          } else {
            addEdges(edges, worklist, insn, jmp.label, Collections.emptyList());
          }
          break;
        } else if (type == LOOKUPSWITCH_INSN) {
          LookupSwitchInsnNode swtch = (LookupSwitchInsnNode) insn;
          convertLookupSwitchInsn(swtch);
          LabelNode dflt = swtch.dflt;
          addEdges(edges, worklist, insn, dflt, swtch.labels);
          break;
        } else if (type == METHOD_INSN) {
          convertMethodInsn((MethodInsnNode) insn);
        } else if (type == INVOKE_DYNAMIC_INSN) {
          convertInvokeDynamicInsn((InvokeDynamicInsnNode) insn);
        } else if (type == MULTIANEWARRAY_INSN) {
          convertMultiANewArrayInsn((MultiANewArrayInsnNode) insn);
        } else if (type == TABLESWITCH_INSN) {
          TableSwitchInsnNode swtch = (TableSwitchInsnNode) insn;
          convertTableSwitchInsn(swtch);
          LabelNode dflt = swtch.dflt;
          addEdges(edges, worklist, insn, dflt, swtch.labels);
          break;
        } else if (type == TYPE_INSN) {
          convertTypeInsn((TypeInsnNode) insn);
        } else if (type == VAR_INSN) {
          if (insn.getOpcode() == RET) {
            throw new UnsupportedOperationException("RET!");
          }
          convertVarInsn((VarInsnNode) insn);
        } else if (type == LABEL) {
          convertLabel((LabelNode) insn);
        } else if (type == LINE) {
          convertLine((LineNumberNode) insn);
        } else
        //noinspection StatementWithEmptyBody
        if (type == FRAME) {
          // we can ignore it
        } else {
          throw new RuntimeException("Unknown instruction type: " + type);
        }
      } while ((insn = insn.getNext()) != null);
    } while (!worklist.isEmpty());
  }

  // inline exceptionhandler := exceptionhandler thats reachable through unexceptional "normal" flow
  // and exceptional flow
  private void indexInlineExceptionHandlers() {
    final Set<LabelNode> handlerLabelNodes = trapHandler.keySet();

    if (handlerLabelNodes.isEmpty()) {
      // my job is done here
      return;
    }

    for (AbstractInsnNode node : instructions) {
      if (node instanceof JumpInsnNode) {
        final LabelNode handlerLabel = ((JumpInsnNode) node).label;
        if (handlerLabelNodes.contains(handlerLabel)) {
          inlineExceptionLabels.add(handlerLabel);
        }
      } else if (node instanceof LookupSwitchInsnNode) {

        final LookupSwitchInsnNode lookupSwitchInsnNode = (LookupSwitchInsnNode) node;
        if (handlerLabelNodes.contains(lookupSwitchInsnNode.dflt)) {
          inlineExceptionLabels.add(lookupSwitchInsnNode.dflt);
          continue;
        }
        for (LabelNode l : lookupSwitchInsnNode.labels) {
          if (handlerLabelNodes.contains(l)) {
            inlineExceptionLabels.add(l);
            break;
          }
        }
      } else if (node instanceof TableSwitchInsnNode) {

        final TableSwitchInsnNode tableSwitchInsnNode = (TableSwitchInsnNode) node;
        if (handlerLabelNodes.contains(tableSwitchInsnNode.dflt)) {
          inlineExceptionLabels.add(tableSwitchInsnNode.dflt);
          continue;
        }
        for (LabelNode l : tableSwitchInsnNode.labels) {
          if (handlerLabelNodes.contains(l)) {
            inlineExceptionLabels.add(l);
            break;
          }
        }
      }
    }
  }

  public static LineNumberNode findLineInfo(
      @Nonnull InsnList insnList, @Nonnull AbstractInsnNode insnNode) {
    int idx = insnList.indexOf(insnNode);
    if (idx < 0) {
      return null;
    }

    // Get index of labels and insnNode within method
    ListIterator<AbstractInsnNode> insnIt = insnList.iterator(idx);
    while (insnIt.hasPrevious()) {
      AbstractInsnNode node = insnIt.previous();

      if (node instanceof LineNumberNode) {
        return (LineNumberNode) node;
      }
    }
    return null;
  }

  @Nonnull
  private StmtPositionInfo getFirstLineOfMethod() {
    for (AbstractInsnNode node : instructions) {
      if (node instanceof LineNumberNode) {
        return new SimpleStmtPositionInfo(((LineNumberNode) node).line);
      }
    }
    return StmtPositionInfo.createNoStmtPositionInfo();
  }

  @Nonnull
  private List<Stmt> buildPreambleLocals(Body.BodyBuilder bodyBuilder) {

    List<Stmt> preambleBlock = new ArrayList<>();
    MethodSignature methodSignature = lazyMethodSignature.get();
    final StmtPositionInfo methodPosInfo = getFirstLineOfMethod();

    int localIdx = 0;
    // create this Local if necessary ( i.e. not static )
    if (!bodyBuilder.getModifiers().contains(MethodModifier.STATIC)) {
      JavaLocal thisLocal = JavaJimple.newLocal(determineLocalName(localIdx), declaringClass);
      locals.set(localIdx++, thisLocal);
      final JIdentityStmt<JThisRef> stmt =
          Jimple.newIdentityStmt(thisLocal, Jimple.newThisRef(declaringClass), methodPosInfo);
      preambleBlock.add(stmt);
    }
    // add parameter Locals
    for (int i = 0; i < methodSignature.getParameterTypes().size(); i++) {
      Type parameterType = methodSignature.getParameterTypes().get(i);
      // [BH] parameterlocals do not exist yet -> create with annotation
      JavaLocal local =
          JavaJimple.newLocal(
              determineLocalName(localIdx),
              parameterType,
              AsmUtil.createAnnotationUsage(
                  invisibleParameterAnnotations == null ? null : invisibleParameterAnnotations[i]));
      locals.set(localIdx, local);

      final JIdentityStmt<JParameterRef> stmt =
          Jimple.newIdentityStmt(local, Jimple.newParameterRef(parameterType, i), methodPosInfo);
      preambleBlock.add(stmt);

      // see https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.6.1
      if (AsmUtil.isDWord(parameterType)) {
        localIdx += 2;
      } else {
        localIdx++;
      }
    }

    return preambleBlock;
  }

  private List<Trap> buildTraps() {
    List<Trap> traps = new ArrayList<>();
    for (TryCatchBlockNode trycatch : tryCatchBlocks) {
      Stmt handler = trapHandler.get(trycatch.handler);
      if (handler == null) {
        throw new IllegalStateException(
            "Label for the TrapHandler "
                + trycatch.handler
                + " has no associated Stmt to jump to.");
      }

      // FIXME: [ms] create java.lang.Throwable for modules i.e. with ModuleSignature if modules are
      // used..
      final String exceptionName =
          (trycatch.type != null) ? AsmUtil.toQualifiedName(trycatch.type) : "java.lang.Throwable";
      JavaClassType exceptionType = identifierFactory.getClassType(exceptionName);

      Trap trap =
          Jimple.newTrap(
              exceptionType,
              labelsToStmt.get(trycatch.start),
              labelsToStmt.get(trycatch.end),
              handler);
      traps.add(trap);
    }
    return traps;
  }

  /** all Instructions are converted. Now they can be arranged into the StmtGraph. */
  private void arrangeStmts(
      MutableBlockStmtGraph graph, List<Stmt> stmtList, Body.BodyBuilder builder) {

    AbstractInsnNode insn = instructions.getFirst();
    ArrayDeque<LabelNode> danglingLabel = new ArrayDeque<>();

    Map<ClassType, Stmt> currentTraps = new HashMap<>();

    // (n, n+1) := (from, to)
    // List<Stmt> connectBlocks = new ArrayList<>();
    // every LabelNode denotes a border of a Block

    do {

      // assign Stmt associated with the current instruction. see
      // https://asm.ow2.io/javadoc/org/objectweb/asm/Label.html
      // there can be multiple labels assigned to the following stmt! (and other AbstractNodes in
      // between!)
      final boolean isLabelNode = insn instanceof LabelNode;
      if (isLabelNode) {
        // Save the label to assign it then to the next real Stmt
        danglingLabel.add((LabelNode) insn);
      }

      Stmt stmt = insnToStmt.get(insn);
      if (stmt == null) {
        continue;
      }

      if (!danglingLabel.isEmpty()) {
        // there is (at least) a LabelNode ->
        // associate collected labels from danglingLabel with the following stmt
        Stmt targetStmt =
            stmt instanceof StmtContainer ? ((StmtContainer) stmt).getFirstStmt() : stmt;
        danglingLabel.forEach(l -> labelsToStmt.put(l, targetStmt));
        if (isLabelNode) {
          // If the targetStmt is an exception handler, register the starting Stmt for it
          JIdentityStmt<?> identityRef = findIdentityRefInStmtContainer(stmt);
          if (identityRef != null && identityRef.getRightOp() instanceof JCaughtExceptionRef) {
            danglingLabel.forEach(label -> trapHandler.put(label, identityRef));
          }
        }
        danglingLabel.clear();
      }

      emitStmt(stmt, stmtList);

    } while ((insn = insn.getNext()) != null);

    Map<BranchingStmt, List<Stmt>> branchingMap = new HashMap<>();
    for (Map.Entry<BranchingStmt, Collection<LabelNode>> entry :
        stmtsThatBranchToLabel.asMap().entrySet()) {
      final BranchingStmt fromStmt = entry.getKey();
      List<Stmt> targets = new ArrayList<>();
      for (LabelNode labelNode : entry.getValue()) {
        final Stmt targetStmt = labelsToStmt.get(labelNode);
        if (targetStmt == null) {
          throw new IllegalStateException(
              "targetStmt not found for fromStmt"
                  + fromStmt
                  + " "
                  + entry.getValue()
                  + " in method "
                  + lazyMethodSignature.get());
        }
        targets.add(targetStmt);
      }
      branchingMap.put(fromStmt, targets);
    }

    final List<Trap> traps = buildTraps();
    // TODO: performance: [ms] we already know Blocks borders from the label information -> use
    // addBlocks+collect trap data and connect blocks afterwards via branching information +
    // collected fallsthroughBlock information
    graph.initializeWith(stmtList, branchingMap, traps);

    // Emit the inline exception handler blocks i.e. those that are reachable without exceptional
    // flow
    // FIXME:[ms] the following code seems odd.. we need a testcase to test inlineexceptionhandling!
    for (Entry<LabelNode, AbstractDefinitionStmt<Local, JCaughtExceptionRef>> entry :
        inlineExceptionHandlers.entrySet()) {

      AbstractDefinitionStmt<Local, JCaughtExceptionRef> handlerStmt = entry.getValue();
      emitStmt(handlerStmt, stmtList);
      trapHandler.put(entry.getKey(), handlerStmt);
      // TODO: update handlerStmts positioninfo!

      // jump back to the original implementation
      JGotoStmt gotoStmt = Jimple.newGotoStmt(handlerStmt.getPositionInfo());
      stmtList.add(gotoStmt);

      // add stmtList to graph
      graph.addBlock(stmtList, currentTraps);
      stmtList.clear();

      // connect tail of stmtList with its target
      Stmt targetStmt = insnToStmt.get(entry.getKey());
      graph.putEdge(gotoStmt, targetStmt);
    }
  }

  private void emitStmt(@Nonnull Stmt handlerStmt, @Nonnull List<Stmt> block) {
    if (handlerStmt instanceof StmtContainer) {
      block.addAll(((StmtContainer) handlerStmt).getStmts());
    } else {
      block.add(handlerStmt);
    }
  }

  @Nullable
  private JIdentityStmt<?> findIdentityRefInStmtContainer(@Nonnull Stmt stmt) {
    if (stmt instanceof JIdentityStmt) {
      return (JIdentityStmt<?>) stmt;
    } else if (stmt instanceof StmtContainer) {
      for (Stmt stmtEntry : ((StmtContainer) stmt).getStmts()) {
        if (stmtEntry instanceof JIdentityStmt) {
          return (JIdentityStmt<?>) stmtEntry;
        }
      }
    }
    return null;
  }

  /**
   * Returns the latest version of a statement that is used in this method source, or null if the
   * statement is not used
   *
   * @param oldStmt the Stmt which we want to check if there is a newer Stmt replacing it
   * @return the most recent version of a Stmt or itself if there is no newer version. Otherwise
   *     returns null.
   */
  @Nonnull
  Stmt getLatestVersionOfStmt(@Nonnull Stmt oldStmt) {
    while (true) {
      final Stmt replacedVersion = replacedStmt.get(oldStmt);
      if (replacedVersion != null) {
        oldStmt = replacedVersion;
      } else {
        return oldStmt;
      }
    }
  }

  void replaceStmt(@Nonnull Stmt oldStmt, Stmt newStmt) {
    AbstractInsnNode key = null;

    // TODO: [ms] bit expensive and called a lot? -> find better solution!
    for (Entry<AbstractInsnNode, Stmt> entry : insnToStmt.entrySet()) {
      if (Objects.equals(oldStmt, entry.getValue())) {
        key = entry.getKey();
      }
    }

    if (key == null) {
      // throw new IllegalStateException("Could not replace value in insn map because oldStmt " +
      // oldStmt + " it is absent");
      return;
    }

    if (newStmt == null) {
      insnToStmt.remove(key);
      return;
    }

    insnToStmt.put(key, newStmt);
    replacedStmt.put(oldStmt, newStmt);

    if (oldStmt instanceof BranchingStmt) {
      List<LabelNode> branchLabels = stmtsThatBranchToLabel.get((BranchingStmt) oldStmt);
      if (branchLabels != null) {
        branchLabels.forEach(bl -> stmtsThatBranchToLabel.put((BranchingStmt) newStmt, bl));
        stmtsThatBranchToLabel.removeAll(oldStmt);
      }
    }
  }

  /**
   * * returns all stmts that use this expr
   *
   * @param expr which is used to filter associated Stmts
   */
  public Stream<Stmt> getStmtsThatUse(@Nonnull Expr expr) {
    Stream<Stmt> currentUses =
        insnToStmt.values().stream()
            .flatMap(
                stmt ->
                    stmt instanceof StmtContainer
                        ? ((StmtContainer) stmt).getStmts().stream()
                        : Stream.of(stmt))
            .filter(stmt -> stmt.getUses().contains(expr));

    Stream<Stmt> oldMappedUses =
        replacedStmt.entrySet().stream()
            .filter(stmt -> stmt.getKey().getUses().contains(expr))
            .map(stmt -> getLatestVersionOfStmt(stmt.getValue()));

    return Stream.concat(currentUses, oldMappedUses);
  }
}
