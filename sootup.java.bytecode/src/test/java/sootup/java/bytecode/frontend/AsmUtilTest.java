package sootup.java.bytecode.frontend;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import categories.Java8Test;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;

@Category(Java8Test.class)
public class AsmUtilTest {

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
    ClassType classType = AsmUtil.toJimpleClassType("Ljava/lang/Object;");
    assertEquals("java.lang.Object", classType.getFullyQualifiedName());
    ClassType classType2 = AsmUtil.toJimpleClassType("java/lang/Object");
    assertEquals("java.lang.Object", classType2.getFullyQualifiedName());
    ClassType classType3 = AsmUtil.toJimpleClassType("java.lang.Object");
    assertEquals("java.lang.Object", classType3.getFullyQualifiedName());
  }

  @Test
  public void testToJimpleTypePrimitives() {
    Type primitiveTypeZ = AsmUtil.toJimpleType("Z");
    assertTrue(primitiveTypeZ instanceof PrimitiveType.BooleanType);
    Type primitiveTypeB = AsmUtil.toJimpleType("B");
    assertTrue(primitiveTypeB instanceof PrimitiveType.ByteType);
    Type primitiveTypeC = AsmUtil.toJimpleType("C");
    assertTrue(primitiveTypeC instanceof PrimitiveType.CharType);
    Type primitiveTypeS = AsmUtil.toJimpleType("S");
    assertTrue(primitiveTypeS instanceof PrimitiveType.ShortType);
    Type primitiveTypeI = AsmUtil.toJimpleType("I");
    assertTrue(primitiveTypeI instanceof PrimitiveType.IntType);
    Type primitiveTypeF = AsmUtil.toJimpleType("F");
    assertTrue(primitiveTypeF instanceof PrimitiveType.FloatType);
    Type primitiveTypeJ = AsmUtil.toJimpleType("J");
    assertTrue(primitiveTypeJ instanceof PrimitiveType.LongType);
    Type primitiveTypeD = AsmUtil.toJimpleType("D");
    assertTrue(primitiveTypeD instanceof PrimitiveType.DoubleType);
    Type primitiveTypeV = AsmUtil.toJimpleType("V");
    assertTrue(primitiveTypeV instanceof VoidType);
  }

  @Test
  public void testToJimpleTypeClass() {
    Type classType = AsmUtil.toJimpleType("Ljava/lang/Object;");
    assertTrue(classType instanceof ClassType);
    assertEquals("java.lang.Object", ((ClassType) classType).getFullyQualifiedName());
  }

  @Test
  public void testToJimpleTypeArrays() {
    Type primitiveTypeZ = AsmUtil.toJimpleType("[Z");
    assertTrue(primitiveTypeZ instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeZ).getBaseType() instanceof PrimitiveType.BooleanType);
    assertEquals(((ArrayType) primitiveTypeZ).getDimension(), 1);

    Type primitiveTypeB = AsmUtil.toJimpleType("[[B");
    assertTrue(primitiveTypeB instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeB).getBaseType() instanceof PrimitiveType.ByteType);
    assertEquals(((ArrayType) primitiveTypeB).getDimension(), 2);

    Type primitiveTypeC = AsmUtil.toJimpleType("[[[C");
    assertTrue(primitiveTypeC instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeC).getBaseType() instanceof PrimitiveType.CharType);
    assertEquals(((ArrayType) primitiveTypeC).getDimension(), 3);

    Type primitiveTypeS = AsmUtil.toJimpleType("[[[[S");
    assertTrue(primitiveTypeS instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeS).getBaseType() instanceof PrimitiveType.ShortType);
    assertEquals(((ArrayType) primitiveTypeS).getDimension(), 4);

    Type primitiveTypeI = AsmUtil.toJimpleType("[[[[[I");
    assertTrue(primitiveTypeI instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeI).getBaseType() instanceof PrimitiveType.IntType);
    assertEquals(((ArrayType) primitiveTypeI).getDimension(), 5);

    Type primitiveTypeF = AsmUtil.toJimpleType("[[[[F");
    assertTrue(primitiveTypeF instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeF).getBaseType() instanceof PrimitiveType.FloatType);
    assertEquals(((ArrayType) primitiveTypeF).getDimension(), 4);

    Type primitiveTypeJ = AsmUtil.toJimpleType("[[[J");
    assertTrue(primitiveTypeJ instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeJ).getBaseType() instanceof PrimitiveType.LongType);
    assertEquals(((ArrayType) primitiveTypeJ).getDimension(), 3);

    Type primitiveTypeD = AsmUtil.toJimpleType("[[D");
    assertTrue(primitiveTypeD instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeD).getBaseType() instanceof PrimitiveType.DoubleType);
    assertEquals(((ArrayType) primitiveTypeD).getDimension(), 2);

    Type classType = AsmUtil.toJimpleType("[Ljava.lang.Object;");
    assertTrue(classType instanceof ArrayType);
    assertTrue(((ArrayType) classType).getBaseType() instanceof ClassType);
    assertEquals(((ArrayType) classType).getDimension(), 1);
  }

  @Test(expected = AssertionError.class)
  public void testToJimpleTypeUnknownDescriptor() {
    AsmUtil.toJimpleType("P");
  }

  @Test(expected = AssertionError.class)
  public void testToJimpleTypeInvalidRefDescriptor() {
    AsmUtil.toJimpleType("L");
  }

  @Test(expected = AssertionError.class)
  public void testToJimpleTypeInvalidPrimitiveDescriptor() {
    AsmUtil.toJimpleType("II");
  }

  @Test(expected = AssertionError.class)
  public void testToJimpleTypeInvalidVoidDescriptor() {
    AsmUtil.toJimpleType("VI");
  }

  @Test(expected = AssertionError.class)
  public void testToJimpleTypeIncorrectArray() {
    AsmUtil.toJimpleType("[I[I");
  }

  @Test
  public void testArrayTypeToJimpleType() {
    Type primitiveTypeZ = AsmUtil.arrayTypetoJimpleType("Z");
    assertTrue(primitiveTypeZ instanceof ClassType);
    Type primitiveTypeB = AsmUtil.arrayTypetoJimpleType("B");
    assertTrue(primitiveTypeB instanceof ClassType);
    Type primitiveTypeC = AsmUtil.arrayTypetoJimpleType("C");
    assertTrue(primitiveTypeC instanceof ClassType);
    Type primitiveTypeS = AsmUtil.arrayTypetoJimpleType("S");
    assertTrue(primitiveTypeS instanceof ClassType);
    Type primitiveTypeI = AsmUtil.arrayTypetoJimpleType("I");
    assertTrue(primitiveTypeI instanceof ClassType);
    Type primitiveTypeF = AsmUtil.arrayTypetoJimpleType("F");
    assertTrue(primitiveTypeF instanceof ClassType);
    Type primitiveTypeJ = AsmUtil.arrayTypetoJimpleType("J");
    assertTrue(primitiveTypeJ instanceof ClassType);
    Type primitiveTypeD = AsmUtil.arrayTypetoJimpleType("D");
    assertTrue(primitiveTypeD instanceof ClassType);
    Type primitiveTypeV = AsmUtil.arrayTypetoJimpleType("V");
    assertTrue(primitiveTypeV instanceof ClassType);

    Type classType = AsmUtil.arrayTypetoJimpleType("java/lang/Object");
    assertTrue(classType instanceof ClassType);
    assertEquals("java.lang.Object", ((ClassType) classType).getFullyQualifiedName());

    Type primitiveTypeZArray = AsmUtil.arrayTypetoJimpleType("[Z");
    assertTrue(primitiveTypeZArray instanceof ArrayType);
    assertTrue(
        ((ArrayType) primitiveTypeZArray).getBaseType() instanceof PrimitiveType.BooleanType);
    assertEquals(((ArrayType) primitiveTypeZArray).getDimension(), 1);

    Type primitiveTypeBArray = AsmUtil.arrayTypetoJimpleType("[[B");
    assertTrue(primitiveTypeBArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeBArray).getBaseType() instanceof PrimitiveType.ByteType);
    assertEquals(((ArrayType) primitiveTypeBArray).getDimension(), 2);

    Type primitiveTypeCArray = AsmUtil.arrayTypetoJimpleType("[[[C");
    assertTrue(primitiveTypeCArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeCArray).getBaseType() instanceof PrimitiveType.CharType);
    assertEquals(((ArrayType) primitiveTypeCArray).getDimension(), 3);

    Type primitiveTypeSArray = AsmUtil.arrayTypetoJimpleType("[[[[S");
    assertTrue(primitiveTypeSArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeSArray).getBaseType() instanceof PrimitiveType.ShortType);
    assertEquals(((ArrayType) primitiveTypeSArray).getDimension(), 4);

    Type primitiveTypeIArray = AsmUtil.arrayTypetoJimpleType("[[[[[I");
    assertTrue(primitiveTypeIArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeIArray).getBaseType() instanceof PrimitiveType.IntType);
    assertEquals(((ArrayType) primitiveTypeIArray).getDimension(), 5);

    Type primitiveTypeFArray = AsmUtil.arrayTypetoJimpleType("[[[[F");
    assertTrue(primitiveTypeFArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeFArray).getBaseType() instanceof PrimitiveType.FloatType);
    assertEquals(((ArrayType) primitiveTypeFArray).getDimension(), 4);

    Type primitiveTypeJArray = AsmUtil.arrayTypetoJimpleType("[[[J");
    assertTrue(primitiveTypeJArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeJArray).getBaseType() instanceof PrimitiveType.LongType);
    assertEquals(((ArrayType) primitiveTypeJArray).getDimension(), 3);

    Type primitiveTypeDArray = AsmUtil.arrayTypetoJimpleType("[[D");
    assertTrue(primitiveTypeDArray instanceof ArrayType);
    assertTrue(((ArrayType) primitiveTypeDArray).getBaseType() instanceof PrimitiveType.DoubleType);
    assertEquals(((ArrayType) primitiveTypeDArray).getDimension(), 2);

    Type classTypeArray = AsmUtil.arrayTypetoJimpleType("[Ljava.lang.Object;");
    assertTrue(classTypeArray instanceof ArrayType);
    assertTrue(((ArrayType) classTypeArray).getBaseType() instanceof ClassType);
    assertEquals(((ArrayType) classTypeArray).getDimension(), 1);
  }
}
