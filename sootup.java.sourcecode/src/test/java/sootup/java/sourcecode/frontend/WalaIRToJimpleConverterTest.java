package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class WalaIRToJimpleConverterTest {
  private final WalaIRToJimpleConverter converter =
      new WalaIRToJimpleConverter(Collections.emptySet());

  @Test
  public void testConvertClassName1() {
    String walaName = "Ljava/lang/String";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("java.lang.String", name);
  }

  @Test
  public void testConvertClassName2() {
    String walaName =
        "LJLex/SparseBitSet/elements()Ljava/util/Enumeration;/<anonymous subclass of java.lang.Object>$4";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("JLex.SparseBitSet$4", name);
  }

  @Test
  public void testConvertClassName3() {
    String walaName = "LJLex/SparseBitSet/<init>/<anonymous subclass of java.lang.Object>$1";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("JLex.SparseBitSet$1", name);
  }

  @Test
  public void testConvertClassName4() {
    String walaName = "LLocalClass/main([Ljava/lang/String;)V/Foo";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("LocalClass1$Foo", name);
    walaName = "LLocalClass/methodRef()V/Foo";
    name = converter.convertClassNameFromWala(walaName);
    assertEquals("LocalClass2$Foo", name);
  }

  @Test
  public void testConvertClassName5() {
    String walaName =
        "LScoping2/main([Ljava/lang/String;)V/<anonymous subclass of java.lang.Object>$1";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("Scoping2$1", name);
  }

  @Test
  public void testConvertClassName6() {
    String walaName =
        "Ljavaonepointfive/NotSoSimpleEnums$Direction/<init>/<anonymous subclass of javaonepointfive.NotSoSimpleEnums$Direction>$1";
    String name = converter.convertClassNameFromWala(walaName);
    assertEquals("javaonepointfive.NotSoSimpleEnums$Direction$1", name);
  }

  @Test
  public void testConvertClassName7() {
    String sootName = "javaonepointfive.AnonGeneNullarySimple$Ops";
    String name = converter.convertClassNameFromSoot(sootName);
  }
}
