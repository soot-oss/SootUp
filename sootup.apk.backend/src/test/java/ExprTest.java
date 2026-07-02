import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Arrays;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.apk.backend.*;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.*;
import sootup.java.core.JavaIdentifierFactory;

public class ExprTest {

  static DexBuilder dexBuilder;
  static MethodImplementationBuilder methodImplementationBuilder;
  static DexConstantVisitor dexConstantVisitor;
  static DexStmtVisitor dexStmtVisitor;
  static DexExprVisitor dexExprVisitor;
  static RegisterAllocator registerAllocator;
  static Register targetRegister;

  @BeforeAll
  static void initAll() {
    dexBuilder = new DexBuilder(Opcodes.getDefault());
  }

  @BeforeEach
  void init() {
    dexStmtVisitor = new DexStmtVisitor();
    methodImplementationBuilder = new MethodImplementationBuilder(10);
    dexConstantVisitor = new DexConstantVisitor(dexBuilder, dexStmtVisitor);
    registerAllocator = new RegisterAllocator(dexConstantVisitor);
    dexExprVisitor =
        new DexExprVisitor(
            dexBuilder, methodImplementationBuilder, registerAllocator, dexStmtVisitor);
  }

  @Test
  public void testXor1() {
    Constant numConst1 = BooleanConstant.getTrue();
    Constant numConst2 = IntConstant.getInstance(-1);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    JXorExpr jXorExpr = new JXorExpr(numConst1, numConst2);
    dexExprVisitor.setTargetRegister(targetRegister);
    jXorExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.NOT_INT, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(1, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testXor2() {
    Constant numConst1 = LongConstant.getInstance(123);
    Constant numConst2 = LongConstant.getInstance(-1);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    JXorExpr jXorExpr = new JXorExpr(numConst1, numConst2);
    dexExprVisitor.setTargetRegister(targetRegister);
    jXorExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.NOT_LONG, builderInstruction12x.getOpcode());
    assertEquals(1, builderInstruction12x.getRegisterA());
    assertEquals(3, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testCastPrimitive() {
    Constant numConst1 = IntConstant.getInstance(123);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getByte());
    JCastExpr jCastExpr = new JCastExpr(numConst1, PrimitiveType.getByte());
    dexExprVisitor.setTargetRegister(targetRegister);
    jCastExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.INT_TO_BYTE, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(1, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testCastPrimitive2() {
    Constant numConst1 = LongConstant.getInstance(123L);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getByte());
    JCastExpr jCastExpr = new JCastExpr(numConst1, PrimitiveType.getByte());
    dexExprVisitor.setTargetRegister(targetRegister);
    jCastExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.INT_TO_BYTE, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(3, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testDivIntLit() {
    Constant numConst1 = IntConstant.getInstance(124);
    Constant numConst2 = IntConstant.getInstance(2);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    JDivExpr jDivExpr = new JDivExpr(numConst1, numConst2);
    dexExprVisitor.setTargetRegister(targetRegister);
    jDivExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction22b.class, builderInstruction);
    BuilderInstruction22b builderInstruction22b = (BuilderInstruction22b) builderInstruction;

    assertEquals(Opcode.DIV_INT_LIT8, builderInstruction22b.getOpcode());
    assertEquals(0, builderInstruction22b.getRegisterA());
    assertEquals(1, builderInstruction22b.getRegisterB());
    assertEquals(2, builderInstruction22b.getNarrowLiteral());
  }

  @Test
  public void testAddIntLit16() {
    Constant numConst1 = IntConstant.getInstance(123);
    Constant numConst2 = IntConstant.getInstance(456);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    JAddExpr jAddExpr = new JAddExpr(numConst1, numConst2);
    dexExprVisitor.setTargetRegister(targetRegister);
    jAddExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction22s.class, builderInstruction);
    BuilderInstruction22s builderInstruction22s = (BuilderInstruction22s) builderInstruction;

    assertEquals(Opcode.ADD_INT_LIT16, builderInstruction22s.getOpcode());
    assertEquals(0, builderInstruction22s.getRegisterA());
    assertEquals(1, builderInstruction22s.getRegisterB());
    assertEquals(456, builderInstruction22s.getNarrowLiteral());
  }

  @Test
  public void testSubLong2Addr() {
    Constant numConst1 = LongConstant.getInstance(1234567890123L);
    Constant numConst2 = LongConstant.getInstance(23);

    Local local = new Local("i1", PrimitiveType.getLong());
    Stmt lStmt1 = new JAssignStmt(local, numConst1, StmtPositionInfo.getNoStmtPositionInfo());
    lStmt1.accept(dexStmtVisitor);

    targetRegister = registerAllocator.getRegisterForImmediate(local);

    JSubExpr jSubExpr = new JSubExpr(local, numConst2);

    dexExprVisitor.setTargetRegister(targetRegister);
    jSubExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction22s = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.SUB_LONG_2ADDR, builderInstruction22s.getOpcode());
    assertEquals(0, builderInstruction22s.getRegisterA());
    assertEquals(2, builderInstruction22s.getRegisterB());
  }

  @Test
  public void testMulDouble() {
    Constant numConst1 = FloatConstant.getInstance(1234.56f);
    Constant numConst2 = FloatConstant.getInstance(12.33f);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getLong());

    JMulExpr jMulExpr = new JMulExpr(numConst1, numConst2);

    dexExprVisitor.setTargetRegister(targetRegister);
    jMulExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(4, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction);
    BuilderInstruction23x builderInstruction22s = (BuilderInstruction23x) builderInstruction;

    assertEquals(Opcode.MUL_FLOAT, builderInstruction22s.getOpcode());
    assertEquals(4, builderInstruction22s.getRegisterA());
    assertEquals(2, builderInstruction22s.getRegisterB());
    assertEquals(3, builderInstruction22s.getRegisterC());
  }

  @Test
  public void testInstanceOf() {
    ClassType classType = JavaIdentifierFactory.getInstance().getClassType("java.lang.String");

    Local local = Jimple.newLocal("x", classType);

    JInstanceOfExpr instanceOfExpr = new JInstanceOfExpr(local, classType);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getBoolean());
    dexExprVisitor.setTargetRegister(targetRegister);
    instanceOfExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction22c.class, builderInstruction);
    BuilderInstruction22c builderInstruction22c = (BuilderInstruction22c) builderInstruction;

