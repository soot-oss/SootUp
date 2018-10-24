package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.util.printer.Printer;

import java.io.PrintWriter;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class InstructionConverstionTest {

  private WalaClassLoader loader;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    String exclusionFilePath = srcDir + "WalaExclusions.txt";
    loader = new WalaClassLoader(srcDir, exclusionFilePath);
  }

  @Test
  public void test1() {
    Optional<SootClass> op = loader.getSootClass(new DefaultSignatureFactory().getClassSignature("Breaks"));
    assertTrue(op.isPresent());
    SootClass cl = op.get();

    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(cl, writer);

    writer.flush();
    writer.close();
  }
}
