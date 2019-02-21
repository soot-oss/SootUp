package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.java.JimpleConverter;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import soot.G;
import soot.Scene;
import soot.options.Options;

/**
 * 
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class JimpleConverterTest {

  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;
  private SootClass klass;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();

  }

  @After
  public void convertJimple() {
    // set up soot options
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().set_print_tags_in_output(true);
    Scene.v().loadDynamicClasses();
    // load basic classes from soot
    Scene.v().loadBasicClasses();
    JimpleConverter jimpleConverter = new JimpleConverter(Collections.singletonList(klass));
    soot.SootClass c = jimpleConverter.convertSootClass(klass);
    PrintWriter writer = new PrintWriter(System.out);
    soot.Printer.v().printTo(c, writer);
    // writer.flush();
    // writer.close();
  }

  @Test
  public void testSimple1() {
    declareClassSig = sigFactory.getClassSignature("Simple1");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    klass = c.get();
  }

  @Test
  public void testVarDeclInSwitch() {
    declareClassSig = sigFactory.getClassSignature("bugfixes.VarDeclInSwitch");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    klass = c.get();
    // Utils.print(klass, true);
  }

}