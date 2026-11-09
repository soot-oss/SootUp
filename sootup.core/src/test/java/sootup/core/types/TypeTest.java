package sootup.core.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TypeTest {

  @Test
  public void testValueBitSize() {
    assertEquals(1, Type.getValueBitSize(PrimitiveType.getBoolean()));
    assertEquals(8, Type.getValueBitSize(PrimitiveType.getByte()));
    assertEquals(16, Type.getValueBitSize(PrimitiveType.getShort()));
    assertEquals(16, Type.getValueBitSize(PrimitiveType.getChar()));
    assertEquals(32, Type.getValueBitSize(PrimitiveType.getInt()));
    assertEquals(32, Type.getValueBitSize(PrimitiveType.getFloat()));
    assertEquals(64, Type.getValueBitSize(PrimitiveType.getLong()));
    assertEquals(64, Type.getValueBitSize(PrimitiveType.getDouble()));
    assertEquals(0, Type.getValueBitSize(NullType.getInstance()));
    assertEquals(0, Type.getValueBitSize(VoidType.getInstance()));
    assertEquals(0, Type.getValueBitSize(UnknownType.getInstance()));
  }
}
