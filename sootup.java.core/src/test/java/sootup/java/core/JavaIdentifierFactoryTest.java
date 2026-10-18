package sootup.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

  @Test
  void testPrimitiveTypes() {
    JavaIdentifierFactory javaIdentifierFactory = new JavaIdentifierFactory();
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
  void testBoxedType() throws ClassNotFoundException {
    JavaIdentifierFactory javaIdentifierFactory = new JavaIdentifierFactory();

    for (PrimitiveType jiPrimitiveType : javaIdentifierFactory.getAllPrimitiveTypes()) {
      JavaClassType jiBoxedType = javaIdentifierFactory.getBoxedType(jiPrimitiveType);
      Class<?> rtPrimitiveType = RT_PRIMITIVE_TYPES.get(jiPrimitiveType.getName());
      Class<?> rtBoxedType = MethodType.methodType(rtPrimitiveType).wrap().returnType();
      assertEquals(
          rtBoxedType.getName(), jiBoxedType.getFullyQualifiedName(), "boxed names don't match");
    }
  }
}
