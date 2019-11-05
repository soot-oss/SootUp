package de.upb.swt.soot.java.bytecode.frontend;

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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import de.upb.swt.soot.core.frontend.MethodSource;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
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
import de.upb.swt.soot.core.jimple.common.expr.JAddExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JInstanceOfExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewArrayExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewMultiArrayExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.FieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractOpStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JTableSwitchStmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** @author Andreas Dann */
public class AsmMethodSource extends JSRInlinerAdapter implements MethodSource {

  private static final Operand DWORD_DUMMY = new Operand(null, null);

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
  private Map<Integer, Local> locals;
  private Multimap<LabelNode, StmtBox> labels;
  private Map<AbstractInsnNode, Stmt> units;
  private ArrayList<Operand> stack;
  private Map<AbstractInsnNode, StackFrame> frames;
  private Multimap<LabelNode, StmtBox> trapHandlers;
  private int lastLineNumber = -1;
  @Nullable private JavaClassType declaringClass;
  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  /*
   * Hint: in InstructionConverter convertInvokeInstruction() ling creates string for methodRef and types and stores/replaces
   * the methodref with a MethodSignature (for the methodRef to call) and then creates the invoke Instruction
   * JavaJimple.getInstance().newStaticInvokeExpr
   */

  /* -const fields- */

  @Nonnull private final Set<LabelNode> inlineExceptionLabels = new HashSet<>();

  @Nonnull private final Map<LabelNode, Stmt> inlineExceptionHandlers = new HashMap<>();

  private final Supplier<MethodSignature> lazyMethodSignature =
      Suppliers.memoize(
          () -> {
            List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(desc);
            Type retType = sigTypes.remove(sigTypes.size() - 1);

            return JavaIdentifierFactory.getInstance()
                .getMethodSignature(name, declaringClass, retType, sigTypes);
          });

  private final Supplier<Set<Modifier>> lazyModifiers =
      Suppliers.memoize(() -> AsmUtil.getModifiers(access));

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
  @Nullable
  public Body resolveBody() throws AsmFrontendException {
    Set<Local> bodyLocals = new HashSet<>();
    List<Trap> bodyTraps = new ArrayList<>();
    List<Stmt> bodyStmts = new ArrayList<>();
    // FIXME: [AD] add real line number
    Position bodyPos = NoPositionInformation.getInstance();

    /* initialize */
    int nrInsn = instructions.size();
    nextLocal = maxLocals;
    locals = new HashMap<>(maxLocals + (maxLocals / 2));
    labels = ArrayListMultimap.create(4, 1);
    units = new HashMap<>(nrInsn);
    frames = new HashMap<>(nrInsn);
    trapHandlers = ArrayListMultimap.create(tryCatchBlocks.size(), 1);

    /* retrieve all trap handlers */
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      trapHandlers.put(tc.handler, Jimple.newStmtBox(null));
    }
    /* convert instructions */
    try {
      convert();
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert " + lazyMethodSignature.get(), e);
    }

    /* build body (add units, locals, traps, etc.) */
    emitLocals(bodyLocals, bodyStmts);
    emitTraps(bodyTraps);
    emitUnits(bodyStmts);

    /* clean up */
    locals = null;
    labels = null;
    units = null;
    stack = null;
    frames = null;

    // TODO CastAndReturnInliner:
    // Make sure to inline patterns of the form to enable proper variable
    // splitting and type assignment:
    // a = new A();
    // goto l0;
    // l0:
    // b = (B) a;
    // return b;

