package de.upb.soot.frontends;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;

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
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("makeAB", declareClassSig, "alreadywalaunittests.InnerClassAA.AB", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test2() {
    declareClassSig = sigFactory.getClassSignature("AnonymousClass");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("method", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test3() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("doAllThis", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test4() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test5() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("<init>", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test6() {
    declareClassSig = sigFactory.getClassSignature("foo.bar.hello.world.ArraysAndSuch");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testThrowInstruction() {
    declareClassSig = sigFactory.getClassSignature("FooEx1");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("bar", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testSwitchInstruction() {
    declareClassSig = sigFactory.getClassSignature("bugfixes.DoWhileInCase");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testLoadMetadataInstruction() {
    declareClassSig = sigFactory.getClassSignature("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("valueOf", declareClassSig,
        "javaonepointfive.EnumSwitch$Palo", Arrays.asList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testCheckCastInstruction() {
    declareClassSig = sigFactory.getClassSignature("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("valueOf", declareClassSig,
        "javaonepointfive.EnumSwitch$Palo", Arrays.asList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReference() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("doSomeCrazyStuff", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReferenceWithFieldCreation() {
    declareClassSig = sigFactory.getClassSignature("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalRead() {
    declareClassSig = sigFactory.getClassSignature("AnonymousClass$1");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("getValueBase", declareClassSig, "int", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadFromField() {
    declareClassSig = sigFactory.getClassSignature("Scoping2");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadWithFieldCreation() {
    declareClassSig = sigFactory.getClassSignature("AnonymousClass$1");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalWrite() {
    declareClassSig = sigFactory.getClassSignature("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("anonymousCoward", declareClassSig, "java.lang.Object", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteToField() {
    declareClassSig = sigFactory.getClassSignature("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("supportLocalBusiness", declareClassSig, "java.lang.Object", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteWithFieldCreation() {
    declareClassSig = sigFactory.getClassSignature("foo.bar.hello.world.InnerClasses");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstAssertInstruction() {
    declareClassSig = sigFactory.getClassSignature("MiniaturSliceBug");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("validNonDispatchedCall", declareClassSig, "void", Arrays.asList("IntWrapper")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstAssertInstructionWithFieldCreation() {
    declareClassSig = sigFactory.getClassSignature("MiniaturSliceBug");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), true);
  }

  @Test
  public void testMonitorInstruction() {
    declareClassSig = sigFactory.getClassSignature("Monitor");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("incr", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testGetCaughtExceptionInstruction() {
    declareClassSig = sigFactory.getClassSignature("Exception1");
    Optional<SootMethod> m
        = loader.getSootMethod(
            sigFactory.getMethodSignature("main", declareClassSig, "void", Arrays.asList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testArrayInstructions() {
    declareClassSig = sigFactory.getClassSignature("Array1");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("foo", declareClassSig, "void", Arrays.asList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  public void testAstLexialReadWithMultipleAccesses() {
    // TODO
  }

  public void testAstLexialWriteWithMultipleAccesses() {
    // TODO
  }
}
