package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.util.Utils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.WalaClassLoaderTestUtils;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class InvokeTest {
  private WalaJavaClassProvider loader;

  private JavaIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaJavaClassProvider(srcDir);
    identifierFactory = JavaIdentifierFactory.getInstance();
  }

  @Test
  public void testInvokeSpecialInstanceInit() {
    declareClassSig = identifierFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "specialInvokeInstanceInit", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeSpecial",
                "$r1 = new java.util.ArrayList",
                "specialinvoke $r1.<java.util.ArrayList: void <init>()>()",
                "$z0 = virtualinvoke $r1.<java.util.ArrayList: boolean add(java.lang.Object)>(\"item1\")",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeSpecialPrivateMethod() {
    declareClassSig = identifierFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "specialInvokePrivateMethod", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeSpecial",
                "$i0 = specialinvoke r0.<InvokeSpecial: int privateMethod()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeSpecialSuperClassMethod() {
    declareClassSig = identifierFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "specialInvokeSupperClassMethod",
                "java.lang.String",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeSpecial",
                "$r1 = specialinvoke r0.<java.lang.Object: java.lang.String toString()>()",
                "return $r1")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeStatic1() {
    declareClassSig = identifierFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "<clinit>", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "$r0 = new java.lang.String",
                "specialinvoke $r0.<java.lang.String: void <init>()>()",
                "<InvokeStatic: java.lang.String string> = $r0",
                "<InvokeStatic: java.lang.String x> = \"abc\"",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeStatic2() {
    declareClassSig = identifierFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig,
                "repro",
                "void",
                Arrays.asList("int", "java.lang.String", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "$i0 := @parameter0: int",
                "$r0 := @parameter1: java.lang.String",
                "$z0 := @parameter2: boolean",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeStatic3() {
    declareClassSig = identifierFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "repro1", "void", Collections.singletonList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "$r0 := @parameter0: java.lang.Object",
                "$r1 = $r0",
                "$r2 = virtualinvoke $r1.<java.lang.Object: java.lang.String toString()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeStatic4() {
    declareClassSig = identifierFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "repro2", "void", Collections.singletonList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "$r0 := @parameter0: java.lang.Object",
                "$r1 = \"\"",
                "$r2 = virtualinvoke $r1.<java.lang.Object: java.lang.String toString()>()",
                "$r3 = \"A\"",
                "$r4 = \"B\"",
                "$z0 = $r3 == $r4",
                "if $z0 == 0 goto label1",
                "return",
                "label1:",
                "$z1 = 5 < 3",
                "if $z1 == 0 goto label2",
                "return",
                "label2:",
                "$z2 = 5.0 < 3.0",
                "if $z2 == 0 goto label3",
                "return",
                "label3:",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeVirtual1() {
    declareClassSig = identifierFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "equals", "boolean", Collections.singletonList("InvokeVirtual")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);
    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeVirtual",
                "$r1 := @parameter0: InvokeVirtual",
                "$r2 = r0.<InvokeVirtual: java.lang.String x>",
                "$r3 = $r1.<InvokeVirtual: java.lang.String x>",
                "$z0 = virtualinvoke $r2.<java.lang.String: boolean equals(java.lang.Object)>($r3)",
                "return $z0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeVirtual2() {
    declareClassSig = identifierFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "interfaceMethod", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeVirtual",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"abc\")",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeVirtual3() {
    declareClassSig = identifierFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declareClassSig, "doStuf", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeVirtual",
                "virtualinvoke r0.<InvokeVirtual: void interfaceMethod()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
