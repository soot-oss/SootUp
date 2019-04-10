package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
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
  private DefaultTypeFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    DefaultFactories factories = DefaultFactories.create();
    sigFactory = factories.getSignatureFactory();
    typeFactory = factories.getTypeFactory();
  }

  @Ignore
  public void test1() {
    // TODO FIX IT
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA.AA");
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
    declareClassSig = typeFactory.getClassType("AnonymousClass");
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
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA");
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
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA");
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
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA");
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
    declareClassSig = typeFactory.getClassType("foo.bar.hello.world.ArraysAndSuch");
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
    declareClassSig = typeFactory.getClassType("FooEx1");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature("bar", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testSwitchInstruction() {
    declareClassSig = typeFactory.getClassType("bugfixes.DoWhileInCase");
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
    declareClassSig = typeFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
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
    declareClassSig = typeFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
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
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
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
    declareClassSig = typeFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalRead() {
    declareClassSig = typeFactory.getClassType("AnonymousClass$1");
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
    declareClassSig = typeFactory.getClassType("Scoping2");
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
    declareClassSig = typeFactory.getClassType("AnonymousClass$1");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalWrite() {
    declareClassSig = typeFactory.getClassType("foo.bar.hello.world.InnerClasses");
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
    declareClassSig = typeFactory.getClassType("foo.bar.hello.world.InnerClasses");
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
    declareClassSig = typeFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstAssertInstruction() {
    declareClassSig = typeFactory.getClassType("MiniaturSliceBug");
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
    declareClassSig = typeFactory.getClassType("MiniaturSliceBug");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    Utils.print(m.get(), false);
  }

  @Test
  public void testMonitorInstruction() {
    declareClassSig = typeFactory.getClassType("Monitor");
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
    declareClassSig = typeFactory.getClassType("Exception1");
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
    declareClassSig = typeFactory.getClassType("Array1");
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
