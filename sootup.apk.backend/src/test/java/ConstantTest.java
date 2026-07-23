import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.apk.backend.*;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.constant.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;

public class ConstantTest {
  static DexConstantVisitor dexConstantVisitor;
  static DexStmtVisitor dexStmtVisitor;
  static RegisterAllocator registerAllocator;
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
    dexConstantVisitor = new DexConstantVisitor(dexMethodBuilder);
    registerAllocator = new RegisterAllocator(dexConstantVisitor);
    dexMethodBuilder.setRegisterAllocator(registerAllocator);
    dexStmtVisitor =
        new DexStmtVisitor(null, registerAllocator, dexConstantVisitor, dexMethodBuilder, null);
  }

  @Test
  public void testSmallInt() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant numConst1 = IntConstant.getInstance(2);
    numConst1.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);

    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction11n.class, builderInstruction);
    BuilderInstruction11n builderInstruction11n = (BuilderInstruction11n) builderInstruction;

    assertEquals(Opcode.CONST_4, builderInstruction11n.getOpcode());
    assertEquals(0, builderInstruction11n.getRegisterA());
    assertEquals(2, builderInstruction11n.getNarrowLiteral());
  }

  @Test
  public void testMediumInt() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant numConst1 = IntConstant.getInstance(42);
    numConst1.accept(dexConstantVisitor);

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
  public void testLargeInt() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant numConst1 = IntConstant.getInstance(60000);
    numConst1.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction31i.class, builderInstruction);
    BuilderInstruction31i builderInstruction31i = (BuilderInstruction31i) builderInstruction;

    assertEquals(Opcode.CONST, builderInstruction31i.getOpcode());
    assertEquals(0, builderInstruction31i.getRegisterA());
    assertEquals(60000, builderInstruction31i.getNarrowLiteral());
  }

  @Test
  public void testLong() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getLong());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant numConst1 = LongConstant.getInstance(8000000000L);
    numConst1.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction51l.class, builderInstruction);
    BuilderInstruction51l builderInstruction51l = (BuilderInstruction51l) builderInstruction;

    assertEquals(Opcode.CONST_WIDE, builderInstruction51l.getOpcode());
    assertEquals(0, builderInstruction51l.getRegisterA());
    assertEquals(8000000000L, builderInstruction51l.getWideLiteral());
  }

  @Test
  public void testBoolean() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getBoolean());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant booleanTrue = BooleanConstant.getTrue();
    booleanTrue.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    BuilderInstruction11n builderInstruction11n = (BuilderInstruction11n) builderInstruction;

    assertInstanceOf(BuilderInstruction11n.class, builderInstruction);
    assertEquals(Opcode.CONST_4, builderInstruction11n.getOpcode());
    assertEquals(0, builderInstruction11n.getRegisterA());
    assertEquals(1, builderInstruction11n.getNarrowLiteral());

    Constant booleanFalse = BooleanConstant.getFalse();
    booleanFalse.accept(dexConstantVisitor);

    instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(2, instructions.size());

    builderInstruction = instructions.get(1);
    builderInstruction11n = (BuilderInstruction11n) builderInstruction;

    assertInstanceOf(BuilderInstruction11n.class, builderInstruction);
    assertEquals(Opcode.CONST_4, builderInstruction11n.getOpcode());
    assertEquals(0, builderInstruction11n.getRegisterA());
    assertEquals(0, builderInstruction11n.getNarrowLiteral());
  }

  @Test
  public void testDouble() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getDouble());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant doubleConst = DoubleConstant.getInstance(3.141592653589793);
    doubleConst.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction51l.class, builderInstruction);
    BuilderInstruction51l builderInstruction51l = (BuilderInstruction51l) builderInstruction;

    assertEquals(Opcode.CONST_WIDE, builderInstruction51l.getOpcode());
    assertEquals(0, builderInstruction51l.getRegisterA());
    assertEquals(
        Double.doubleToLongBits(3.141592653589793), builderInstruction51l.getWideLiteral());
  }

  @Test
  public void testFloat() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getFloat());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant floatConstant = FloatConstant.getInstance(19.99f);
    floatConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction31i.class, builderInstruction);
    BuilderInstruction31i builderInstruction31i = (BuilderInstruction31i) builderInstruction;

    assertEquals(Opcode.CONST, builderInstruction31i.getOpcode());
    assertEquals(0, builderInstruction31i.getRegisterA());
    assertEquals(Float.floatToIntBits(19.99f), builderInstruction31i.getWideLiteral());
  }

  @Test
  public void testNull() {
    Register targetRegister = registerAllocator.getRegisterForType(NullType.getInstance());
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant nullConstant = NullConstant.getInstance();
    nullConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21s.class, builderInstruction);
    BuilderInstruction21s builderInstruction21s = (BuilderInstruction21s) builderInstruction;

    assertEquals(Opcode.CONST_16, builderInstruction21s.getOpcode());
    assertEquals(0, builderInstruction21s.getRegisterA());
    assertEquals(0, builderInstruction21s.getNarrowLiteral());
  }

  @Test
  public void testString() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    ClassType stringClass = identifierFactory.getClassType("java.lang.String");
    Register targetRegister = registerAllocator.getRegisterForType(stringClass);
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant stringConstant = new StringConstant("test", stringClass);
    stringConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.CONST_STRING, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());
    assertEquals(new ImmutableStringReference("test"), builderInstruction21c.getReference());
  }

  @Test
  public void testEnum() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    ClassType myEnumType = identifierFactory.getClassType("com.example.MyEnum");
    EnumConstant enumConstant = new EnumConstant("VALUE", myEnumType);
    enumConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.SGET_OBJECT, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());

    Reference reference = builderInstruction21c.getReference();
    assertInstanceOf(FieldReference.class, reference);
    FieldReference fieldReference = (FieldReference) reference;
    assertEquals("Lcom/example/MyEnum;", fieldReference.getDefiningClass());
    assertEquals("VALUE", fieldReference.getName());
    assertEquals("Lcom/example/MyEnum;", fieldReference.getType());
  }

  @Test
  public void testClassConstant() {
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    ClassType classType = identifierFactory.getClassType("java.lang.String");
    Register targetRegister = registerAllocator.getRegisterForType(classType);
    dexConstantVisitor.setTargetRegister(targetRegister);
    Constant classConstant = new ClassConstant("java/lang/String", classType);
    classConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.CONST_CLASS, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());

    Reference reference = builderInstruction21c.getReference();
    assertInstanceOf(TypeReference.class, reference);
    TypeReference typeReference = (TypeReference) reference;

    assertEquals("Ljava/lang/String;", typeReference.getType());
  }

  @Test
  public void testMethodType() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    MethodSubSignature subSig =
        identifierFactory.getMethodSubSignature(
            "foo",
            identifierFactory.getType("java.lang.String"),
            List.of(identifierFactory.getType("int")));

    ClassType methodTypeClass = identifierFactory.getClassType("java.lang.invoke.MethodType");
    Constant methodTypeConstant = new MethodType(subSig, methodTypeClass);

    methodTypeConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.CONST_METHOD_TYPE, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());

    MethodProtoReference proto =
        (MethodProtoReference) ((ReferenceInstruction) builderInstruction21c).getReference();
    assertEquals("Ljava/lang/String;", proto.getReturnType());
    assertEquals(1, proto.getParameterTypes().size());
    assertEquals("I", proto.getParameterTypes().get(0));
  }

  @Test
  public void testMethodHandle() {
    Register targetRegister = registerAllocator.getRegisterForType(PrimitiveType.getInt());
    dexConstantVisitor.setTargetRegister(targetRegister);
    IdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    ClassType owner = identifierFactory.getClassType("com.example.Foo");

    MethodSignature methodSig =
        identifierFactory.getMethodSignature(owner, "bar", "void", List.of("java.lang.String"));

    MethodHandle methodHandleConstant =
        new MethodHandle(
            methodSig,
            MethodHandle.Kind.REF_INVOKE_STATIC,
            identifierFactory.getClassType("java.lang.invoke.MethodHandle"));

    methodHandleConstant.accept(dexConstantVisitor);

    List<BuilderInstruction> instructions =
        dexMethodBuilder.addBuilderInstructions(methodImplementationBuilder, labelAssigner);
    assertEquals(1, instructions.size());

    BuilderInstruction builderInstruction = instructions.get(0);
    assertInstanceOf(BuilderInstruction21c.class, builderInstruction);
    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;

    assertEquals(Opcode.CONST_METHOD_HANDLE, builderInstruction21c.getOpcode());
    assertEquals(0, builderInstruction21c.getRegisterA());

    MethodHandleReference handle =
        (MethodHandleReference) ((ReferenceInstruction) builderInstruction21c).getReference();
    assertEquals(MethodHandleType.INVOKE_STATIC, handle.getMethodHandleType());
    assertInstanceOf(MethodReference.class, handle.getMemberReference());
  }
}
