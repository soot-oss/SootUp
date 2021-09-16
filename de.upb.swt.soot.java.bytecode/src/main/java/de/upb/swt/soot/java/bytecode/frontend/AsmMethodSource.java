package de.upb.swt.soot.java.bytecode.frontend;
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
import de.upb.swt.soot.core.frontend.BodySource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.MethodHandle;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractBinopExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractUnopExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.expr.JAddExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JInstanceOfExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewArrayExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewMultiArrayExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractOpStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.ConstantUtil;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.jimple.basic.JavaLocal;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.*;

/**
 * A {@link BodySource} that can read Java bytecode
 *
 * @author Andreas Dann
 */
public class AsmMethodSource extends JSRInlinerAdapter implements BodySource {

  static final Operand DWORD_DUMMY = new Operand(null, null, null);

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
  private LinkedListMultimap<Stmt, LabelNode> stmtsThatBranchToLabel;
  private Map<AbstractInsnNode, Stmt> InsnToStmt;

  @Nonnull private final Map<Stmt, Stmt> replacedStmt = new HashMap<>();

  private OperandStack operandStack;
  private Map<LabelNode, Stmt> trapHandler;
  private int lastLineNumber = -1;

  @Nullable private JavaClassType declaringClass;
  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  @Nonnull private final Set<LabelNode> inlineExceptionLabels = new HashSet<>();
  @Nonnull private final Map<LabelNode, Stmt> inlineExceptionHandlers = new HashMap<>();

  private Map<LabelNode, Stmt> labelsToStmt;
  @Nonnull private final Body.BodyBuilder bodyBuilder = Body.builder();

  Stmt rememberedStmt = null;
  boolean isFirstStmtSet = false;

  private final Supplier<MethodSignature> lazyMethodSignature =
      Suppliers.memoize(
          () -> {
            List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(desc);
            Type retType = sigTypes.remove(sigTypes.size() - 1);

            return JavaIdentifierFactory.getInstance()
                .getMethodSignature(name, declaringClass, retType, sigTypes);
          });

  AsmMethodSource(
      int access,
      @Nonnull String name,
      @Nonnull String desc,
      @Nonnull String signature,
      @Nonnull String[] exceptions,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    super(AsmUtil.SUPPORTED_ASM_OPCODE, null, access, name, desc, signature, exceptions);
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  @Nonnull
  public MethodSignature getSignature() {
    return lazyMethodSignature.get();
  }

  void setDeclaringClass(@Nonnull ClassType declaringClass) {
    this.declaringClass = (JavaClassType) declaringClass;
  }

  @Override
  @Nonnull
  public Body resolveBody(@Nonnull Iterable<Modifier> modifiers) {
    bodyBuilder.setModifiers(AsmUtil.getModifiers(access));

    /* initialize */
    int nrInsn = instructions.size();
    nextLocal = maxLocals;
    locals =
        new NonIndexOutofBoundsArrayList<>(
            maxLocals
                + Math.max((maxLocals / 2), 5)); // [ms] initial capacity is just roughly estimated.
    stmtsThatBranchToLabel = LinkedListMultimap.create();
    InsnToStmt = new LinkedHashMap<>(nrInsn);
    operandStack = new OperandStack(this, nrInsn);
    trapHandler = new LinkedHashMap<>(tryCatchBlocks.size());

    /* retrieve all trap handlers */
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      trapHandler.put(tc.handler, Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));
    }
    /* convert instructions */
    try {
      convert();
    } catch (Exception e) {
      instructions.iterator().forEachRemaining(item -> System.out.println(AsmUtil.toString(item)));
      throw new RuntimeException("Failed to convert " + lazyMethodSignature.get(), e);
    }

    /* build body (add stmts, locals, traps, etc.) */
    buildLocals();
    buildStmts();
    buildTraps();

    // FIXME: [AD] add real line number
    Position bodyPos = NoPositionInformation.getInstance();
    bodyBuilder.setPosition(bodyPos);

    /* clean up references for GC */
    locals = null;
    stmtsThatBranchToLabel = null;
    InsnToStmt = null;
    operandStack = null;

