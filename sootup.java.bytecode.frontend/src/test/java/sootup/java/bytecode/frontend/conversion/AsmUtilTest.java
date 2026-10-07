package sootup.java.bytecode.frontend.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.JavaIdentifierFactory;

public class AsmUtilTest {

  private final JavaIdentifierFactory identifierFactory = new JavaIdentifierFactory();

  @Test
  public void testIsDword() {
    assertTrue(AsmUtil.isDWord(PrimitiveType.getLong()));
    assertTrue(AsmUtil.isDWord(PrimitiveType.getDouble()));
    assertFalse(AsmUtil.isDWord(PrimitiveType.getInt()));
  }

  @Test
  public void testToQualifiedName() {
    assertEquals(AsmUtil.toQualifiedName("Ljava/lang/Object;"), "java.lang.Object");
    assertEquals(AsmUtil.toQualifiedName("java/lang/Object"), "java.lang.Object");
    assertEquals(AsmUtil.toQualifiedName("java.lang.Object"), "java.lang.Object");
  }

  @Test
  public void testToJimpleClassType() {
    ClassType classType = AsmUtil.toJimpleClassType("Ljava/lang/Object;", identifierFactory);
    assertEquals("java.lang.Object", classType.getFullyQualifiedName());
    ClassType classType2 = AsmUtil.toJimpleClassType("java/lang/Object", identifierFactory);
    assertEquals("java.lang.Object", classType2.getFullyQualifiedName());
    ClassType classType3 = AsmUtil.toJimpleClassType("java.lang.Object", identifierFactory);
    assertEquals("java.lang.Object", classType3.getFullyQualifiedName());
  }

  @Test
  public void testToJimpleTypePrimitives() {
    Type primitiveTypeZ = AsmUtil.toJimpleType("Z", identifierFactory);
    assertTrue(primitiveTypeZ instanceof PrimitiveType.BooleanType);
    Type primitiveTypeB = AsmUtil.toJimpleType("B", identifierFactory);
    assertTrue(primitiveTypeB instanceof PrimitiveType.ByteType);
    Type primitiveTypeC = AsmUtil.toJimpleType("C", identifierFactory);
    assertTrue(primitiveTypeC instanceof PrimitiveType.CharType);
    Type primitiveTypeS = AsmUtil.toJimpleType("S", identifierFactory);
    assertTrue(primitiveTypeS instanceof PrimitiveType.ShortType);
    Type primitiveTypeI = AsmUtil.toJimpleType("I", identifierFactory);
    assertTrue(primitiveTypeI instanceof PrimitiveType.IntType);
    Type primitiveTypeF = AsmUtil.toJimpleType("F", identifierFactory);
    assertTrue(primitiveTypeF instanceof PrimitiveType.FloatType);
    Type primitiveTypeJ = AsmUtil.toJimpleType("J", identifierFactory);
    assertTrue(primitiveTypeJ instanceof PrimitiveType.LongType);
    Type primitiveTypeD = AsmUtil.toJimpleType("D", identifierFactory);
    assertTrue(primitiveTypeD instanceof PrimitiveType.DoubleType);
    Type primitiveTypeV = AsmUtil.toJimpleType("V", identifierFactory);
    assertTrue(primitiveTypeV instanceof VoidType);
  }

  @Test
  public void testToJimpleTypeClass() {
    Type classType = AsmUtil.toJimpleType("Ljava/lang/Object;", identifierFactory);
    assertTrue(classType instanceof ClassType);
    assertEquals("java.lang.Object", ((ClassType) classType).getFullyQualifiedName());
  }

  @Test
  public void testToJimpleTypeArrays() {
    Type primitiveTypeZ = AsmUtil.toJimpleType("[Z", identifierFactory);
    assertTrue(primitiveTypeZ instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeZ).getBaseType() instanceof PrimitiveType.BooleanType);
    assertEquals(((ArrayType) primitiveTypeZ).getDimension(), 1);

