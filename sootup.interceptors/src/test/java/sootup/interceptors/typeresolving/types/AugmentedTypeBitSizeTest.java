package sootup.interceptors.typeresolving.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import sootup.core.types.Type;

/** The type checker compares bit sizes of the augmented types, so they must be visitable. */
public class AugmentedTypeBitSizeTest {

  @Test
  public void testValueBitSize() {
    assertEquals(1, Type.getValueBitSize(AugmentIntegerTypes.getInteger1()));
    assertEquals(32, Type.getValueBitSize(AugmentIntegerTypes.getInteger127()));
    assertEquals(32, Type.getValueBitSize(AugmentIntegerTypes.getInteger32767()));
    assertEquals(0, Type.getValueBitSize(TopType.getInstance()));
    assertEquals(0, Type.getValueBitSize(BottomType.getInstance()));
  }
}
