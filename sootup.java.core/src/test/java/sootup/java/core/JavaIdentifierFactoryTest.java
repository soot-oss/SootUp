package sootup.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.core.types.PrimitiveType;
import sootup.java.core.types.JavaClassType;

public class JavaIdentifierFactoryTest {
  private static final Map<String, Class<?>> RT_PRIMITIVE_TYPES =
      Stream.of(
              boolean.class,
              byte.class,
              short.class,
              char.class,
              int.class,
              long.class,
              float.class,
              double.class)
          .collect(Collectors.toMap(Class::getName, Function.identity()));

  private final JavaIdentifierFactory javaIdentifierFactory = new JavaIdentifierFactory();

  @Test
  void testPrimitiveTypes() {
    for (String rtPrimitiveType : RT_PRIMITIVE_TYPES.keySet()) {
      Optional<PrimitiveType> jiPrimitiveTypeO =
          javaIdentifierFactory.getPrimitiveType(rtPrimitiveType);
      assertTrue(jiPrimitiveTypeO.isPresent(), "must resolve");
    }

    assertFalse(
        javaIdentifierFactory.getPrimitiveType(void.class.getName()).isPresent(),
        "must not resolve");

    Collection<PrimitiveType> allPrimitiveTypes = javaIdentifierFactory.getAllPrimitiveTypes();
    assertThrows(
        UnsupportedOperationException.class,
        () -> allPrimitiveTypes.addAll(allPrimitiveTypes),
        "must not be modifiable");
    assertEquals(RT_PRIMITIVE_TYPES.size(), allPrimitiveTypes.size());
  }

  @Test
  void testBoxedType() {

    for (PrimitiveType jiPrimitiveType : javaIdentifierFactory.getAllPrimitiveTypes()) {
      JavaClassType jiBoxedType = javaIdentifierFactory.getBoxedType(jiPrimitiveType);
      Class<?> rtPrimitiveType = RT_PRIMITIVE_TYPES.get(jiPrimitiveType.getName());
      Class<?> rtBoxedType = MethodType.methodType(rtPrimitiveType).wrap().returnType();
      assertEquals(
          rtBoxedType.getName(), jiBoxedType.getFullyQualifiedName(), "boxed names don't match");
    }
  }

  @Test
  void testBoxedTypeNull() {
    assertThrows(NullPointerException.class, () -> javaIdentifierFactory.getBoxedType(null));
  }

  @Test
  void testUnboxedType() {
    for (Class<?> rtPrimitiveType : RT_PRIMITIVE_TYPES.values()) {
      Class<?> rtBoxedType = MethodType.methodType(rtPrimitiveType).wrap().returnType();
      JavaClassType jiBoxedType = javaIdentifierFactory.getClassType(rtBoxedType.getName());
      Optional<PrimitiveType> jiUnboxedTypeO = javaIdentifierFactory.getUnboxedType(jiBoxedType);
      assertTrue(jiUnboxedTypeO.isPresent());
      PrimitiveType jiExpectedPrimitiveType =
          javaIdentifierFactory.getPrimitiveType(rtPrimitiveType.getTypeName()).get();
      assertEquals(jiExpectedPrimitiveType, jiUnboxedTypeO.get());
    }

    JavaClassType jiVoidReferenceType = javaIdentifierFactory.getClassType("java.lang.Void");
    Optional<PrimitiveType> jiUnboxedTypeO =
        javaIdentifierFactory.getUnboxedType(jiVoidReferenceType);
    assertFalse(jiUnboxedTypeO.isPresent());
  }

  @Test
  void testUnboxedTypeNull() {
    assertThrows(NullPointerException.class, () -> javaIdentifierFactory.getUnboxedType(null));
  }

  @Test
  void testBooleanPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getBooleanPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testBytePrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getBytePrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testShortPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getShortPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testCharPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getCharPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testIntPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getIntPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testLongPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getLongPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testFloatPrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getFloatPrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }

  @Test
  void testDoublePrimitiveType() {
    PrimitiveType jiPrimitiveType1 = javaIdentifierFactory.getDoublePrimitiveType();
    assertNotNull(jiPrimitiveType1);
    Optional<PrimitiveType> jiPrimitiveType2O =
        javaIdentifierFactory.getPrimitiveType(jiPrimitiveType1.getName());
    assertTrue(jiPrimitiveType2O.isPresent());
    assertEquals(jiPrimitiveType1, jiPrimitiveType2O.get());
  }
}
