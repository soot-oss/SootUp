package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.WalaClassLoaderTestUtils;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class SelectedInstructionConversionTest {

  private WalaJavaClassProvider loader;

  private JavaIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/wala-tests/";
    loader = new WalaJavaClassProvider(srcDir);
    identifierFactory = JavaIdentifierFactory.getInstance();
  }

  @Test
  @Ignore
  public void test1() {
    // TODO FIX IT
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA.AA");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "makeAB",
                "alreadywalaunittests.InnerClassAA.AB",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void test2() {
    declareClassSig = identifierFactory.getClassType("AnonymousClass");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "method", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: AnonymousClass",
                "$r1 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(7)",
                "$r2 = new AnonymousClass$2",
                "$i0 = 0 - 4",
                "specialinvoke $r2.<AnonymousClass$2: void <init>(int)>($i0)",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "$i1 = interfaceinvoke $r2.<AnonymousClass$Foo: int getValue()>()",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i1)",
                "$r4 = <java.lang.System: java.io.PrintStream out>",
                "$i2 = interfaceinvoke $r2.<AnonymousClass$Foo: int getValueBase()>()",
                "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i2)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  // TODO This test should not be ignored
  //  https://github.com/secure-software-engineering/soot-reloaded/issues/108
  @Test
  @Ignore
  public void test3() {
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "doAllThis", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: alreadywalaunittests.InnerClassAA",
                "$r1 = new alreadywalaunittests.InnerClassAA$AA",
                "specialinvoke $r1.<alreadywalaunittests.InnerClassAA$AA: void <init>(alreadywalaunittests.InnerClassAA)>($r0)",
                "$r2 = new alreadywalaunittests.InnerClassAA$AA",
                "specialinvoke $r2.<alreadywalaunittests.InnerClassAA$AA: void <init>(alreadywalaunittests.InnerClassAA)>($r1)",
                "$r1 = $r2",
                "$r3 = virtualinvoke $r1.<alreadywalaunittests.InnerClassAA$AA: alreadywalaunittests.InnerClassAA$AB makeAB()>()",
                "$r0.<alreadywalaunittests.InnerClassAA: int a_x> = 5",
                "$i0 = virtualinvoke $r3.<alreadywalaunittests.InnerClassAA$AB: int getA_X_from_AB()>()",
                "$r4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i0)",
                "$i1 = virtualinvoke $r3.<alreadywalaunittests.InnerClassAA$AB: int getA_X_thru_AB()>()",
                "$r5 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r5.<java.io.PrintStream: void println(int)>($i1)",
                "virtualinvoke $r1.<alreadywalaunittests.InnerClassAA$AA: void doSomeCrazyStuff()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test4() {
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "main", "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "$r0 := @parameter0: java.lang.String[]",
                "$r1 = new alreadywalaunittests.InnerClassAA",
                "specialinvoke $r1.<alreadywalaunittests.InnerClassAA: void <init>()>()",
                "virtualinvoke $r1.<alreadywalaunittests.InnerClassAA: void doAllThis()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test5() {
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "<init>", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: alreadywalaunittests.InnerClassAA",
                "specialinvoke r0.<java.lang.Object: void <init>()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Ignore
  public void test6() {
    // TODO The Jimple here is probably incorrect, but complicated to test for.
    //   Likely issues:
    //     wait(long, int) is invoked with an int as its first argument
    //     Multi-dimensional array is not created properly

    declareClassSig = identifierFactory.getClassType("foo.bar.hello.world.ArraysAndSuch");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "main", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testThrowInstruction() {
    declareClassSig = identifierFactory.getClassType("FooEx1");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "bar", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: FooEx1",
                "$r1 = new BadLanguageExceptionEx1",
                "specialinvoke $r1.<BadLanguageExceptionEx1: void <init>()>()",
                "throw $r1")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testSwitchInstruction() {
    // TODO Conversion from switch is very broken (default-case is not compiled correctly),
    //      And the target of the loop condition is null.
    declareClassSig = identifierFactory.getClassType("bugfixes.DoWhileInCase");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "main", "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testLoadMetadataInstruction() {
    // TODO Is the cast wrong?
    declareClassSig = identifierFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "valueOf",
                "javaonepointfive.EnumSwitch$Palo",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testCheckCastInstruction() {
    declareClassSig = identifierFactory.getClassType("javaonepointfive.EnumSwitch$Palo");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "valueOf",
                "javaonepointfive.EnumSwitch$Palo",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReference() {
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "doSomeCrazyStuff", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testEnclosingObjectReferenceWithFieldCreation() {
    declareClassSig = identifierFactory.getClassType("alreadywalaunittests.InnerClassAA$AA");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    // TODO. replace the next line with assertions.
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalRead() {
    declareClassSig = identifierFactory.getClassType("AnonymousClass$1");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "getValueBase", "int", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadFromField() {
    declareClassSig = identifierFactory.getClassType("Scoping2");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "main", "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalReadWithFieldCreation() {
    declareClassSig = identifierFactory.getClassType("AnonymousClass$1");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    // TODO. replace the next line with assertions.
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstLexicalWrite() {
    declareClassSig = identifierFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "anonymousCoward", "java.lang.Object", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteToField() {
    declareClassSig = identifierFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "supportLocalBusiness",
                "java.lang.Object",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAstLexicalWriteWithFieldCreation() {
    declareClassSig = identifierFactory.getClassType("foo.bar.hello.world.InnerClasses");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    // TODO. replace the next line with assertions.
    Utils.print(m.get(), false);
  }

  @Test
  public void testAstAssertInstruction() {
    declareClassSig = identifierFactory.getClassType("MiniaturSliceBug");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "validNonDispatchedCall",
                "void",
                Collections.singletonList("IntWrapper")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testAstAssertInstructionWithFieldCreation() {
    declareClassSig = identifierFactory.getClassType("MiniaturSliceBug");
    Optional<SootClass> m = loader.getSootClass(declareClassSig);
    assertTrue(m.isPresent());
    // TODO. replace the next line with assertions.
    Utils.print(m.get(), false);
  }

  @Test
  public void testMonitorInstruction() {
    declareClassSig = identifierFactory.getClassType("Monitor");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "incr", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Ignore
  public void testGetCaughtExceptionInstruction() {
    declareClassSig = identifierFactory.getClassType("Exception1");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "main", "void", Collections.singletonList("java.lang.String[]")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void testArrayInstructions() {
    declareClassSig = identifierFactory.getClassType("Array1");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "foo", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  public void testAstLexialReadWithMultipleAccesses() {
    // TODO
  }

  public void testAstLexialWriteWithMultipleAccesses() {
    // TODO
  }
}
