package sootup.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import sootup.java.core.OverridingJavaClassSource.OverridingJavaClassSourceBuilder;

public class OverridingJavaClassSourceTest {

  @Test
  public void testUnsetOverridingJavaClassSource() {
    OverridingJavaClassSource ojcs= OverridingJavaClassSourceBuilder.builder().build();
    assertEquals(0,ojcs.resolveInterfaces().size());
    assertEquals(0,ojcs.resolveFields().size());
    assertEquals(0,ojcs.resolveModifiers().size());
    assertTrue(ojcs.resolveSuperclass().isEmpty());
    assertTrue(ojcs.resolveOuterClass().isEmpty());
    assertEquals(0,ojcs.resolveMethods().size());
    assertEquals(0,ojcs.resolveMethods().size());
  }

}
