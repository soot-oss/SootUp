package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class AnnotationLibraryTest extends MinimalBytecodeTestSuiteBase {

  // TODO: [bh] annotation methods lose default values

  @Test
  public void testAnnotationDeclaration() {
    SootClass sootClass = loadClass(getDeclaredClassSignature());
    Printer p = new Printer(Printer.Option.LegacyMode);
    StringWriter out = new StringWriter();
    p.printTo(sootClass, new PrintWriter(out));
    System.out.println(out.toString());
    assertTrue(Modifier.isAnnotation(sootClass.getModifiers()));
  }

  // TODO: [ms] add test for more annotation declarations e.g. inheritance

}
