package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.util.printer.Printer;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 * 
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class SelectedInstructionConverstionTest {

  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
  }

  @Ignore
  public void test1() {
    // TODO FIX IT
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA.AA");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("makeAB", declareClassSig,
            "alreadywalaunittests.InnerClassAA.AB", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test2() {
    declareClassSig = sigFactory.getClassSignature("AnonymousClass");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("method", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test3() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("doAllThis", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test4() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test5() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("<init>", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void test6() {
    declareClassSig = sigFactory.getClassSignature("foo.bar.hello.world.ArraysAndSuch");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void testThrowInstruction() {
    declareClassSig = sigFactory.getClassSignature("FooEx1");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("bar", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void testSwitchInstruction() {
    declareClassSig = sigFactory.getClassSignature("bugfixes.DoWhileInCase");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void testLoadMetadataInstruction() {
    declareClassSig = sigFactory.getClassSignature("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("valueOf", declareClassSig,
            "javaonepointfive.EnumSwitch$Palo", Arrays.asList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }

  @Test
  public void testCheckCastInstruction() {
    declareClassSig = sigFactory.getClassSignature("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("valueOf", declareClassSig,
        "javaonepointfive.EnumSwitch$Palo", Arrays.asList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    PrintWriter writer = new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(method.getActiveBody(), writer);
    writer.flush();
    writer.close();
  }
}