    Body body = new Body(bodyLocals, bodyTraps, bodyStmts, bodyPos);
    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      try {
        body = bodyInterceptor.interceptBody(body);
      } catch (Exception e) {
        throw new RuntimeException(
            "Failed to apply " + bodyInterceptor + " to " + lazyMethodSignature.get(), e);
      }
    }

    return body;
  }

  private StackFrame getFrame(AbstractInsnNode insn) {
    StackFrame frame = frames.get(insn);
    if (frame == null) {
      frame = new StackFrame(this);
      frames.put(insn, frame);
    }
    return frame;
  }

  private Local getLocal(int idx) {
    if (idx >= maxLocals) {
      throw new IllegalArgumentException("Invalid local index: " + idx);
    }
    Integer i = idx;
    Local l = locals.get(i);
    if (l == null) {
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
      l = Jimple.newLocal(name, UnknownType.getInstance());
      locals.put(i, l);
    }
    return l;
  }

  private void push(Operand opr) {
    stack.add(opr);
  }

  private void pushDual(Operand opr) {
    stack.add(DWORD_DUMMY);
    stack.add(opr);
  }

  private Operand peek() {
    return stack.get(stack.size() - 1);
  }

  private void push(Type t, Operand opr) {
    if (AsmUtil.isDWord(t)) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private Operand pop() {
    if (stack.isEmpty()) {
      throw new RuntimeException("Stack underrun");
    }
    return stack.remove(stack.size() - 1);
  }

  private Operand popDual() {
    Operand o = pop();
    Operand o2 = pop();
    if (o2 != DWORD_DUMMY && o2 != o) {
      throw new AssertionError("Not dummy operand, " + o2.value + " -- " + o.value);
    }
    return o;
  }

  @Nonnull
  private Operand pop(@Nonnull Type t) {
    return AsmUtil.isDWord(t) ? popDual() : pop();
  }

  @Nonnull
  private Operand popLocal(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Local)) {
      l = o.stack = newStackLocal();
      setUnit(o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  private Operand popImmediate(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Local) && !(v instanceof Constant)) {
      l = o.stack = newStackLocal();
      setUnit(o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  private Operand popStackConst(@Nonnull Operand o) {
    Value v = o.value;
    Local l = o.stack;
    if (l == null && !(v instanceof Constant)) {
      l = o.stack = newStackLocal();
      setUnit(o.insn, Jimple.newAssignStmt(l, v, StmtPositionInfo.createNoStmtPositionInfo()));
      o.updateBoxes();
    }
    return o;
  }

  @Nonnull
  private Operand popLocal() {
    return popLocal(pop());
  }

  @SuppressWarnings("unused")
  @Nonnull
  private Operand popLocalDual() {
    return popLocal(popDual());
  }

  @Nonnull
  private Operand popImmediate() {
    return popImmediate(pop());
  }

  @Nonnull
  private Operand popImmediateDual() {
    return popImmediate(popDual());
  }

  @Nonnull
  private Operand popImmediate(@Nonnull Type t) {
    return AsmUtil.isDWord(t) ? popImmediateDual() : popImmediate();
  }

  @Nonnull
  private Operand popStackConst() {
    return popStackConst(pop());
  }

  @SuppressWarnings("unused")
  @Nonnull
  private Operand popStackConstDual() {
    return popStackConst(popDual());
  }

  void setUnit(@Nonnull AbstractInsnNode insn, @Nonnull Stmt u) {
    // FIXME: re-add linenumber keep
    // ASM LineNumberNode
    // if (Options.keep_line_number() && lastLineNumber >= 0) {
    // Tag lineTag = u.getTag("LineNumberTag");
    // if (lineTag == null) {
    // lineTag = new LineNumberTag(lastLineNumber);
    // u.addTag(lineTag);
    // } else if (((LineNumberTag) lineTag).getLineNumber() != lastLineNumber) {
    // throw new RuntimeException("Line tag mismatch");
    // }
    // }

    Stmt o = units.put(insn, u);
    if (o != null) {
      throw new AssertionError(insn.getOpcode() + " already has a unit, " + o);
    }
  }

  void mergeUnits(@Nonnull AbstractInsnNode insn, @Nonnull Stmt u) {
    Stmt prev = units.put(insn, u);
    if (prev != null) {
      Stmt merged = new StmtContainer(prev, u);
      units.put(insn, merged);
    }
  }

  @Nonnull
  Local newStackLocal() {
    int idx = nextLocal++;
    Local l = Jimple.newLocal("$stack" + idx, UnknownType.getInstance());
    locals.put(idx, l);
    return l;
  }

  @SuppressWarnings("unchecked")
  <A extends Stmt> A getUnit(@Nonnull AbstractInsnNode insn) {
    return (A) units.get(insn);
  }

  private void assignReadOps(@Nullable Local l) {
    if (stack.isEmpty()) {
      return;
    }
    for (Operand opr : stack) {
      if (opr == DWORD_DUMMY || opr.stack != null || (l == null && opr.value instanceof Local)) {
        continue;
      }
      if (l != null && !opr.value.equivTo(l)) {
        List<ValueBox> uses = opr.value.getUseBoxes();
        boolean noref = true;
        for (ValueBox use : uses) {
          Value val = use.getValue();
          if (val.equivTo(l)) {
            noref = false;
            break;
          }
        }
        if (noref) {
          continue;
        }
      }
      int op = opr.insn.getOpcode();

      // FIXME: [JMP] The IF condition is always false.
      //        Shouldn't it be `l == null && op != GETFIELD && op != GETSTATIC && (op < IALOAD ||
      // op > SALOAD)`?
      //                                                                                        ^^
      // logical OR here
      if (l == null && op != GETFIELD && op != GETSTATIC && (op < IALOAD && op > SALOAD)) {
        continue;
      }
      Local stack = newStackLocal();
      opr.stack = stack;
      JAssignStmt as =
          Jimple.newAssignStmt(stack, opr.value, StmtPositionInfo.createNoStmtPositionInfo());
      opr.updateBoxes();
      setUnit(opr.insn, as);
    }
  }

  private void convertGetFieldInsn(@Nonnull FieldInsnNode insn) {
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    Type type;
    if (out == null) {
      // SootClass declClass =
      // Scene.v().getSootClass(AsmUtil.toQualifiedName(insn.owner));
      JavaClassType declClass =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(insn.owner));
      // type = AsmUtil.toJimpleType(insn.desc);
      type = JavaIdentifierFactory.getInstance().getType((AsmUtil.toQualifiedName(insn.desc)));
      Value val;
      // JFieldRef ref;
      FieldSignature ref;
      if (insn.getOpcode() == GETSTATIC) {
        // ref = Scene.v().makeFieldRef(declClass, insn.name, type, true);
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
      } else {
        Operand base = popLocal();
        // ref = Scene.v().makeFieldRef(declClass, insn.name, type, false);
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        JInstanceFieldRef ifr = Jimple.newInstanceFieldRef(base.stackOrValue(), ref);
        val = ifr;
        base.addBox(ifr.getBaseBox());
        frame.in(base);
        frame.boxes(ifr.getBaseBox());
      }
      opr = new Operand(insn, val);
      frame.out(opr);
    } else {
      opr = out[0];
      type = opr.<JFieldRef>value().getFieldSignature().getType();
      if (insn.getOpcode() == GETFIELD) {
        frame.mergeIn(pop());
      }
    }
    push(type, opr);
  }

  private void convertPutFieldInsn(@Nonnull FieldInsnNode insn) {
    boolean instance = insn.getOpcode() == PUTFIELD;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr, rvalue;
    Type type;
    if (out == null) {
      // SootClass declClass =
      // Scene.v().getSootClass(AsmUtil.toQualifiedName(insn.owner));
      JavaClassType declClass =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(insn.owner));
      // type = AsmUtil.toJimpleType(insn.desc);
      type = JavaIdentifierFactory.getInstance().getType((AsmUtil.toQualifiedName(insn.desc)));

      Value val;
      FieldSignature ref;
      rvalue = popImmediate(type);
      if (!instance) {
        // ref = Scene.v().makeFieldRef(declClass, insn.name, type, true);
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        val = Jimple.newStaticFieldRef(ref);
        frame.in(rvalue);
      } else {
        Operand base = popLocal();
        // ref = Scene.v().makeFieldRef(declClass, insn.name, type, false);
        ref = JavaIdentifierFactory.getInstance().getFieldSignature(insn.name, declClass, type);
        JInstanceFieldRef ifr = Jimple.newInstanceFieldRef(base.stackOrValue(), ref);
        val = ifr;
        base.addBox(ifr.getBaseBox());
        frame.in(rvalue, base);
      }
      opr = new Operand(insn, val);
      frame.out(opr);
      JAssignStmt as =
          Jimple.newAssignStmt(
              val, rvalue.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
      rvalue.addBox(as.getRightOpBox());
      if (!instance) {
        frame.boxes(as.getRightOpBox());
      } else {
        frame.boxes(as.getRightOpBox(), ((JInstanceFieldRef) val).getBaseBox());
      }
      setUnit(insn, as);
    } else {
      opr = out[0];
      type = opr.<JFieldRef>value().getFieldSignature().getType();
      rvalue = pop(type);
      if (!instance) {
        /* PUTSTATIC only needs one operand on the stack, the rvalue */
        frame.mergeIn(rvalue);
      } else {
        /* PUTFIELD has a rvalue and a base */
        frame.mergeIn(rvalue, pop());
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
    Local local = getLocal(insn.var);
    assignReadOps(local);
    if (!units.containsKey(insn)) {
      JAddExpr add = Jimple.newAddExpr(local, IntConstant.getInstance(insn.incr));
      setUnit(insn, Jimple.newAssignStmt(local, add, StmtPositionInfo.createNoStmtPositionInfo()));
    }
  }

  private void convertConstInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
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
      opr = new Operand(insn, v);
      frame.out(opr);
    } else {
      opr = out[0];
    }
    if (op == LCONST_0 || op == LCONST_1 || op == DCONST_0 || op == DCONST_1) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertArrayLoadInsn(@Nonnull InsnNode insn) {
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Operand indx = popImmediate();
      Operand base = popImmediate();
      JArrayRef ar = JavaJimple.getInstance().newArrayRef(base.stackOrValue(), indx.stackOrValue());
      indx.addBox(ar.getIndexBox());
      base.addBox(ar.getBaseBox());
      opr = new Operand(insn, ar);
      frame.in(indx, base);
      frame.boxes(ar.getIndexBox(), ar.getBaseBox());
      frame.out(opr);
    } else {
      opr = out[0];
      frame.mergeIn(pop(), pop());
    }
    int op = insn.getOpcode();
    if (op == DALOAD || op == LALOAD) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertArrayStoreInsn(@Nonnull InsnNode insn) {
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Operand indx = popImmediate();
      Operand base = popImmediate();
      JArrayRef ar = JavaJimple.getInstance().newArrayRef(base.stackOrValue(), indx.stackOrValue());
      indx.addBox(ar.getIndexBox());
      base.addBox(ar.getBaseBox());
      opr = new Operand(insn, ar);
      frame.in(indx, base);
      frame.boxes(ar.getIndexBox(), ar.getBaseBox());
      frame.out(opr);
    } else {
      opr = out[0];
      frame.mergeIn(pop(), pop());
    }
    int op = insn.getOpcode();
    if (op == DALOAD || op == LALOAD) {
      pushDual(opr);
    } else {
      push(opr);
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
    Operand dupd = popImmediate();
    Operand dupd2 = null;

    // Some instructions allow operands that take two registers
    boolean dword = op == DUP2 || op == DUP2_X1 || op == DUP2_X2;
    if (dword) {
      if (peek() == DWORD_DUMMY) {
        pop();
        dupd2 = dupd;
      } else {
        dupd2 = popImmediate();
      }
    }

    if (op == DUP) {
      // val -> val, val
      push(dupd);
      push(dupd);
    } else if (op == DUP_X1) {
      // val2, val1 -> val1, val2, val1
      // value1, value2 must not be of type double or long
      Operand o2 = popImmediate();
      push(dupd);
      push(o2);
      push(dupd);
    } else if (op == DUP_X2) {
      // value3, value2, value1 -> value1, value3, value2, value1
      Operand o2 = popImmediate();
      Operand o3 = peek() == DWORD_DUMMY ? pop() : popImmediate();
      push(dupd);
      push(o3);
      push(o2);
      push(dupd);
    } else if (op == DUP2) {
      // value2, value1 -> value2, value1, value2, value1
      push(dupd2);
      push(dupd);
      push(dupd2);
      push(dupd);
    } else if (op == DUP2_X1) {
      // value3, value2, value1 -> value2, value1, value3, value2, value1
      // Attention: value2 may be
      Operand o2 = popImmediate();
      push(dupd2);
      push(dupd);
      push(o2);
      push(dupd2);
      push(dupd);
    } else if (op == DUP2_X2) {
      // (value4, value3), (value2, value1) -> (value2, value1), (value4, value3),
      // (value2, value1)
      Operand o2 = popImmediate();
      Operand o2h = peek() == DWORD_DUMMY ? pop() : popImmediate();
      push(dupd2);
      push(dupd);
      push(o2h);
      push(o2);
      push(dupd2);
      push(dupd);
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
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Operand op2 =
          (dword && op != LSHL && op != LSHR && op != LUSHR) ? popImmediateDual() : popImmediate();
      Operand op1 = dword ? popImmediateDual() : popImmediate();
      Value v1 = op1.stackOrValue();
      Value v2 = op2.stackOrValue();
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
      op1.addBox(binop.getOp1Box());
      op2.addBox(binop.getOp2Box());
      opr = new Operand(insn, binop);
      frame.in(op2, op1);
      frame.boxes(binop.getOp2Box(), binop.getOp1Box());
      frame.out(opr);
    } else {
      opr = out[0];
      if (dword) {
        if (op != LSHL && op != LSHR && op != LUSHR) {
          frame.mergeIn(popDual(), popDual());
        } else {
          frame.mergeIn(pop(), popDual());
        }
      } else {
        frame.mergeIn(pop(), pop());
      }
    }
    if (dword && (op < LCMP || op > DCMPG)) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertUnopInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LNEG || op == DNEG;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Operand op1 = dword ? popImmediateDual() : popImmediate();
      Value v1 = op1.stackOrValue();
      AbstractUnopExpr unop;
      if (op >= INEG && op <= DNEG) {
        unop = Jimple.newNegExpr(v1);
      } else if (op == ARRAYLENGTH) {
        unop = Jimple.newLengthExpr(v1);
      } else {
        throw new AssertionError("Unknown unop: " + op);
      }
      op1.addBox(unop.getOpBox());
      opr = new Operand(insn, unop);
      frame.in(op1);
      frame.boxes(unop.getOpBox());
      frame.out(opr);
    } else {
      opr = out[0];
      frame.mergeIn(dword ? popDual() : pop());
    }
    if (dword) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertPrimCastInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean tod = op == I2L || op == I2D || op == F2L || op == F2D || op == D2L || op == L2D;
    boolean fromd = op == D2L || op == L2D || op == D2I || op == L2I || op == D2F || op == L2F;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Type totype;
      if (op == I2L || op == F2L || op == D2L) {
        totype = PrimitiveType.getLong();
      } else if (op == L2I || op == F2I || op == D2I) {
        totype = PrimitiveType.getInt();
      } else if (op == I2F || op == L2F || op == D2F) {
        totype = PrimitiveType.getFloat();
      } else if (op == I2D || op == L2D || op == F2D) {
        totype = PrimitiveType.getDouble();
      } else if (op == I2B) {
        totype = PrimitiveType.getByte();
      } else if (op == I2S) {
        totype = PrimitiveType.getShort();
      } else if (op == I2C) {
        totype = PrimitiveType.getChar();
      } else {
        throw new AssertionError("Unknonw prim cast op: " + op);
      }
      Operand val = fromd ? popImmediateDual() : popImmediate();
      JCastExpr cast = Jimple.newCastExpr(val.stackOrValue(), totype);
      opr = new Operand(insn, cast);
      val.addBox(cast.getOpBox());
      frame.in(val);
      frame.boxes(cast.getOpBox());
      frame.out(opr);
    } else {
      opr = out[0];
      frame.mergeIn(fromd ? popDual() : pop());
    }
    if (tod) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertReturnInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LRETURN || op == DRETURN;
    StackFrame frame = getFrame(insn);
    if (!units.containsKey(insn)) {
      Operand val = dword ? popImmediateDual() : popImmediate();
      JReturnStmt ret =
          Jimple.newReturnStmt(val.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
      val.addBox(ret.getOpBox());
      frame.in(val);
      frame.boxes(ret.getOpBox());
      setUnit(insn, ret);
    } else {
      frame.mergeIn(dword ? popDual() : pop());
    }
  }

  private void convertInsn(@Nonnull InsnNode insn) {
    int op = insn.getOpcode();
    if (op == NOP) {
      /*
       * We can ignore NOP instructions, but for completeness, we handle them
       */
      if (!units.containsKey(insn)) {
        units.put(insn, Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));
      }
    } else if (op >= ACONST_NULL && op <= DCONST_1) {
      convertConstInsn(insn);
    } else if (op >= IALOAD && op <= SALOAD) {
      convertArrayLoadInsn(insn);
    } else if (op >= IASTORE && op <= SASTORE) {
      convertArrayStoreInsn(insn);
    } else if (op == POP) {
      popImmediate();
    } else if (op == POP2) {
      popImmediate();
      if (peek() == DWORD_DUMMY) {
        pop();
      } else {
        popImmediate();
      }
    } else if (op >= DUP && op <= DUP2_X2) {
      convertDupInsn(insn);
    } else if (op == SWAP) {
      Operand o1 = popImmediate();
      Operand o2 = popImmediate();
      push(o1);
      push(o2);
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
      if (!units.containsKey(insn)) {
        setUnit(insn, Jimple.newReturnVoidStmt(StmtPositionInfo.createNoStmtPositionInfo()));
      }
    } else if (op == ATHROW) {
      StackFrame frame = getFrame(insn);
      Operand opr;
      if (!units.containsKey(insn)) {
        opr = popImmediate();
        JThrowStmt ts =
            Jimple.newThrowStmt(opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
        opr.addBox(ts.getOpBox());
        frame.in(opr);
        frame.out(opr);
        frame.boxes(ts.getOpBox());
        setUnit(insn, ts);
      } else {
        opr = pop();
        frame.mergeIn(opr);
      }
      push(opr);
    } else if (op == MONITORENTER || op == MONITOREXIT) {
      StackFrame frame = getFrame(insn);
      if (!units.containsKey(insn)) {
        Operand opr = popStackConst();
        AbstractOpStmt ts =
            op == MONITORENTER
                ? Jimple.newEnterMonitorStmt(
                    opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo())
                : Jimple.newExitMonitorStmt(
                    opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
        opr.addBox(ts.getOpBox());
        frame.in(opr);
        frame.boxes(ts.getOpBox());
        setUnit(insn, ts);
      } else {
        frame.mergeIn(pop());
      }
    } else {
      throw new AssertionError("Unknown insn op: " + op);
    }
  }

  private void convertIntInsn(@Nonnull IntInsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Value v;
      if (op == BIPUSH || op == SIPUSH) {
        v = IntConstant.getInstance(insn.operand);
      } else {
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
        Operand size = popImmediate();
        JNewArrayExpr anew = JavaJimple.getInstance().newNewArrayExpr(type, size.stackOrValue());
        size.addBox(anew.getSizeBox());
        frame.in(size);
        frame.boxes(anew.getSizeBox());
        v = anew;
      }
      opr = new Operand(insn, v);
      frame.out(opr);
    } else {
      opr = out[0];
      if (op == NEWARRAY) {
        frame.mergeIn(pop());
      }
    }
    push(opr);
  }

  @SuppressWarnings("ConstantConditions")
  private void convertJumpInsn(@Nonnull JumpInsnNode insn) {
    int op = insn.getOpcode();
    if (op == GOTO) {
      if (!units.containsKey(insn)) {
        StmtBox box = Jimple.newStmtBox(null);
        labels.put(insn.label, box);
        setUnit(insn, Jimple.newGotoStmt(box, StmtPositionInfo.createNoStmtPositionInfo()));
      }
      return;
    }
    /* must be ifX insn */
    StackFrame frame = getFrame(insn);
    if (!units.containsKey(insn)) {
      Operand val = popImmediate();
      Value v = val.stackOrValue();
      AbstractConditionExpr cond;
      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        Operand val1 = popImmediate();
        Value v1 = val1.stackOrValue();
        if (op == IF_ICMPEQ) {
          cond = Jimple.newEqExpr(v1, v);
        } else if (op == IF_ICMPNE) {
          cond = Jimple.newNeExpr(v1, v);
        } else if (op == IF_ICMPLT) {
          cond = Jimple.newLtExpr(v1, v);
        } else if (op == IF_ICMPGE) {
          cond = Jimple.newGeExpr(v1, v);
        } else if (op == IF_ICMPGT) {
          cond = Jimple.newGtExpr(v1, v);
        } else if (op == IF_ICMPLE) {
          cond = Jimple.newLeExpr(v1, v);
        } else if (op == IF_ACMPEQ) {
          cond = Jimple.newEqExpr(v1, v);
        } else if (op == IF_ACMPNE) {
          cond = Jimple.newNeExpr(v1, v);
        } else {
          throw new AssertionError("Unknown if op: " + op);
        }
        val1.addBox(cond.getOp1Box());
        val.addBox(cond.getOp2Box());
        frame.boxes(cond.getOp2Box(), cond.getOp1Box());
        frame.in(val, val1);
      } else {
        if (op == IFEQ) {
          cond = Jimple.newEqExpr(v, IntConstant.getInstance(0));
        } else if (op == IFNE) {
          cond = Jimple.newNeExpr(v, IntConstant.getInstance(0));
        } else if (op == IFLT) {
          cond = Jimple.newLtExpr(v, IntConstant.getInstance(0));
        } else if (op == IFGE) {
          cond = Jimple.newGeExpr(v, IntConstant.getInstance(0));
        } else if (op == IFGT) {
          cond = Jimple.newGtExpr(v, IntConstant.getInstance(0));
        } else if (op == IFLE) {
          cond = Jimple.newLeExpr(v, IntConstant.getInstance(0));
        } else if (op == IFNULL) {
          cond = Jimple.newEqExpr(v, NullConstant.getInstance());
        } else if (op == IFNONNULL) {
          cond = Jimple.newNeExpr(v, NullConstant.getInstance());
        } else {
          throw new AssertionError("Unknown if op: " + op);
        }
        val.addBox(cond.getOp1Box());
        frame.boxes(cond.getOp1Box());
        frame.in(val);
      }
      StmtBox box = Jimple.newStmtBox(null);
      labels.put(insn.label, box);
      setUnit(insn, Jimple.newIfStmt(cond, box, StmtPositionInfo.createNoStmtPositionInfo()));
    } else {
      if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        frame.mergeIn(pop(), pop());
      } else {
        frame.mergeIn(pop());
      }
    }
  }

  private void convertLdcInsn(@Nonnull LdcInsnNode insn) {
    Object val = insn.cst;
    boolean dword = val instanceof Long || val instanceof Double;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Value v = toSootValue(val);
      opr = new Operand(insn, v);
      frame.out(opr);
    } else {
      opr = out[0];
    }
    if (dword) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private Value toSootValue(@Nonnull Object val) throws AssertionError {
    Value v;
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

  private FieldRef toSootFieldRef(Handle methodHandle) {
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
      Operand base = popLocal();
      return Jimple.newInstanceFieldRef(base.stackOrValue(), fieldSignature);
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
    StackFrame frame = getFrame(insn);
    if (units.containsKey(insn)) {
      frame.mergeIn(pop());
      return;
    }
    Operand key = popImmediate();
    StmtBox dflt = Jimple.newStmtBox(null);

    List<StmtBox> targets = new ArrayList<>(insn.labels.size());
    labels.put(insn.dflt, dflt);
    for (LabelNode ln : insn.labels) {
      StmtBox box = Jimple.newStmtBox(null);
      targets.add(box);
      labels.put(ln, box);
    }

    List<IntConstant> keys = new ArrayList<>(insn.keys.size());
    for (Integer i : insn.keys) {
      keys.add(IntConstant.getInstance(i));
    }

    JLookupSwitchStmt lss =
        Jimple.newLookupSwitchStmt(
            key.stackOrValue(), keys, targets, dflt, StmtPositionInfo.createNoStmtPositionInfo());
    key.addBox(lss.getKeyBox());
    frame.in(key);
    frame.boxes(lss.getKeyBox());
    setUnit(insn, lss);
  }

  private void convertMethodInsn(@Nonnull MethodInsnNode insn) {
    int op = insn.getOpcode();
    boolean instance = op != INVOKESTATIC;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    // Type returnType;
    Type returnType;
    if (out == null) {
      String clsName = AsmUtil.toQualifiedName(insn.owner);
      if (clsName.charAt(0) == '[') {
        clsName = "java.lang.Object";
      }
      // SootClass cls = Scene.v().getSootClass(clsName);
      JavaClassType cls =
          JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(clsName));
      // List<Type> sigTypes = AsmUtil.toJimpleDesc(insn.desc);
      List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(insn.desc);
      // returnType = sigTypes.remove(sigTypes.size() - 1);
      returnType = sigTypes.remove((sigTypes.size() - 1));
      // SootMethodRef ref = Scene.v().makeMethodRef(cls, insn.name, sigTypes,
      // returnType,
      // !instance);
      MethodSignature methodSignature =
          JavaIdentifierFactory.getInstance()
              .getMethodSignature(insn.name, cls, returnType, sigTypes);
      // MethodRef ref = JavaJimple.getInstance().newMethodRef(view, methodSignature, !instance);
      int nrArgs = sigTypes.size();
      final Operand[] args;
      List<Value> argList = Collections.emptyList();
      if (!instance) {
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
        args[nrArgs] = popImmediate(sigTypes.get(nrArgs));
        argList.add(args[nrArgs].stackOrValue());
      }
      if (argList.size() > 1) {
        Collections.reverse(argList);
      }
      if (instance) {
        args[args.length - 1] = popLocal();
      }
      ValueBox[] boxes = args == null ? null : new ValueBox[args.length];
      AbstractInvokeExpr invoke;
      if (!instance) {
        invoke = Jimple.newStaticInvokeExpr(methodSignature, argList);
      } else {
        Local base = (Local) args[args.length - 1].stackOrValue();
        AbstractInstanceInvokeExpr iinvoke;
        if (op == INVOKESPECIAL) {
          iinvoke = Jimple.newSpecialInvokeExpr(base, methodSignature, argList);
        } else if (op == INVOKEVIRTUAL) {
          iinvoke = Jimple.newVirtualInvokeExpr(base, methodSignature, argList);
        } else if (op == INVOKEINTERFACE) {
          iinvoke = Jimple.newInterfaceInvokeExpr(base, methodSignature, argList);
        } else {
          throw new AssertionError("Unknown invoke op:" + op);
        }
        boxes[boxes.length - 1] = iinvoke.getBaseBox();
        args[args.length - 1].addBox(boxes[boxes.length - 1]);
        invoke = iinvoke;
      }
      if (boxes != null) {
        for (int i = 0; i != sigTypes.size(); i++) {
          boxes[i] = invoke.getArgBox(i);
          args[i].addBox(boxes[i]);
        }
        frame.boxes(boxes);
        frame.in(args);
      }
      opr = new Operand(insn, invoke);
      frame.out(opr);
    } else {
      opr = out[0];
      AbstractInvokeExpr expr = (AbstractInvokeExpr) opr.value;
      List<Type> types = expr.getMethodSignature().getParameterSignatures();
      Operand[] oprs;
      int nrArgs = types.size();
      if (lazyModifiers.get().contains(Modifier.STATIC)) {
        oprs = nrArgs == 0 ? null : new Operand[nrArgs];
      } else {
        oprs = new Operand[nrArgs + 1];
      }
      if (oprs != null) {
        while (nrArgs-- != 0) {
          oprs[nrArgs] = pop(types.get(nrArgs));
        }
        if (!lazyModifiers.get().contains(Modifier.STATIC)) {
          oprs[oprs.length - 1] = pop();
        }
        frame.mergeIn(oprs);
        nrArgs = types.size();
      }
      returnType = expr.getMethodSignature().getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      pushDual(opr);
    } else if (!(returnType == VoidType.getInstance())) {
      push(opr);
    } else if (!units.containsKey(insn)) {
      setUnit(insn, Jimple.newInvokeStmt(opr.value, StmtPositionInfo.createNoStmtPositionInfo()));
    }
    /*
     * assign all read ops in case the methodRef modifies any of the fields
     */
    assignReadOps(null);
  }

  private void convertInvokeDynamicInsn(@Nonnull InvokeDynamicInsnNode insn) {
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    Type returnType;
    if (out == null) {
      // convert info on bootstrap method
      MethodSignature bsmMethodRef = toMethodSignature(insn.bsm);
      List<Value> bsmMethodArgs = new ArrayList<>(insn.bsmArgs.length);
      for (Object bsmArg : insn.bsmArgs) {
        bsmMethodArgs.add(toSootValue(bsmArg));
      }

      // create ref to actual method
      JavaClassType bclass =
          JavaIdentifierFactory.getInstance()
              .getClassType(SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME);

      // Generate parameters & returnType & parameterTypes
      List<Type> types = AsmUtil.toJimpleSignatureDesc(insn.desc);
      int nrArgs = types.size() - 1;
      List<Type> parameterTypes = new ArrayList<>(nrArgs);
      List<Value> methodArgs = new ArrayList<>(nrArgs);

      Operand[] args = new Operand[nrArgs];
      ValueBox[] boxes = new ValueBox[nrArgs];

      // Beware: Call stack is FIFO, Jimple is linear

      for (int i = nrArgs - 1; i >= 0; i--) {
        parameterTypes.add(types.get(i));
        args[i] = popImmediate(types.get(i));
        methodArgs.add(args[i].stackOrValue());
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
        boxes[i] = indy.getArgBox(i);
        args[i].addBox(boxes[i]);
      }

      frame.boxes(boxes);
      frame.in(args);
      opr = new Operand(insn, indy);
      frame.out(opr);
    } else {
      opr = out[0];
      AbstractInvokeExpr expr = (AbstractInvokeExpr) opr.value;
      List<Type> types = expr.getMethodSignature().getParameterSignatures();
      Operand[] oprs;
      int nrArgs = types.size();
      if (expr instanceof JStaticInvokeExpr) {
        oprs = (nrArgs == 0) ? null : new Operand[nrArgs];
      } else {
        oprs = new Operand[nrArgs + 1];
      }
      if (oprs != null) {
        while (nrArgs-- >= 0) {
          oprs[nrArgs] = pop(types.get(nrArgs));
        }
        if (!(expr instanceof JStaticInvokeExpr)) {
          oprs[oprs.length - 1] = pop();
        }
        frame.mergeIn(oprs);
      }
      returnType = expr.getType();
    }
    if (AsmUtil.isDWord(returnType)) {
      pushDual(opr);
    } else if (!(returnType instanceof VoidType)) {
      push(opr);
    } else if (!units.containsKey(insn)) {
      // FIXME: [JMP] Set correct position information
      setUnit(insn, Jimple.newInvokeStmt(opr.value, StmtPositionInfo.createNoStmtPositionInfo()));
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
  // private FieldRef toSootFieldRef(Handle methodHandle) {
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
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      ArrayType t = (ArrayType) AsmUtil.toJimpleType(insn.desc);
      int dims = insn.dims;
      Operand[] sizes = new Operand[dims];
      Value[] sizeVals = new Value[dims];
      ValueBox[] boxes = new ValueBox[dims];
      while (dims-- != 0) {
        sizes[dims] = popImmediate();
        sizeVals[dims] = sizes[dims].stackOrValue();
      }
      JNewMultiArrayExpr nm = Jimple.newNewMultiArrayExpr(t, Arrays.asList(sizeVals));
      for (int i = 0; i != boxes.length; i++) {
        ValueBox vb = nm.getSizeBox(i);
        sizes[i].addBox(vb);
        boxes[i] = vb;
      }
      frame.boxes(boxes);
      frame.in(sizes);
      opr = new Operand(insn, nm);
      frame.out(opr);
    } else {
      opr = out[0];
      int dims = insn.dims;
      Operand[] sizes = new Operand[dims];
      while (dims-- != 0) {
        sizes[dims] = pop();
      }
      frame.mergeIn(sizes);
    }
    push(opr);
  }

  private void convertTableSwitchInsn(@Nonnull TableSwitchInsnNode insn) {
    StackFrame frame = getFrame(insn);
    if (units.containsKey(insn)) {
      frame.mergeIn(pop());
      return;
    }
    Operand key = popImmediate();
    StmtBox dflt = Jimple.newStmtBox(null);
    List<StmtBox> targets = new ArrayList<>(insn.labels.size());
    labels.put(insn.dflt, dflt);
    for (LabelNode ln : insn.labels) {
      StmtBox box = Jimple.newStmtBox(null);
      targets.add(box);
      labels.put(ln, box);
    }
    JTableSwitchStmt tss =
        Jimple.newTableSwitchStmt(
            key.stackOrValue(),
            insn.min,
            insn.max,
            targets,
            dflt,
            StmtPositionInfo.createNoStmtPositionInfo());
    key.addBox(tss.getKeyBox());
    frame.in(key);
    frame.boxes(tss.getKeyBox());
    setUnit(insn, tss);
  }

  private void convertTypeInsn(@Nonnull TypeInsnNode insn) {
    int op = insn.getOpcode();
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      Type t = JavaIdentifierFactory.getInstance().getType(insn.desc);
      Value val;
      if (op == NEW) {
        val = Jimple.newNewExpr((ReferenceType) t);
      } else {
        Operand op1 = popImmediate();
        Value v1 = op1.stackOrValue();
        ValueBox vb;
        if (op == ANEWARRAY) {
          JNewArrayExpr expr = JavaJimple.getInstance().newNewArrayExpr(t, v1);
          vb = expr.getSizeBox();
          val = expr;
        } else if (op == CHECKCAST) {
          JCastExpr expr = Jimple.newCastExpr(v1, t);
          vb = expr.getOpBox();
          val = expr;
        } else if (op == INSTANCEOF) {
          JInstanceOfExpr expr = Jimple.newInstanceOfExpr(v1, t);
          vb = expr.getOpBox();
          val = expr;
        } else {
          throw new AssertionError("Unknown type op: " + op);
        }
        op1.addBox(vb);
        frame.in(op1);
        frame.boxes(vb);
      }
      opr = new Operand(insn, val);
      frame.out(opr);
    } else {
      opr = out[0];
      if (op != NEW) {
        frame.mergeIn(pop());
      }
    }
    push(opr);
  }

  private void convertVarLoadInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LLOAD || op == DLOAD;
    StackFrame frame = getFrame(insn);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      opr = new Operand(insn, getLocal(insn.var));
      frame.out(opr);
    } else {
      opr = out[0];
    }
    if (dword) {
      pushDual(opr);
    } else {
      push(opr);
    }
  }

  private void convertVarStoreInsn(@Nonnull VarInsnNode insn) {
    int op = insn.getOpcode();
    boolean dword = op == LSTORE || op == DSTORE;
    StackFrame frame = getFrame(insn);
    Operand opr = dword ? popDual() : pop();
    Local local = getLocal(insn.var);
    if (!units.containsKey(insn)) {
      AbstractDefinitionStmt as =
          Jimple.newAssignStmt(
              local, opr.stackOrValue(), StmtPositionInfo.createNoStmtPositionInfo());
      opr.addBox(as.getRightOpBox());
      frame.boxes(as.getRightOpBox());
      frame.in(opr);
      setUnit(insn, as);
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
      /* we handle it, even thought it should be removed */
      if (!units.containsKey(insn)) {
        setUnit(
            insn,
            Jimple.newRetStmt(getLocal(insn.var), StmtPositionInfo.createNoStmtPositionInfo()));
      }
    } else {
      throw new AssertionError("Unknown var op: " + op);
    }
  }

  private void convertLabel(@Nonnull LabelNode ln) {
    if (!trapHandlers.containsKey(ln)) {
      return;
    }

    // We create a nop statement as a placeholder so that we can jump
    // somewhere from the real exception handler in case this is inline
    // code
    if (inlineExceptionLabels.contains(ln)) {
      if (!units.containsKey(ln)) {
        JNopStmt nop = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
        setUnit(ln, nop);
      }
      return;
    }

    StackFrame frame = getFrame(ln);
    Operand[] out = frame.out();
    Operand opr;
    if (out == null) {
      JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
      Local stack = newStackLocal();
      AbstractDefinitionStmt as =
          Jimple.newIdentityStmt(stack, ref, StmtPositionInfo.createNoStmtPositionInfo());
      opr = new Operand(ln, ref);
      opr.stack = stack;
      frame.out(opr);
      setUnit(ln, as);
    } else {
      opr = out[0];
    }
    push(opr);
  }

  private void convertLine(@Nonnull LineNumberNode ln) {
    lastLineNumber = ln.line;
  }

  /* Conversion */

  // FIXME: is it reasonable to get rid of it?
  private final class Edge {
    /* edge endpoint */
    final AbstractInsnNode insn;
    /* previous stacks at edge */
    final LinkedList<Operand[]> prevStacks;
    /* current stack at edge */
    ArrayList<Operand> stack;

    Edge(AbstractInsnNode insn, ArrayList<Operand> stack) {
      this.insn = insn;
      this.prevStacks = new LinkedList<>();
      this.stack = stack;
    }

    Edge(AbstractInsnNode insn) {
      this(insn, new ArrayList<>(AsmMethodSource.this.stack));
    }
  }

  private Table<AbstractInsnNode, AbstractInsnNode, Edge> edges;
  private ArrayDeque<Edge> conversionWorklist;

  private void addEdges(
      @Nonnull AbstractInsnNode cur,
      @Nonnull AbstractInsnNode tgt1,
      @Nullable List<LabelNode> tgts) {
    int lastIdx = tgts == null ? -1 : tgts.size() - 1;
    Operand[] stackss = (new ArrayList<>(stack)).toArray(new Operand[stack.size()]);
    AbstractInsnNode tgt = tgt1;
    int i = 0;
    tgt_loop:
    do {
      Edge edge = edges.get(cur, tgt);
      if (edge == null) {
        edge = new Edge(tgt);
        edge.prevStacks.add(stackss);
        edges.put(cur, tgt, edge);
        conversionWorklist.add(edge);
        continue;
      }
      if (edge.stack != null) {
        ArrayList<Operand> stackTemp = edge.stack;
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
      for (Operand[] ps : edge.prevStacks) {
        if (Arrays.equals(ps, stackss)) {
          continue tgt_loop;
        }
      }
      edge.stack = new ArrayList<>(stack);
      edge.prevStacks.add(stackss);
      conversionWorklist.add(edge);
    } while (i <= lastIdx && (tgt = tgts.get(i++)) != null);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  private void convert() {
    ArrayDeque<Edge> worklist = new ArrayDeque<>();
    for (LabelNode ln : trapHandlers.keySet()) {
      if (checkInlineExceptionHandler(ln)) {
        handleInlineExceptionHandler(ln, worklist);
      } else {
        worklist.add(new Edge(ln, new ArrayList<>()));
      }
    }
    worklist.add(new Edge(instructions.getFirst(), new ArrayList<>()));
    conversionWorklist = worklist;
    edges = HashBasedTable.create(1, 1);

    do {
      Edge edge = worklist.pollLast();
      AbstractInsnNode insn = edge.insn;
      stack = edge.stack;
      edge.stack = null;
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
            addEdges(insn, next, Collections.singletonList(jmp.label));
          } else {
            addEdges(insn, jmp.label, null);
          }
          break;
        } else if (type == LOOKUPSWITCH_INSN) {
          LookupSwitchInsnNode swtch = (LookupSwitchInsnNode) insn;
          convertLookupSwitchInsn(swtch);
          LabelNode dflt = swtch.dflt;
          addEdges(insn, dflt, swtch.labels);
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
          addEdges(insn, dflt, swtch.labels);
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
        } else if (type == FRAME) {
          // we can ignore it
        } else {
          throw new RuntimeException("Unknown instruction type: " + type);
        }
      } while ((insn = insn.getNext()) != null);
    } while (!worklist.isEmpty());
    conversionWorklist = null;
    edges = null;
  }

  private void handleInlineExceptionHandler(LabelNode ln, ArrayDeque<Edge> worklist) {
    // Catch the exception
    JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
    Local local = newStackLocal();
    AbstractDefinitionStmt as =
        Jimple.newIdentityStmt(local, ref, StmtPositionInfo.createNoStmtPositionInfo());

    Operand opr = new Operand(ln, ref);
    opr.stack = local;

    ArrayList<Operand> stack = new ArrayList<>();
    stack.add(opr);
    worklist.add(new Edge(ln, stack));

    // Save the statements
    inlineExceptionHandlers.put(ln, as);
  }

  private boolean checkInlineExceptionHandler(@Nonnull LabelNode ln) {
    // If this label is reachable through an exception and through normal
    // code, we have to split the exceptional case (with the exception on
    // the stack) from the normal fall-through case without anything on the
    // stack.
    for (Iterator<AbstractInsnNode> it = instructions.iterator(); it.hasNext(); ) {
      AbstractInsnNode node = it.next();
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

  private void emitLocals(@Nonnull Collection<Local> jbl, @Nonnull Collection<Stmt> jbu)
      throws AsmFrontendException {

    MethodSignature methodSignature = lazyMethodSignature.get();

    int iloc = 0;
    if (!lazyModifiers.get().contains(Modifier.STATIC)) {
      Local l = getLocal(iloc++);
      jbu.add(
          Jimple.newIdentityStmt(
              l, Jimple.newThisRef(declaringClass), StmtPositionInfo.createNoStmtPositionInfo()));
    }
    int nrp = 0;
    for (Type ot : methodSignature.getParameterSignatures()) {
      Local l = getLocal(iloc);
      jbu.add(
          Jimple.newIdentityStmt(
              l, Jimple.newParameterRef(ot, nrp++), StmtPositionInfo.createNoStmtPositionInfo()));
      if (AsmUtil.isDWord(ot)) {
        iloc += 2;
      } else {
        iloc++;
      }
    }
    jbl.addAll(locals.values());
  }

  private void emitTraps(@Nonnull Collection<Trap> traps) throws AsmFrontendException {
    // SootClass throwable = Scene.v().getSootClass("java.lang.Throwable");
    JavaClassType throwable =
        JavaIdentifierFactory.getInstance().getClassType("java.lang.Throwable");

    Map<LabelNode, Iterator<StmtBox>> handlers = new HashMap<>(tryCatchBlocks.size());
    for (TryCatchBlockNode tc : tryCatchBlocks) {
      StmtBox start = Jimple.newStmtBox(null);
      StmtBox end = Jimple.newStmtBox(null);
      Iterator<StmtBox> hitr = handlers.get(tc.handler);
      if (hitr == null) {
        hitr = trapHandlers.get(tc.handler).iterator();
        handlers.put(tc.handler, hitr);
      }
      StmtBox handler = hitr.next();
      JavaClassType cls;
      if (tc.type == null) {
        cls = throwable;
      } else {
        // Scene.v().getSootClass(AsmUtil.toQualifiedName(tc.type));

        cls = JavaIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(tc.type));
      }
      Trap trap = Jimple.newTrap(cls, start, end, handler);
      traps.add(trap);
      labels.put(tc.start, start);
      labels.put(tc.end, end);
    }
  }

  private void emitUnits(@Nonnull List<Stmt> bodyStmts, @Nonnull Stmt u) {
    if (u instanceof StmtContainer) {
      for (Stmt uu : ((StmtContainer) u).units) {
        emitUnits(bodyStmts, uu);
      }
    } else {
      bodyStmts.add(u);
    }
  }

  private void emitUnits(@Nonnull List<Stmt> bodyStmts) {
    AbstractInsnNode insn = instructions.getFirst();
    ArrayDeque<LabelNode> labls = new ArrayDeque<>();

    while (insn != null) {
      // Save the label to assign it to the next real Stmt
      if (insn instanceof LabelNode) {
        labls.add((LabelNode) insn);
      }

      // Get the Stmt associated with the current instruction
      Stmt u = units.get(insn);
      if (u == null) {
        insn = insn.getNext();
        continue;
      }

      emitUnits(bodyStmts, u);

      // If this is an exception handler, register the starting Stmt for it
      {
        JIdentityStmt caughtEx = null;
        if (u instanceof JIdentityStmt) {
          caughtEx = (JIdentityStmt) u;
        } else if (u instanceof StmtContainer) {
          caughtEx = getIdentityRefFromContrainer((StmtContainer) u);
        }

        if (insn instanceof LabelNode
            && caughtEx != null
            && caughtEx.getRightOp() instanceof JCaughtExceptionRef) {
          // We directly place this label
          Collection<StmtBox> traps = trapHandlers.get((LabelNode) insn);
          for (StmtBox ub : traps) {
            StmtBox.$Accessor.setStmt(ub, caughtEx);
          }
        }
      }

      // Register this Stmt for all targets of the labels ending up at it
      while (!labls.isEmpty()) {
        LabelNode ln = labls.poll();
        Collection<StmtBox> boxes = labels.get(ln);
        if (boxes != null) {
          for (StmtBox box : boxes) {
            StmtBox.$Accessor.setStmt(
                box, u instanceof StmtContainer ? ((StmtContainer) u).getFirstUnit() : u);
          }
        }
      }
      insn = insn.getNext();
    }

    // Emit the inline exception handlers
    for (LabelNode ln : this.inlineExceptionHandlers.keySet()) {
      Stmt handler = this.inlineExceptionHandlers.get(ln);
      emitUnits(bodyStmts, handler);

      Collection<StmtBox> traps = trapHandlers.get(ln);
      for (StmtBox ub : traps) {
        StmtBox.$Accessor.setStmt(ub, handler);
      }

      // We need to jump to the original implementation
      Stmt targetUnit = units.get(ln);
      JGotoStmt gotoImpl =
          Jimple.newGotoStmt(targetUnit, StmtPositionInfo.createNoStmtPositionInfo());
      bodyStmts.add(gotoImpl);
    }

    /* set remaining labels & boxes to last Stmt of chain */
    if (labls.isEmpty()) {
      return;
    }
    Stmt end = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    bodyStmts.add(end);
    while (!labls.isEmpty()) {
      LabelNode ln = labls.poll();
      Collection<StmtBox> boxes = labels.get(ln);
      if (boxes != null) {
        for (StmtBox box : boxes) {
          StmtBox.$Accessor.setStmt(box, end);
        }
      }
    }
  }

  private @Nullable JIdentityStmt getIdentityRefFromContrainer(@Nonnull StmtContainer u) {
    for (Stmt uu : u.units) {
      if (uu instanceof JIdentityStmt) {
        return (JIdentityStmt) uu;
      } else if (uu instanceof StmtContainer) {
        return getIdentityRefFromContrainer((StmtContainer) uu);
      }
    }
    return null;
  }
}