    assertEquals(Opcode.INSTANCE_OF, builderInstruction22c.getOpcode());
    assertEquals(0, builderInstruction22c.getRegisterA());
    assertEquals(1, builderInstruction22c.getRegisterB());
    assertEquals("Ljava/lang/String;", builderInstruction22c.getReference().toString());
  }

  @Test
  public void testNewArray() {
    IdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    JNewArrayExpr newArrayExpr =
        Jimple.newNewArrayExpr(PrimitiveType.getInt(), IntConstant.getInstance(10), idFactory);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    newArrayExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction22c.class, builderInstruction);
    BuilderInstruction22c builderInstruction22c = (BuilderInstruction22c) builderInstruction;

    assertEquals(Opcode.NEW_ARRAY, builderInstruction22c.getOpcode());
    assertEquals(0, builderInstruction22c.getRegisterA());
    assertEquals(1, builderInstruction22c.getRegisterB());
    assertEquals("[I", builderInstruction22c.getReference().toString());
  }

  @Test
  public void testNewArray2() {
    IdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    Type type1 = PrimitiveType.getInt();
    Type type2 = ArrayType.createArrayType(type1, 2);
    JNewArrayExpr newArrayExpr =
        Jimple.newNewArrayExpr(type2, IntConstant.getInstance(10), idFactory);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    newArrayExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction22c.class, builderInstruction);
    BuilderInstruction22c builderInstruction22c = (BuilderInstruction22c) builderInstruction;

    assertEquals(Opcode.NEW_ARRAY, builderInstruction22c.getOpcode());
    assertEquals(0, builderInstruction22c.getRegisterA());
    assertEquals(1, builderInstruction22c.getRegisterB());
    assertEquals("[[[I", builderInstruction22c.getReference().toString());
  }

  @Test
  public void testNewMultiArray() {
    Type type1 = PrimitiveType.getInt();
    ArrayType type2 = ArrayType.createArrayType(type1, 3);

    List<Immediate> sizes = Arrays.asList(IntConstant.getInstance(10), IntConstant.getInstance(5));

    JNewMultiArrayExpr newMultiArrayExpr = Jimple.newNewMultiArrayExpr(type2, sizes);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    newMultiArrayExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(4, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction35c.class, builderInstruction);
    BuilderInstruction35c builderInstruction35c = (BuilderInstruction35c) builderInstruction;

    assertEquals(Opcode.FILLED_NEW_ARRAY, builderInstruction35c.getOpcode());
    assertEquals(1, builderInstruction35c.getRegisterC());
    assertEquals(2, builderInstruction35c.getRegisterD());
    assertEquals("[[I", builderInstruction35c.getReference().toString());
  }

  @Test
  public void testNewMultiArrayRange() {
    ArrayType arrayType = ArrayType.createArrayType(PrimitiveType.getLong(), 3);

    List<Immediate> sizes =
        Arrays.asList(
            LongConstant.getInstance(10), LongConstant.getInstance(5), LongConstant.getInstance(5));

    JNewMultiArrayExpr newMultiArrayExpr = Jimple.newNewMultiArrayExpr(arrayType, sizes);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    newMultiArrayExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(5, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(3);
    assertInstanceOf(BuilderInstruction3rc.class, builderInstruction);
    BuilderInstruction3rc builderInstruction3rc = (BuilderInstruction3rc) builderInstruction;

    assertEquals(Opcode.FILLED_NEW_ARRAY_RANGE, builderInstruction3rc.getOpcode());
    assertEquals(1, builderInstruction3rc.getStartRegister());
    assertEquals(6, builderInstruction3rc.getRegisterCount());
    assertEquals("[[[J", builderInstruction3rc.getReference().toString());
  }

  @Test
  public void testLength() {
    Local arrayLocal = Jimple.newLocal("arr", ClassType.createArrayType(PrimitiveType.getInt(), 1));
    JLengthExpr lengthExpr = Jimple.newLengthExpr(arrayLocal);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    lengthExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.ARRAY_LENGTH, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(1, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testNewInstance() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("java.lang.Object");
    JNewExpr newExpr = Jimple.newNewExpr(type);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    newExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.NEW_INSTANCE, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());
    assertEquals("Ljava/lang/Object;", builderInstruction21c.getReference().toString());
  }

  @Test
  public void testInvokeStatic() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type, new MethodSubSignature("myStaticMethod", List.of(), VoidType.getInstance()));

    JStaticInvokeExpr invokeExpr = Jimple.newStaticInvokeExpr(methodSignature, List.of());

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    invokeExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction35c.class, builderInstruction);
    BuilderInstruction35c builderInstruction35c = (BuilderInstruction35c) builderInstruction;

    assertEquals(Opcode.INVOKE_STATIC, builderInstruction35c.getOpcode());
    assertEquals(0, builderInstruction35c.getRegisterCount());
    assertEquals(
        "Lcom/example/MyClass;->myStaticMethod()V",
        builderInstruction35c.getReference().toString());
  }

  @Test
  public void testInvokeStatic2() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type,
            new MethodSubSignature(
                "myStaticMethod",
                List.of(
                    PrimitiveType.getInt(),
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String")),
                PrimitiveType.getInt()));

    JStaticInvokeExpr invokeExpr =
        Jimple.newStaticInvokeExpr(
            methodSignature,
            List.of(
                IntConstant.getInstance(42),
                new StringConstant(
                    "hello",
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String"))));

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    invokeExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(4, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction35c.class, builderInstruction);
    BuilderInstruction35c builderInstruction35c = (BuilderInstruction35c) builderInstruction;

    assertEquals(Opcode.INVOKE_STATIC, builderInstruction35c.getOpcode());
    assertEquals(2, builderInstruction35c.getRegisterCount());
    assertEquals(
        "Lcom/example/MyClass;->myStaticMethod(ILjava/lang/String;)I",
        builderInstruction35c.getReference().toString());
  }

  @Test
  public void testInvokeStatic3() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type,
            new MethodSubSignature(
                "myStaticMethod",
                List.of(
                    PrimitiveType.getInt(),
                    PrimitiveType.getInt(),
                    PrimitiveType.getInt(),
                    PrimitiveType.getInt(),
                    PrimitiveType.getInt(),
                    PrimitiveType.getInt()),
                PrimitiveType.getInt()));

    JStaticInvokeExpr invokeExpr =
        Jimple.newStaticInvokeExpr(
            methodSignature,
            List.of(
                IntConstant.getInstance(42),
                IntConstant.getInstance(43),
                IntConstant.getInstance(44),
                IntConstant.getInstance(45),
                IntConstant.getInstance(46),
                IntConstant.getInstance(47)));

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    invokeExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(8, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(6);
    assertInstanceOf(BuilderInstruction3rc.class, builderInstruction);
    BuilderInstruction3rc builderInstruction3rc = (BuilderInstruction3rc) builderInstruction;

    assertEquals(Opcode.INVOKE_STATIC_RANGE, builderInstruction3rc.getOpcode());
    assertEquals(6, builderInstruction3rc.getRegisterCount());
    assertEquals(
        "Lcom/example/MyClass;->myStaticMethod(IIIIII)I",
        builderInstruction3rc.getReference().toString());
  }

  @Test
  public void testNegExpr() {
    Constant numConst1 = IntConstant.getInstance(42);
    JNegExpr negExpr = new JNegExpr(numConst1);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    negExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.NEG_INT, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(1, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testNegExpr2() {
    Constant numConst1 = DoubleConstant.getInstance(1.2345);
    JNegExpr negExpr = new JNegExpr(numConst1);
    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getDouble());
    dexExprVisitor.setTargetRegister(targetRegister);
    negExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.NEG_DOUBLE, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(2, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testConditionExpr1() {
    Local local = Jimple.newLocal("obj", PrimitiveType.getInt());
    JNeExpr neExpr = Jimple.newNeExpr(local, IntConstant.getInstance(3));
    neExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction22t.class, builderInstruction);
    BuilderInstruction22t builderInstruction22t = (BuilderInstruction22t) builderInstruction;

    assertEquals(Opcode.IF_NE, builderInstruction22t.getOpcode());
    assertEquals(0, builderInstruction22t.getRegisterA());
    assertEquals(1, builderInstruction22t.getRegisterB());
  }

  @Test
  public void testConditionExpr2() {
    Local local = Jimple.newLocal("obj", PrimitiveType.getInt());
    JGtExpr ntExpr = Jimple.newGtExpr(local, IntConstant.getInstance(0));
    ntExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21t.class, builderInstruction);
    BuilderInstruction21t builderInstruction21t = (BuilderInstruction21t) builderInstruction;

    assertEquals(Opcode.IF_GTZ, builderInstruction21t.getOpcode());
    assertEquals(0, builderInstruction21t.getRegisterA());
  }

  @Test
  public void testCmp() {

    Local a = Jimple.newLocal("a", PrimitiveType.getLong());
    Local b = Jimple.newLocal("b", PrimitiveType.getLong());

    JCmpExpr cmpExpr = Jimple.newCmpExpr(a, b);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    cmpExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction);
    BuilderInstruction23x builderInstruction23x = (BuilderInstruction23x) builderInstruction;

    assertEquals(Opcode.CMP_LONG, builderInstruction23x.getOpcode());
    assertEquals(0, builderInstruction23x.getRegisterA());
    assertEquals(1, builderInstruction23x.getRegisterB());
    assertEquals(3, builderInstruction23x.getRegisterC());
  }

  @Test
  public void testCmpg() {
    Local a = Jimple.newLocal("a", PrimitiveType.getFloat());
    Local b = Jimple.newLocal("b", PrimitiveType.getFloat());

    JCmpgExpr cmpgExpr = Jimple.newCmpgExpr(a, b);

    targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexExprVisitor.setTargetRegister(targetRegister);
    cmpgExpr.accept(dexExprVisitor);

    List<BuilderInstruction> instructions = dexStmtVisitor.getInstructions();
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction);
    BuilderInstruction23x builderInstruction23x = (BuilderInstruction23x) builderInstruction;

    assertEquals(Opcode.CMPG_FLOAT, builderInstruction23x.getOpcode());
    assertEquals(0, builderInstruction23x.getRegisterA());
    assertEquals(1, builderInstruction23x.getRegisterB());
    assertEquals(2, builderInstruction23x.getRegisterC());
  }
}