    Type primitiveTypeB = AsmUtil.toJimpleType("[[B", identifierFactory);
    assertTrue(primitiveTypeB instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeB).getBaseType() instanceof PrimitiveType.ByteType);
    assertEquals(((ArrayType) primitiveTypeB).getDimension(), 2);

    Type primitiveTypeC = AsmUtil.toJimpleType("[[[C", identifierFactory);
    assertTrue(primitiveTypeC instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeC).getBaseType() instanceof PrimitiveType.CharType);
    assertEquals(((ArrayType) primitiveTypeC).getDimension(), 3);

    Type primitiveTypeS = AsmUtil.toJimpleType("[[[[S", identifierFactory);
    assertTrue(primitiveTypeS instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeS).getBaseType() instanceof PrimitiveType.ShortType);
    assertEquals(((ArrayType) primitiveTypeS).getDimension(), 4);

    Type primitiveTypeI = AsmUtil.toJimpleType("[[[[[I", identifierFactory);
    assertTrue(primitiveTypeI instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeI).getBaseType() instanceof PrimitiveType.IntType);
    assertEquals(((ArrayType) primitiveTypeI).getDimension(), 5);

    Type primitiveTypeF = AsmUtil.toJimpleType("[[[[F", identifierFactory);
    assertTrue(primitiveTypeF instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeF).getBaseType() instanceof PrimitiveType.FloatType);
    assertEquals(((ArrayType) primitiveTypeF).getDimension(), 4);

    Type primitiveTypeJ = AsmUtil.toJimpleType("[[[J", identifierFactory);
    assertTrue(primitiveTypeJ instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeJ).getBaseType() instanceof PrimitiveType.LongType);
    assertEquals(((ArrayType) primitiveTypeJ).getDimension(), 3);

    Type primitiveTypeD = AsmUtil.toJimpleType("[[D", identifierFactory);
    assertTrue(primitiveTypeD instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeD).getBaseType() instanceof PrimitiveType.DoubleType);
    assertEquals(((ArrayType) primitiveTypeD).getDimension(), 2);

    Type classType = AsmUtil.toJimpleType("[Ljava.lang.Object;", identifierFactory);
    assertTrue(classType instanceof ArrayType);
    assertTrue(((ArrayType) classType).getBaseType() instanceof ClassType);
    assertEquals(((ArrayType) classType).getDimension(), 1);
  }

  @Test
  public void testToJimpleTypeUnknownDescriptor() {
    Assertions.assertThrows(
        AssertionError.class, () -> AsmUtil.toJimpleType("P", identifierFactory));
  }

  @Test()
  public void testToJimpleTypeInvalidRefDescriptor() {
    Assertions.assertThrows(
        AssertionError.class, () -> AsmUtil.toJimpleType("L", identifierFactory));
  }

  @Test()
  public void testToJimpleTypeInvalidPrimitiveDescriptor() {
    Assertions.assertThrows(
        AssertionError.class, () -> AsmUtil.toJimpleType("II", identifierFactory));
  }

  @Test()
  public void testToJimpleTypeInvalidVoidDescriptor() {
    Assertions.assertThrows(
        AssertionError.class, () -> AsmUtil.toJimpleType("VI", identifierFactory));
  }

  @Test()
  public void testToJimpleTypeIncorrectArray() {
    Assertions.assertThrows(
        AssertionError.class, () -> AsmUtil.toJimpleType("[I[I", identifierFactory));
  }

  @Test
  public void testArrayTypeToJimpleType() {
    Type primitiveTypeZ = AsmUtil.arrayTypetoJimpleType("Z", identifierFactory);
    assertTrue(primitiveTypeZ instanceof ClassType);
    Type primitiveTypeB = AsmUtil.arrayTypetoJimpleType("B", identifierFactory);
    assertTrue(primitiveTypeB instanceof ClassType);
    Type primitiveTypeC = AsmUtil.arrayTypetoJimpleType("C", identifierFactory);
    assertTrue(primitiveTypeC instanceof ClassType);
    Type primitiveTypeS = AsmUtil.arrayTypetoJimpleType("S", identifierFactory);
    assertTrue(primitiveTypeS instanceof ClassType);
    Type primitiveTypeI = AsmUtil.arrayTypetoJimpleType("I", identifierFactory);
    assertTrue(primitiveTypeI instanceof ClassType);
    Type primitiveTypeF = AsmUtil.arrayTypetoJimpleType("F", identifierFactory);
    assertTrue(primitiveTypeF instanceof ClassType);
    Type primitiveTypeJ = AsmUtil.arrayTypetoJimpleType("J", identifierFactory);
    assertTrue(primitiveTypeJ instanceof ClassType);
    Type primitiveTypeD = AsmUtil.arrayTypetoJimpleType("D", identifierFactory);
    assertTrue(primitiveTypeD instanceof ClassType);
    Type primitiveTypeV = AsmUtil.arrayTypetoJimpleType("V", identifierFactory);
    assertTrue(primitiveTypeV instanceof ClassType);

    Type classType = AsmUtil.arrayTypetoJimpleType("java/lang/Object", identifierFactory);
    assertTrue(classType instanceof ClassType);
    assertEquals("java.lang.Object", ((ClassType) classType).getFullyQualifiedName());

    Type primitiveTypeZArray = AsmUtil.arrayTypetoJimpleType("[Z", identifierFactory);
    assertTrue(primitiveTypeZArray instanceof ArrayType);
    assertTrue(
        ((ArrayType) primitiveTypeZArray).getBaseType() instanceof PrimitiveType.BooleanType);
    assertEquals(((ArrayType) primitiveTypeZArray).getDimension(), 1);

    Type primitiveTypeBArray = AsmUtil.arrayTypetoJimpleType("[[B", identifierFactory);
    assertTrue(primitiveTypeBArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeBArray).getBaseType() instanceof PrimitiveType.ByteType);
    assertEquals(((ArrayType) primitiveTypeBArray).getDimension(), 2);

    Type primitiveTypeCArray = AsmUtil.arrayTypetoJimpleType("[[[C", identifierFactory);
    assertTrue(primitiveTypeCArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeCArray).getBaseType() instanceof PrimitiveType.CharType);
    assertEquals(((ArrayType) primitiveTypeCArray).getDimension(), 3);

    Type primitiveTypeSArray = AsmUtil.arrayTypetoJimpleType("[[[[S", identifierFactory);
    assertTrue(primitiveTypeSArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeSArray).getBaseType() instanceof PrimitiveType.ShortType);
    assertEquals(((ArrayType) primitiveTypeSArray).getDimension(), 4);

    Type primitiveTypeIArray = AsmUtil.arrayTypetoJimpleType("[[[[[I", identifierFactory);
    assertTrue(primitiveTypeIArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeIArray).getBaseType() instanceof PrimitiveType.IntType);
    assertEquals(((ArrayType) primitiveTypeIArray).getDimension(), 5);

    Type primitiveTypeFArray = AsmUtil.arrayTypetoJimpleType("[[[[F", identifierFactory);
    assertTrue(primitiveTypeFArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeFArray).getBaseType() instanceof PrimitiveType.FloatType);
    assertEquals(((ArrayType) primitiveTypeFArray).getDimension(), 4);

    Type primitiveTypeJArray = AsmUtil.arrayTypetoJimpleType("[[[J", identifierFactory);
    assertTrue(primitiveTypeJArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeJArray).getBaseType() instanceof PrimitiveType.LongType);
    assertEquals(((ArrayType) primitiveTypeJArray).getDimension(), 3);

    Type primitiveTypeDArray = AsmUtil.arrayTypetoJimpleType("[[D", identifierFactory);
    assertTrue(primitiveTypeDArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeDArray).getBaseType() instanceof PrimitiveType.DoubleType);
    assertEquals(((ArrayType) primitiveTypeDArray).getDimension(), 2);

    Type classTypeArray = AsmUtil.arrayTypetoJimpleType("[Ljava.lang.Object;", identifierFactory);
    assertTrue(classTypeArray instanceof ArrayType);
    assertTrue(((ArrayType) classTypeArray).getBaseType() instanceof ClassType);
    assertEquals(((ArrayType) classTypeArray).getDimension(), 1);
  }
}