    bodyBuilder.setMethodSignature(lazyMethodSignature.get());

    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      try {
        bodyInterceptor.interceptBody(bodyBuilder);
      } catch (Exception e) {
        throw new RuntimeException(
            "Failed to apply " + bodyInterceptor + " to " + lazyMethodSignature.get(), e);
      }
    }
    return bodyBuilder.build();
  }

  @Override
  public Object resolveDefaultValue() {
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
    return ConstantUtil.fromObject(a);
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
    // FIXME: [AD] re-add linenumber keep
    // ASM LineNumberNode
    // if (Options.keep_line_number() && lastLineNumber >= 0) {
    // Tag lineTag = stmt.getTag("LineNumberTag");
    // if (lineTag == null) {
    // lineTag = new LineNumberTag(lastLineNumber);
    // stmt.addTag(lineTag);
    // } else if (((LineNumberTag) lineTag).getLineNumber() != lastLineNumber) {
    // throw new RuntimeException("Line tag mismatch");
    // }
    // }

    Stmt overwrittenStmt = InsnToStmt.put(insn, stmt);
    if (overwrittenStmt != null) {
      throw new AssertionError(
          insn.getOpcode() + " already has an associated Stmt: " + overwrittenStmt);
    }
  }

  void mergeStmts(@Nonnull AbstractInsnNode insn, @Nonnull Stmt stmt) {
    Stmt prev = InsnToStmt.put(insn, stmt);
    if (prev != null) {
      Stmt merged = StmtContainer.create(prev, stmt);
      InsnToStmt.put(insn, merged);
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
    return (A) InsnToStmt.get(insn);
  }

  private void assignReadOps(@Nullable Local local) {
    for (Operand operand : operandStack.getStack()) {
      if (operand == DWORD_DUMMY
          || operand.stackLocal != null
          || (local == null && operand.value instanceof Local)) {
        continue;
      }
      if (local != null && !operand.value.equivTo(local)) {
        List<Value> uses = operand.value.getUses();
        boolean noRef = true;
        for (Value use : uses) {
          if (use.equivTo(local)) {
            noRef = false;
            break;
          }
        }
        if (noRef) {
          continue;
        }
      }
      int op = operand.insn.getOpcode();

      // FIXME: [JMP] The IF condition is always false. --> [ms]: *ALOAD are array load instructions
      // -> seems someone wanted to include or exclude the array instructions?
      if (local == null && op != GETFIELD && op != GETSTATIC && (op < IALOAD && op > SALOAD)) {
        continue;
      }

      Local stackLocal = newStackLocal();
      operand.stackLocal = stackLocal;
      JAssignStmt<Local, ?> asssignStmt =
          Jimple.newAssignStmt(
              stackLocal, operand.value, StmtPositionInfo.createNoStmtPositionInfo());

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
      JavaClassType declClass =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(insn.owner));
      type = AsmUtil.toJimpleType(insn.desc);
      JFieldRef val;
      FieldSignature ref;
      if (insn.getOpcode() == GETSTATIC) {
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
      } else {
        Operand base = operandStack.popLocal();
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        val = Jimple.newInstanceFieldRef((Local) base.stackOrValue(), ref);
        frame.setIn(base);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      type = ((JFieldRef) opr.value).getFieldSignature().getType();
      if (insn.getOpcode() == GETFIELD) {
        frame.mergeIn(operandStack.pop());
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
      JavaClassType declClass =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(insn.owner));
      type = AsmUtil.toJimpleType(insn.desc);

      JFieldRef val;
      FieldSignature ref;
      rvalue = operandStack.popImmediate(type);
      if (notInstance) {
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
        frame.setIn(rvalue);
      } else {
        Operand base = operandStack.popLocal();
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        JInstanceFieldRef ifr = Jimple.newInstanceFieldRef((Local) base.stackOrValue(), ref);
        val = ifr;
        frame.setIn(rvalue, base);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
      JAssignStmt<JFieldRef, ?> as =
          Jimple.newAssignStmt(
              val, rvalue.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());

      setStmt(insn, as);
      rvalue.addUsageInStmt(as);
    } else {
      opr = out[0];
      type = ((JFieldRef) opr.value).getFieldSignature().getType();
      rvalue = operandStack.pop(type);
      if (notInstance) {
        /* PUTSTATIC only needs one operand on the stack, the rvalue */
        frame.mergeIn(rvalue);
      } else {
        /* PUTFIELD has a rvalue and a base */
        frame.mergeIn(rvalue, operandStack.pop());
      }
    }
    /*
     * in case any static field or array is read from, and the static constructor or the field this instruction writes to,
     * modifies that field, write out any previous read from field/array
     */
    assignReadOps(null);
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
    assignReadOps(local);
    if (!InsnToStmt.containsKey(insn)) {
      JAddExpr add = Jimple.newAddExpr(local, IntConstant.getInstance(insn.incr));
      setStmt(insn, Jimple.newAssignStmt(local, add, StmtPositionInfo.createNoStmtPositionInfo()));
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
        throw new AssertionError("Unknown constant opcode: " + op);
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
      frame.mergeIn(operandStack.pop(), operandStack.pop());
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
    if (!InsnToStmt.containsKey(insn)) {
      Operand valueOp = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      Operand indexOp = operandStack.popImmediate();
      Operand baseOp = operandStack.popLocal();
      JArrayRef ar =
          JavaJimple.getInstance()
              .newArrayRef((Local) baseOp.stackOrValue(), (Immediate) indexOp.stackOrValue());
      JAssignStmt<JArrayRef, ?> as =
          Jimple.newAssignStmt(
              ar, valueOp.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
      frame.setIn(valueOp, indexOp, baseOp);
      setStmt(insn, as);
      valueOp.addUsageInStmt(as);

    } else {
      frame.mergeIn(
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
      if (operandStack.peek() == DWORD_DUMMY) {
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
          operandStack.peek() == DWORD_DUMMY ? operandStack.pop() : operandStack.popImmediate();
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
          operandStack.peek() == DWORD_DUMMY ? operandStack.pop() : operandStack.popImmediate();
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
        throw new AssertionError("Unknown binop: " + op);
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

          frame.mergeIn(operandStack.popDual(), operandStack.popDual());
        } else {
          frame.mergeIn(operandStack.pop(), operandStack.popDual());
        }
      } else {
        frame.mergeIn(operandStack.pop(), operandStack.pop());
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
        throw new AssertionError("Unknown unop: " + op);
      }
      op1.addUsageInExpr(unop);
      opr = new Operand(insn, unop, this);
      frame.setIn(op1);
      frame.setOut(opr);
    } else {
      opr = out[0];
      frame.mergeIn(dword ? operandStack.popDual() : operandStack.pop());
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
          throw new AssertionError("Unknonw prim cast op: " + op);
      }
      Operand val = fromd ? operandStack.popImmediateDual() : operandStack.popImmediate();
      JCastExpr cast = Jimple.newCastExpr((Immediate) val.stackOrValue(), totype);
      opr = new Operand(insn, cast, this);
      val.addUsageInExpr(cast);
      frame.setIn(val);
      frame.setOut(opr);
    } else {
      opr = out[0];
      frame.mergeIn(fromd ? operandStack.popDual() : operandStack.pop());
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
    if (!InsnToStmt.containsKey(insn)) {
      Operand val = dword ? operandStack.popImmediateDual() : operandStack.popImmediate();
      JReturnStmt ret =
          Jimple.newReturnStmt(
              (Immediate) val.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());

      frame.setIn(val);
      setStmt(insn, ret);
      val.addUsageInStmt(ret);
    } else {
      final Operand operand = dword ? operandStack.popDual() : operandStack.pop();
      frame.mergeIn(operand);
    }
  }

  private void convertInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    if (op == NOP) {
      /*
       * We can ignore NOP instructions, but for completeness, we handle them
       */
      if (!InsnToStmt.containsKey(insn)) {
        InsnToStmt.put(insn, Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));
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
      if (operandStack.peek() == DWORD_DUMMY) {
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
      if (!InsnToStmt.containsKey(insn)) {
        setStmt(insn, Jimple.newReturnVoidStmt(StmtPositionInfo.createNoStmtPositionInfo()));
      }
    } else if (op == ATHROW) {
      StackFrame frame = operandStack.getOrCreateStackframe(insn);
      Operand opr;
      if (!InsnToStmt.containsKey(insn)) {
        opr = operandStack.popImmediate();
        JThrowStmt ts =
            Jimple.newThrowStmt(
                (Immediate) opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
        frame.setIn(opr);
        frame.setOut(opr);
        setStmt(insn, ts);
        opr.addUsageInStmt(ts);
      } else {
        opr = operandStack.pop();
        frame.mergeIn(opr);
      }
      operandStack.push(opr);
    } else if (op == MONITORENTER || op == MONITOREXIT) {
      StackFrame frame = operandStack.getOrCreateStackframe(insn);
      if (!InsnToStmt.containsKey(insn)) {
        Operand opr = operandStack.popStackConst();
        AbstractOpStmt ts =
            op == MONITORENTER
                ? Jimple.newEnterMonitorStmt(
                    (Immediate) opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo())
                : Jimple.newExitMonitorStmt(
                    (Immediate) opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());

        frame.setIn(opr);
        setStmt(insn, ts);
        opr.addUsageInStmt(ts);
      } else {
        frame.mergeIn(operandStack.pop());
      }
    } else {
      throw new AssertionError("Unknown insn op: " + op);
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
            throw new AssertionError("Unknown NEWARRAY type!");
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
        frame.mergeIn(operandStack.pop());
      }
    }
    operandStack.push(opr);
  }

  private void convertJumpInsn(@Nonnull JumpInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GOTO) {
      if (!InsnToStmt.containsKey(insn)) {
        Stmt gotoStmt = Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
        stmtsThatBranchToLabel.put(gotoStmt, insn.label);
        setStmt(insn, gotoStmt);
      }
      return;
    }
    /* must be ifX insn */
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (!InsnToStmt.containsKey(insn)) {
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
            throw new AssertionError("Unknown if op: " + op);
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
            throw new AssertionError("Unknown if op: " + op);
        }
        val.addUsageInExpr(cond);
        frame.setIn(val);
      }
      Stmt ifStmt = Jimple.newIfStmt(cond, StmtPositionInfo.createNoStmtPositionInfo());

      stmtsThatBranchToLabel.put(ifStmt, insn.label);
      setStmt(insn, ifStmt);
      if (isCmp) {
        val1.addUsageInStmt(ifStmt);
      }

      val.addUsageInStmt(ifStmt);
    } else {
      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        frame.mergeIn(operandStack.pop(), operandStack.pop());
      } else {
        frame.mergeIn(operandStack.pop());
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

  private Immediate toSootValue(@Nonnull Object val) throws AssertionError {
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
      throw new AssertionError("Unknown constant type: " + val.getClass());
    }
    return v;
  }

  private JFieldRef toSootFieldRef(Handle methodHandle) {
    String bsmClsName = AsmUtil.toQualifiedName(methodHandle.getOwner());
    JavaClassType bsmCls = JavaIdentifierFactory.getInstance().getClassType(bsmClsName);
    Type t = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc()).get(0);
    int kind = methodHandle.getTag();
    FieldSignature fieldSignature =
        JavaIdentifierFactory.getInstance().getFieldSignature(methodHandle.getName(), bsmCls, t);
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
    JavaClassType bsmCls = JavaIdentifierFactory.getInstance().getClassType(bsmClsName);
    List<Type> bsmSigTypes = AsmUtil.toJimpleSignatureDesc(methodHandle.getDesc());
    Type returnType = bsmSigTypes.remove(bsmSigTypes.size() - 1);
    return JavaIdentifierFactory.getInstance()
        .getMethodSignature(methodHandle.getName(), bsmCls, returnType, bsmSigTypes);
  }

  private void convertLookupSwitchInsn(@Nonnull LookupSwitchInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (InsnToStmt.containsKey(insn)) {
      frame.mergeIn(operandStack.pop());
      return;
    }
    Operand key = operandStack.popImmediate();

    List<IntConstant> keys = new ArrayList<>(insn.keys.size());
    for (Integer i : insn.keys) {
      keys.add(IntConstant.getInstance(i));
    }
    JSwitchStmt lookupSwitchStmt =
        Jimple.newLookupSwitchStmt(
            (Immediate) key.stackOrValue(), keys, StmtPositionInfo.createNoStmtPositionInfo());

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
      JavaClassType cls =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(clsName));
      List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(insn.desc);
      returnType = sigTypes.remove((sigTypes.size() - 1));
      MethodSignature methodSignature =
          JavaIdentifierFactory.getInstance()
              .getMethodSignature(insn.name, cls, returnType, sigTypes);
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
            throw new AssertionError("Unknown invoke op:" + op);
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

        frame.mergeIn(oprs);
      }
      returnType = expr.getMethodSignature().getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (!(returnType == VoidType.getInstance())) {
      operandStack.push(opr);
    } else if (!InsnToStmt.containsKey(insn)) {
      JInvokeStmt stmt =
          Jimple.newInvokeStmt(
              (AbstractInvokeExpr) opr.value, StmtPositionInfo.createNoStmtPositionInfo());
      setStmt(insn, stmt);
      opr.addUsageInStmt(stmt);
    }
    /*
     * assign all read ops in case the methodRef modifies any of the fields
     */
    assignReadOps(null);
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
          JavaIdentifierFactory.getInstance()
              .getClassType(JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME);

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
      MethodSignature methodRef =
          JavaIdentifierFactory.getInstance()
              .getMethodSignature(insn.name, bclass, returnType, parameterTypes);

      JDynamicInvokeExpr indy =
          Jimple.newDynamicInvokeExpr(
              bsmMethodRef, bsmMethodArgs, methodRef, insn.bsm.getTag(), methodArgs);
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
        frame.mergeIn(oprs);
      }
      returnType = expr.getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      operandStack.pushDual(opr);
    } else if (!(returnType instanceof VoidType)) {
      operandStack.push(opr);
    } else if (!InsnToStmt.containsKey(insn)) {
      JInvokeStmt stmt =
          Jimple.newInvokeStmt(
              (AbstractInvokeExpr) opr.value, StmtPositionInfo.createNoStmtPositionInfo());
      setStmt(insn, stmt);
      opr.addUsageInStmt(stmt);
    }
    /*
     * assign all read ops in case the method modifies any of the fields
     */
    assignReadOps(null);
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
      frame.mergeIn(sizes);
    }
    operandStack.push(opr);
  }

  private void convertTableSwitchInsn(@Nonnull TableSwitchInsnNode insn) {
    StackFrame frame = operandStack.getOrCreateStackframe(insn);
    if (InsnToStmt.containsKey(insn)) {
      frame.mergeIn(operandStack.pop());
      return;
    }
    Operand key = operandStack.popImmediate();
    JSwitchStmt tableSwitchStmt =
        Jimple.newTableSwitchStmt(
            (Immediate) key.stackOrValue(),
            insn.min,
            insn.max,
            StmtPositionInfo.createNoStmtPositionInfo());

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
      ClassType t = AsmUtil.toJimpleClassType(insn.desc);
      Expr val;
      if (op == NEW) {
        val = Jimple.newNewExpr(t);
      } else {
        Operand op1 = operandStack.popImmediate();
        Value v1 = op1.stackOrValue();
        switch (op) {
          case ANEWARRAY:
            {
              JNewArrayExpr expr = JavaJimple.getInstance().newNewArrayExpr(t, (Immediate) v1);
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          case CHECKCAST:
            {
              JCastExpr expr = Jimple.newCastExpr((Immediate) v1, t);
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          case INSTANCEOF:
            {
              JInstanceOfExpr expr = Jimple.newInstanceOfExpr((Immediate) v1, t);
              val = expr;
              op1.addUsageInExpr(expr);
              break;
            }
          default:
            throw new AssertionError("Unknown type op: " + op);
        }
        op1.addUsageInExpr(val);
        frame.setIn(op1);
      }
      opr = new Operand(insn, val, this);
      frame.setOut(opr);
    } else {
      opr = out[0];
      if (op != NEW) {
        frame.mergeIn(operandStack.pop());
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
    if (!InsnToStmt.containsKey(insn)) {
      AbstractDefinitionStmt<Local, ?> as =
          Jimple.newAssignStmt(
              local, opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());

      frame.setIn(opr);
      setStmt(insn, as);
      opr.addUsageInStmt(as);
    } else {

      frame.mergeIn(opr);
    }
    assignReadOps(local);
  }

  private void convertVarInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    if (op >= ILOAD && op <= ALOAD) {
      convertVarLoadInsn(insn);
    } else if (op >= ISTORE && op <= ASTORE) {
      convertVarStoreInsn(insn);
    } else if (op == RET) {
      /* we handle it, even though it should be removed */
      if (!InsnToStmt.containsKey(insn)) {
        setStmt(
            insn,
            Jimple.newRetStmt(
                getOrCreateLocal(insn.var), StmtPositionInfo.createNoStmtPositionInfo()));
      }
    } else {
      throw new AssertionError("Unknown var op: " + op);
    }
  }

  private void convertLabel(@Nonnull LabelNode ln) {
    if (!trapHandler.containsKey(ln)) {
      return;
    }

    // We create a nop statement as a placeholder so that we can jump
    // somewhere from the real exception handler in case this is inline
    // code
    if (inlineExceptionLabels.contains(ln)) {
      if (!InsnToStmt.containsKey(ln)) {
        JNopStmt nop = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(ln, nop);
        emitStmt(nop);
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
          Jimple.newIdentityStmt(stack, ref, StmtPositionInfo.createNoStmtPositionInfo());
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
    lastLineNumber = ln.line;
  }

  /* Conversion */

  private void addEdges(
      @Nonnull Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges,
      @Nonnull ArrayDeque<BranchedInsnInfo> conversionWorklist,
      @Nonnull AbstractInsnNode cur,
      @Nonnull AbstractInsnNode tgt,
      @Nullable List<LabelNode> tgts) {
    int lastIdx = tgts == null ? 0 : tgts.size();
    Operand[] stackss = operandStack.getStack().toArray(new Operand[0]);
    int i = 0;
    tgt_loop:
    do {
      BranchedInsnInfo edge = edges.get(cur, tgt);
      if (edge == null) {
        edge = new BranchedInsnInfo(tgt, operandStack.getStack());
        edge.addToPrevStack(stackss);
        edges.put(cur, tgt, edge);
        conversionWorklist.add(edge);
        continue;
      }
      if (edge.getOperandStack() != null) {
        List<Operand> stackTemp = edge.getOperandStack();
        if (stackTemp.size() != stackss.length) {
          throw new AssertionError("Multiple un-equal stacks!");
        }
        for (int j = 0; j != stackss.length; j++) {
          if (!stackTemp.get(j).equivTo(stackss[j])) {
            throw new AssertionError("Multiple un-equal stacks!");
          }
        }
        continue;
      }
      final LinkedList<Operand[]> prevStacks = edge.getPrevStacks();
      for (Operand[] ps : prevStacks) {
        if (Arrays.equals(ps, stackss)) {
          continue tgt_loop;
        }
      }
      edge.setOperandStack(operandStack.getStack());
      prevStacks.add(stackss);
      conversionWorklist.add(edge);
    } while (i < lastIdx && (tgt = tgts.get(i++)) != null);
  }

  private void convert() {
    ArrayDeque<BranchedInsnInfo> worklist = new ArrayDeque<>();
    for (LabelNode ln : trapHandler.keySet()) {
      if (checkInlineExceptionHandler(ln)) {
        // Catch the exception
        JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
        Local local = newStackLocal();
        AbstractDefinitionStmt<Local, JCaughtExceptionRef> as =
            Jimple.newIdentityStmt(local, ref, StmtPositionInfo.createNoStmtPositionInfo());

        Operand opr = new Operand(ln, ref, this);
        opr.stackLocal = local;

        worklist.add(new BranchedInsnInfo(ln, Collections.singletonList(opr)));

        // Save the statements
        inlineExceptionHandlers.put(ln, as);
      } else {
        worklist.add(new BranchedInsnInfo(ln, new ArrayList<>()));
      }
    }
    worklist.add(new BranchedInsnInfo(instructions.getFirst(), new ArrayList<>()));
    Table<AbstractInsnNode, AbstractInsnNode, BranchedInsnInfo> edges = HashBasedTable.create(1, 1);

    do {
      BranchedInsnInfo edge = worklist.pollLast();
      AbstractInsnNode insn = edge.getInsn();
      operandStack.setOperandStack(edge.getOperandStack());
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
            addEdges(edges, worklist, insn, jmp.label, null);
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

  private boolean checkInlineExceptionHandler(@Nonnull LabelNode ln) {
    // If this label is reachable through an exception and through normal
    // code, we have to split the exceptional case (with the exception on
    // the stack) from the normal fall-through case without anything on the
    // stack.
    for (AbstractInsnNode node : instructions) {
      if (node instanceof JumpInsnNode) {
        if (((JumpInsnNode) node).label == ln) {
          inlineExceptionLabels.add(ln);
          return true;
        }
      } else if (node instanceof LookupSwitchInsnNode) {
        if (((LookupSwitchInsnNode) node).labels.contains(ln)) {
          inlineExceptionLabels.add(ln);
          return true;
        }
      } else if (node instanceof TableSwitchInsnNode) {
        if (((TableSwitchInsnNode) node).labels.contains(ln)) {
          inlineExceptionLabels.add(ln);
          return true;
        }
      }
    }
    return false;
  }

  private void buildLocals() {

    MethodSignature methodSignature = lazyMethodSignature.get();

    int localIdx = 0;
    // create this Local if necessary ( i.e. not static )
    if (!bodyBuilder.getModifiers().contains(Modifier.STATIC)) {
      Local l = getOrCreateLocal(localIdx++);
      emitStmt(
          Jimple.newIdentityStmt(
              l, Jimple.newThisRef(declaringClass), StmtPositionInfo.createNoStmtPositionInfo()));
    }

    // add parameter Locals
    for (int i = 0; i < methodSignature.getParameterTypes().size(); i++) {
      Type parameterType = methodSignature.getParameterTypes().get(i);
      // [BH] assumption: parameterlocals do not exist yet -> create with annotation
      JavaLocal local =
          JavaJimple.newLocal(
              determineLocalName(localIdx),
              UnknownType.getInstance(),
              AsmUtil.createAnnotationUsage(
                  invisibleParameterAnnotations == null ? null : invisibleParameterAnnotations[i]));
      locals.set(localIdx, local);

      emitStmt(
          Jimple.newIdentityStmt(
              local,
              Jimple.newParameterRef(parameterType, i),
              StmtPositionInfo.createNoStmtPositionInfo()));

      // see https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-2.html#jvms-2.6.1
      if (AsmUtil.isDWord(parameterType)) {
        localIdx += 2;
      } else {
        localIdx++;
      }
    }

    Set<Local> bodyLocals = new LinkedHashSet<>(locals);
    bodyBuilder.setLocals(bodyLocals);
  }

  private void buildTraps() {
    List<Trap> traps = new ArrayList<>();

    for (TryCatchBlockNode trycatch : tryCatchBlocks) {
      Stmt handler = trapHandler.get(trycatch.handler);

      final String exceptionName =
          (trycatch.type != null) ? AsmUtil.toQualifiedName(trycatch.type) : "java.lang.Throwable";
      JavaClassType exceptionType = JavaIdentifierFactory.getInstance().getClassType(exceptionName);

      Trap trap =
          Jimple.newTrap(
              exceptionType,
              labelsToStmt.get(trycatch.start),
              labelsToStmt.get(trycatch.end),
              handler);
      traps.add(trap);
    }
    bodyBuilder.setTraps(traps);
  }

  private void emitStmt(@Nonnull Stmt stmt) {
    if (rememberedStmt != null) {
      if (rememberedStmt.fallsThrough()) {
        // determine whether successive emitted Stmts have a flow between them
        bodyBuilder.addFlow(rememberedStmt, stmt);
      }
    } else if (!isFirstStmtSet) {
      // determine first stmt to execute
      bodyBuilder.setStartingStmt(stmt);
      isFirstStmtSet = true;
    }
    rememberedStmt = stmt;
  }

  private void emitStmts(@Nonnull Stmt stmt) {
    if (stmt instanceof StmtContainer) {
      for (Stmt u : ((StmtContainer) stmt).getStmts()) {
        emitStmt(u);
      }
    } else {
      emitStmt(stmt);
    }
  }

  private void buildStmts() {
    AbstractInsnNode insn = instructions.getFirst();
    labelsToStmt = new HashMap<>();
    ArrayDeque<LabelNode> danglingLabel = new ArrayDeque<>();

    do {

      // assign Stmt associated with the current instruction. see
      // https://asm.ow2.io/javadoc/org/objectweb/asm/Label.html
      // there can be multiple labels assigned to the following stmt!
      final boolean isLabelNode = insn instanceof LabelNode;
      if (isLabelNode) {
        // Save the label to assign it to the next real Stmt
        danglingLabel.add((LabelNode) insn);
      }

      Stmt stmt = InsnToStmt.get(insn);
      if (stmt == null) {
        continue;
      }

      // associate label with following stmt
      if (!danglingLabel.isEmpty()) {
        Stmt targetStmt =
            stmt instanceof StmtContainer ? ((StmtContainer) stmt).getFirstStmt() : stmt;
        danglingLabel.forEach(l -> labelsToStmt.put(l, targetStmt));
        // If this is an exception handler, register the starting Stmt for it
        if (isLabelNode) {
          JIdentityStmt<?> caughtEx = findIdentityRefInContainer(stmt);
          if (caughtEx != null && caughtEx.getRightOp() instanceof JCaughtExceptionRef) {
            danglingLabel.forEach(l -> trapHandler.put(l, caughtEx));
          }
        }
        danglingLabel.clear();
      }

      emitStmts(stmt);

    } while ((insn = insn.getNext()) != null);

    // Emit the inline exception handlers
    for (LabelNode ln : inlineExceptionHandlers.keySet()) {
      Stmt handler = inlineExceptionHandlers.get(ln);
      emitStmts(handler);

      trapHandler.put(ln, handler);

      // jump to the original implementation
      Stmt targetStmt = InsnToStmt.get(ln);
      JGotoStmt gotoImpl = Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
      emitStmt(gotoImpl);
      bodyBuilder.addFlow(gotoImpl, targetStmt);
    }

    // link branching stmts with its targets
    for (Map.Entry<Stmt, LabelNode> entry : stmtsThatBranchToLabel.entries()) {
      final Stmt fromStmt = entry.getKey();
      final Stmt targetStmt = labelsToStmt.get(entry.getValue());
      if (targetStmt == null) {
        throw new ResolveException(
            "targetStmt not found for fromStmt"
                + fromStmt
                + " "
                + entry.getValue()
                + " in method "
                + lazyMethodSignature.get());
      }
      bodyBuilder.addFlow(fromStmt, targetStmt);
    }
  }

  @Nullable
  private JIdentityStmt<?> findIdentityRefInContainer(@Nonnull Stmt stmt) {
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

  public Stmt getLatestVersionOfStmt(Stmt oldStmt) {
    if (replacedStmt.containsKey(oldStmt)) {
      return getLatestVersionOfStmt(replacedStmt.get(oldStmt));
    } else {
      return oldStmt;
    }
  }

  public void replaceStmt(Stmt oldStmt, Stmt newStmt) {
    AbstractInsnNode key = null;

    if (rememberedStmt == oldStmt) {
      rememberedStmt = newStmt;
    }

    for (Entry<AbstractInsnNode, Stmt> entry : InsnToStmt.entrySet()) {
      if (Objects.equals(oldStmt, entry.getValue())) {
        key = entry.getKey();
      }
    }

    if (key == null) {
      throw new AssertionError("Could not replace value in insn map because it is absent");
    }

    InsnToStmt.put(key, newStmt);
    replacedStmt.put(oldStmt, newStmt);

    List<LabelNode> branchLabels = stmtsThatBranchToLabel.get(oldStmt);

    if (branchLabels != null) {
      branchLabels.forEach(bl -> stmtsThatBranchToLabel.put(newStmt, bl));
      stmtsThatBranchToLabel.removeAll(oldStmt);
    }
  }

  /**
   * * returns all stmts that use this expr
   *
   * @param expr which is used to filter associated Stmts
   */
  public Stream<Stmt> getStmtsThatUse(@Nonnull Expr expr) {
    Stream<Stmt> currentUses =
        InsnToStmt.values().stream().filter(stmt -> stmt.getUses().contains(expr));

    Stream<Stmt> oldMappedUses =
        replacedStmt.entrySet().stream()
            .filter(stmt -> stmt.getKey().getUses().contains(expr))
            .map(stmt -> getLatestVersionOfStmt(stmt.getValue()));

    return Stream.concat(currentUses, oldMappedUses);
  }
}
