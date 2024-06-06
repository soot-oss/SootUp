package sootup.core.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TypeTest {

  @Test
  public void testValueBitSize() {
    int charValueBitSize = Type.getValueBitSize(PrimitiveType.getChar());
    assertEquals(16, charValueBitSize);
  }
}
