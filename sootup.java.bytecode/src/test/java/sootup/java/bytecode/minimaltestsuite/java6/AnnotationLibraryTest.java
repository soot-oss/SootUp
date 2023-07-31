package sootup.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.util.printer.JimplePrinter;
import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnnotationLibraryTest extends MinimalBytecodeTestSuiteBase {

  // TODO: [bh] annotation methods lose default values

  @Test
  public void testAnnotationDeclaration() {
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    JimplePrinter p = new JimplePrinter(JimplePrinter.Option.LegacyMode);
    StringWriter out = new StringWriter();
    p.printTo(sootClass, new PrintWriter(out));
    assertTrue(ClassModifier.isAnnotation(sootClass.getModifiers()));
  }

  // TODO: [ms] add test for more annotation declarations e.g. inheritance

}
