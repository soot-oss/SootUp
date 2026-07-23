import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.apk.backend.*;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;

public class StmtTest {

  DexStmtVisitor dexStmtVisitor;
  static DexMethodBuilder dexMethodBuilder;
  static MethodImplementationBuilder methodImplementationBuilder;
  static LabelAssigner labelAssigner;

  @BeforeAll
  static void initAll() {
    methodImplementationBuilder = new MethodImplementationBuilder(2);
    labelAssigner = new LabelAssigner(methodImplementationBuilder);
  }

  @BeforeEach
  void init() {
    dexMethodBuilder = new DexMethodBuilder(null);
    DexConstantVisitor dexConstantVisitor = new DexConstantVisitor(dexMethodBuilder);
    RegisterAllocator registerAllocator = new RegisterAllocator(dexConstantVisitor);
    dexMethodBuilder.setRegisterAllocator(registerAllocator);
    dexStmtVisitor =
        new DexStmtVisitor(null, registerAllocator, dexConstantVisitor, dexMethodBuilder, null);
  }

  @Test
  public void testAssignConst() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Immediate numConst1 = IntConstant.getInstance(42);
    Local local = new Local("i0", PrimitiveType.getInt());
    Stmt lStmt = new JAssignStmt(local, numConst1, nop);
    lStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());
    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21s.class, builderInstruction);
    BuilderInstruction21s builderInstruction21s = (BuilderInstruction21s) builderInstruction;

    assertEquals(Opcode.CONST_16, builderInstruction21s.getOpcode());
    assertEquals(0, builderInstruction21s.getRegisterA());
    assertEquals(42, builderInstruction21s.getNarrowLiteral());
  }

  @Test
  public void testAssignLocal() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Immediate numConst1 = IntConstant.getInstance(42);
    Local local = new Local("i0", PrimitiveType.getInt());
    Stmt lStmt = new JAssignStmt(local, numConst1, nop);

    Immediate numConst2 = IntConstant.getInstance(40);
    Local local2 = new Local("i1", PrimitiveType.getInt());
    Stmt lStmt2 = new JAssignStmt(local2, numConst2, nop);

    Stmt lStmt3 = new JAssignStmt(local, local2, nop);

    lStmt.accept(dexStmtVisitor);
    lStmt2.accept(dexStmtVisitor);
    lStmt3.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction12x.class, builderInstruction);
    BuilderInstruction12x builderInstruction12x = (BuilderInstruction12x) builderInstruction;

    assertEquals(Opcode.MOVE, builderInstruction12x.getOpcode());
    assertEquals(0, builderInstruction12x.getRegisterA());
    assertEquals(1, builderInstruction12x.getRegisterB());
  }

  @Test
  public void testAssignRefGet() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Local local = new Local("i0", PrimitiveType.getInt());
    ArrayType intArrayType = new ArrayType(PrimitiveType.getInt(), 1);
    Local arrayLocal = Jimple.newLocal("arr", intArrayType);
    JArrayRef arrayRef = Jimple.newArrayRef(arrayLocal, IntConstant.getInstance(0));
    Stmt stmt = new JAssignStmt(local, arrayRef, nop);

    stmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(2, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(1);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction);
    BuilderInstruction23x builderInstruction23x = (BuilderInstruction23x) builderInstruction;

    assertEquals(Opcode.AGET, builderInstruction23x.getOpcode());
    assertEquals(0, builderInstruction23x.getRegisterA());
    assertEquals(1, builderInstruction23x.getRegisterB());
    assertEquals(2, builderInstruction23x.getRegisterC());
  }

  @Test
  public void testAssignRefPut() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Immediate numConst = BooleanConstant.getTrue();
    ArrayType booleanArrayType = new ArrayType(PrimitiveType.getBoolean(), 1);
    Local arrayLocal = Jimple.newLocal("arr", booleanArrayType);
    JArrayRef arrayRef = Jimple.newArrayRef(arrayLocal, IntConstant.getInstance(0));
    Stmt stmt = new JAssignStmt(arrayRef, numConst, nop);

    stmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction);
    BuilderInstruction23x builderInstruction23x = (BuilderInstruction23x) builderInstruction;

    assertEquals(Opcode.APUT_BOOLEAN, builderInstruction23x.getOpcode());
    assertEquals(0, builderInstruction23x.getRegisterA());
    assertEquals(1, builderInstruction23x.getRegisterB());
    assertEquals(2, builderInstruction23x.getRegisterC());
  }

  @Test
  public void testAssignStmt() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Immediate numConst1 = IntConstant.getInstance(42);
    Immediate numConst2 = IntConstant.getInstance(33102);
    Local local = new Local("i0", PrimitiveType.getInt());
    Stmt deepStmt = new JAssignStmt(local, new JAddExpr(numConst1, numConst2), nop);

    deepStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction1 = instructions.get(0);
    assertInstanceOf(BuilderInstruction21s.class, builderInstruction1);
    BuilderInstruction21s builderInstruction21s1 = (BuilderInstruction21s) builderInstruction1;

    assertEquals(Opcode.CONST_16, builderInstruction21s1.getOpcode());
    assertEquals(1, builderInstruction21s1.getRegisterA());
    assertEquals(42, builderInstruction21s1.getNarrowLiteral());

    BuilderInstruction builderInstruction2 = instructions.get(1);
    assertInstanceOf(BuilderInstruction31i.class, builderInstruction2);
    BuilderInstruction31i builderInstruction31i2 = (BuilderInstruction31i) builderInstruction2;

    assertEquals(Opcode.CONST, builderInstruction31i2.getOpcode());
    assertEquals(2, builderInstruction31i2.getRegisterA());
    assertEquals(33102, builderInstruction31i2.getNarrowLiteral());

    BuilderInstruction builderInstruction3 = instructions.get(2);
    assertInstanceOf(BuilderInstruction23x.class, builderInstruction3);
    BuilderInstruction23x builderInstruction23x = (BuilderInstruction23x) builderInstruction3;

    assertEquals(Opcode.ADD_INT, builderInstruction23x.getOpcode());
    assertEquals(0, builderInstruction23x.getRegisterA());
    assertEquals(1, builderInstruction23x.getRegisterB());
    assertEquals(2, builderInstruction23x.getRegisterC());
  }

  @Test
  public void testAssignInvokeVirtual() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type,
            new MethodSubSignature(
                "myVirtualMethod",
                List.of(
                    PrimitiveType.getInt(),
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String")),
                PrimitiveType.getInt()));

    Local base = Jimple.newLocal("obj", type);
    JNewExpr newExpr = Jimple.newNewExpr(type);
    JAssignStmt jAssignStmt =
        new JAssignStmt(base, newExpr, StmtPositionInfo.getNoStmtPositionInfo());
    jAssignStmt.accept(dexStmtVisitor);

    JVirtualInvokeExpr invokeExpr =
        Jimple.newVirtualInvokeExpr(
            base,
            methodSignature,
            List.of(
                IntConstant.getInstance(42),
                new StringConstant(
                    "hello",
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String"))));

    Local target = Jimple.newLocal("target", PrimitiveType.getInt());
    JAssignStmt jAssignStmt2 =
        new JAssignStmt(target, invokeExpr, StmtPositionInfo.getNoStmtPositionInfo());
    jAssignStmt2.accept(dexStmtVisitor);
    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(5, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(3);
    assertInstanceOf(BuilderInstruction35c.class, builderInstruction);
    BuilderInstruction35c builderInstruction35c = (BuilderInstruction35c) builderInstruction;

    assertEquals(Opcode.INVOKE_VIRTUAL, builderInstruction35c.getOpcode());
    assertEquals(3, builderInstruction35c.getRegisterCount());
    assertEquals(
        "Lcom/example/MyClass;->myVirtualMethod(ILjava/lang/String;)I",
        builderInstruction35c.getReference().toString());
  }

  @Test
  public void testInvokeStmt() {
    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type,
            new MethodSubSignature(
                "myVirtualMethod",
                List.of(
                    PrimitiveType.getInt(),
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String")),
                PrimitiveType.getInt()));
    Local base = Jimple.newLocal("obj", type);
    JVirtualInvokeExpr invokeExpr =
        Jimple.newVirtualInvokeExpr(
            base,
            methodSignature,
            List.of(
                IntConstant.getInstance(42),
                new StringConstant(
                    "hello",
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String"))));

    JInvokeStmt jInvokeStmt = new JInvokeStmt(invokeExpr, StmtPositionInfo.getNoStmtPositionInfo());

    jInvokeStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(3, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(2);
    assertInstanceOf(BuilderInstruction35c.class, builderInstruction);
    BuilderInstruction35c builderInstruction35c = (BuilderInstruction35c) builderInstruction;

    assertEquals(Opcode.INVOKE_VIRTUAL, builderInstruction35c.getOpcode());
    assertEquals(3, builderInstruction35c.getRegisterCount());
    assertEquals(
        "Lcom/example/MyClass;->myVirtualMethod(ILjava/lang/String;)I",
        builderInstruction35c.getReference().toString());
  }

  @Test
  public void testNopStmt() {
    JNopStmt jNopStmt = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

    jNopStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction10x.class, builderInstruction);
    BuilderInstruction10x builderInstruction10x = (BuilderInstruction10x) builderInstruction;
    assertEquals(Opcode.NOP, builderInstruction10x.getOpcode());
  }

  @Test
  public void testReturnStmt() {
    Local local = new Local("i0", PrimitiveType.getLong());
    JReturnStmt jReturnStmt = new JReturnStmt(local, StmtPositionInfo.getNoStmtPositionInfo());

    jReturnStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction11x.class, builderInstruction);
    BuilderInstruction11x builderInstruction11x = (BuilderInstruction11x) builderInstruction;
    assertEquals(Opcode.RETURN_WIDE, builderInstruction11x.getOpcode());
    assertEquals(0, builderInstruction11x.getRegisterA());
  }

  @Test
  public void testReturnVoidStmt() {
    JReturnVoidStmt jReturnVoidStmt = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    jReturnVoidStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction10x.class, builderInstruction);
    BuilderInstruction10x builderInstruction10x = (BuilderInstruction10x) builderInstruction;
    assertEquals(Opcode.RETURN_VOID, builderInstruction10x.getOpcode());
  }

  @Test
  public void testThrowStmt() {
    Local local =
        new Local(
            "r0", JavaIdentifierFactory.getInstance().getClassType("java.lang.RuntimeException"));
    JThrowStmt throwStmt = new JThrowStmt(local, StmtPositionInfo.getNoStmtPositionInfo());

    throwStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction11x.class, builderInstruction);
    BuilderInstruction11x builderInstruction11x = (BuilderInstruction11x) builderInstruction;
    assertEquals(Opcode.THROW, builderInstruction11x.getOpcode());
    assertEquals(0, builderInstruction11x.getRegisterA());
  }

  @Test
  public void testMonitor() {
    Local lock =
        new Local("r0", JavaIdentifierFactory.getInstance().getClassType("java.lang.Object"));

    JEnterMonitorStmt jEnterMonitorStmt =
        new JEnterMonitorStmt(lock, StmtPositionInfo.getNoStmtPositionInfo());

    jEnterMonitorStmt.accept(dexStmtVisitor);

    ClassType type = JavaIdentifierFactory.getInstance().getClassType("com.example.MyClass");
    MethodSignature methodSignature =
        new MethodSignature(
            type,
            new MethodSubSignature(
                "myVirtualMethod",
                List.of(
                    PrimitiveType.getInt(),
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String")),
                PrimitiveType.getInt()));
    Local base = Jimple.newLocal("obj", type);
    JVirtualInvokeExpr invokeExpr =
        Jimple.newVirtualInvokeExpr(
            base,
            methodSignature,
            List.of(
                IntConstant.getInstance(42),
                new StringConstant(
                    "hello",
                    JavaIdentifierFactory.getInstance().getClassType("java.lang.String"))));

    JInvokeStmt jInvokeStmt = new JInvokeStmt(invokeExpr, StmtPositionInfo.getNoStmtPositionInfo());

    jInvokeStmt.accept(dexStmtVisitor);

    JExitMonitorStmt jExitMonitorStmt =
        new JExitMonitorStmt(lock, StmtPositionInfo.getNoStmtPositionInfo());

    jExitMonitorStmt.accept(dexStmtVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(5, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction11x.class, builderInstruction);
    BuilderInstruction11x builderInstruction11x = (BuilderInstruction11x) builderInstruction;
    assertEquals(Opcode.MONITOR_ENTER, builderInstruction11x.getOpcode());
    assertEquals(0, builderInstruction11x.getRegisterA());

    BuilderInstruction builderInstruction2 = instructions.get(4);
    assertInstanceOf(BuilderInstruction11x.class, builderInstruction2);
    BuilderInstruction11x builderInstruction11x2 = (BuilderInstruction11x) builderInstruction2;
    assertEquals(Opcode.MONITOR_EXIT, builderInstruction11x2.getOpcode());
    assertEquals(builderInstruction11x.getRegisterA(), builderInstruction11x2.getRegisterA());
  }
}
