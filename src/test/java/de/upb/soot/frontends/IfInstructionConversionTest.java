package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.util.printer.Printer;

import java.io.PrintWriter;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class IfInstructionConversionTest {
  private WalaClassLoader loader;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
  }

  @Test
  public void test1() {
    Optional<SootClass> op
        = loader.getSootClass(
            new DefaultSignatureFactory().getClassSignature("de.upb.soot.concrete.controlStatements.ControlStatements"));
    assertTrue(op.isPresent());
    SootClass cl = op.get();
    SootMethod method = cl.getMethodBySubSignature("void simpleIfElseIfTakeThen(int, int, int)");
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    
    printer.printTo(method.getActiveBody(), writer);

    writer.flush();
    writer.close();
  }
}
