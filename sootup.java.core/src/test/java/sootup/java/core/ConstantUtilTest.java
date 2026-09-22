package sootup.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;

class ConstantUtilTest {

  /**
   * Reproduces failure when converting Byte and Character boxed literals to Jimple Constants.
   *
   * <p>Subject pattern: Annotations with byte or char attributes, or constant pool entries
   * involving byte/char values: @MyAnnotation(byteVal = (byte) 42, charVal = 'Z')
   *
   * <p>Previously, ConstantUtil threw IllegalArgumentException("cannot convert Object to
   * (Soot-)Constant.") when given a Byte or Character instance.
   *
   * <p>In JVM bytecode (JVMS §2.11.1), byte and char are computational category 1 types represented
   * as IntConstant.
   */
  @Test
  void testFromObjectSupportsByteAndCharacter() {
    Constant byteConst = ConstantUtil.fromObject((byte) 42);
    IntConstant intConst1 = assertInstanceOf(IntConstant.class, byteConst);
    assertEquals(42, intConst1.getValue());

    Constant charConst = ConstantUtil.fromObject('Z');
    IntConstant intConst2 = assertInstanceOf(IntConstant.class, charConst);
    assertEquals((int) 'Z', intConst2.getValue());
  }

  @Test
  void testFromObjectNull() {
    assertInstanceOf(NullConstant.class, ConstantUtil.fromObject(null));
  }

  @Test
  void testFromObjectUnsupportedThrowsWithClassName() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> ConstantUtil.fromObject(new Object()));
    assertTrue(ex.getMessage().contains("java.lang.Object"));
  }
}
