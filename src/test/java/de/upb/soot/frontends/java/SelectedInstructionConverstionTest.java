package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassType;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class SelectedInstructionConverstionTest {

  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
  }

  @Ignore
  public void test1() {
    // TODO FIX IT
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA.AA");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "makeAB",
                declareClassSig,
                "alreadywalaunittests.InnerClassAA.AB",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test2() {
    declareClassSig = sigFactory.getClassType("AnonymousClass");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "method", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test3() {
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "doAllThis", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test4() {
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "main", declareClassSig, "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test5() {
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "<init>", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void test6() {
    declareClassSig = sigFactory.getClassType("foo.bar.hello.world.ArraysAndSuch");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "main", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testThrowInstruction() {
    declareClassSig = sigFactory.getClassType("FooEx1");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature("bar", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testSwitchInstruction() {
    declareClassSig = sigFactory.getClassType("bugfixes.DoWhileInCase");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "main", declareClassSig, "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testLoadMetadataInstruction() {
    declareClassSig = sigFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "valueOf",
                declareClassSig,
                "javaonepointfive.EnumSwitch$Palo",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testCheckCastInstruction() {
    declareClassSig = sigFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "valueOf",
                declareClassSig,
                "javaonepointfive.EnumSwitch$Palo",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReference() {
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "doSomeCrazyStuff", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReferenceWithFieldCreation() {
    declareClassSig = sigFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalRead() {
    declareClassSig = sigFactory.getClassType("AnonymousClass$1");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "getValueBase", declareClassSig, "int", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadFromField() {
    declareClassSig = sigFactory.getClassType("Scoping2");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "main", declareClassSig, "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadWithFieldCreation() {
    declareClassSig = sigFactory.getClassType("AnonymousClass$1");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalWrite() {
    declareClassSig = sigFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "anonymousCoward", declareClassSig, "java.lang.Object", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteToField() {
    declareClassSig = sigFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "supportLocalBusiness",
                declareClassSig,
                "java.lang.Object",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteWithFieldCreation() {
    declareClassSig = sigFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstAssertInstruction() {
    declareClassSig = sigFactory.getClassType("MiniaturSliceBug");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "validNonDispatchedCall",
                declareClassSig,
                "void",
                Collections.singletonList("IntWrapper")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testAstAssertInstructionWithFieldCreation() {
    declareClassSig = sigFactory.getClassType("MiniaturSliceBug");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testMonitorInstruction() {
    declareClassSig = sigFactory.getClassType("Monitor");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "incr", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testGetCaughtExceptionInstruction() {
    declareClassSig = sigFactory.getClassType("Exception1");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "main", declareClassSig, "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testArrayInstructions() {
    declareClassSig = sigFactory.getClassType("Array1");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature("foo", declareClassSig, "void", Collections.emptyList()));
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
