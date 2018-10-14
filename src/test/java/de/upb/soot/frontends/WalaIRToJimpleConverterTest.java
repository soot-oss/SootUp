package de.upb.soot.frontends;

import static org.junit.Assert.assertEquals;

import de.upb.soot.frontends.java.WalaIRToJimpleConverter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class WalaIRToJimpleConverterTest {
  @Test
  public void testConvertClassName() {
    WalaIRToJimpleConverter converter = new WalaIRToJimpleConverter();
    String name = converter.convertClassName("Ljava/lang/String");
    assertEquals("java.lang.String", name);
  }
}
