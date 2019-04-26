package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
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

/** @author Linghui Luo */
@Category(Java8Test.class)
public class InvokeTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private DefaultTypeFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    DefaultFactories factories = DefaultFactories.create();
    sigFactory = factories.getSignatureFactory();
    typeFactory = factories.getTypeFactory();
  }

  @Test
  public void testInvokeSpecialInstanceInit() {
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokeInstanceInit", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokePrivateMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokeSupperClassMethod",
                declareClassSig,
                "java.lang.String",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "<clinit>", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "repro",
                declareClassSig,
                "void",
                Arrays.asList("int", "java.lang.String", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "repro1", declareClassSig, "void", Arrays.asList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "repro2", declareClassSig, "void", Arrays.asList("java.lang.Object")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "$r0 := @parameter0: java.lang.Object",
                "$r1 = \"\"",
                "$r2 = virtualinvoke $r1.<java.lang.Object: java.lang.String toString()>()",
                "$r3 = \"A\"",
                "$r4 = \"B\"",
                "$z0 = $r3 == $r4",
                "if $z0 == 0 goto $z1 = 5 < 3",
                "return",
                "$z1 = 5 < 3",
                "if $z1 == 0 goto $z2 = 5.0 < 3.0",
                "return",
                "$z2 = 5.0 < 3.0",
                "if $z2 == 0 goto return",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeVirtual1() {
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "equals", declareClassSig, "boolean", Collections.singletonList("InvokeVirtual")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeVirtual",
                "$r1 := @parameter0: InvokeVirtual",
                "$r2 = r0.<InvokeVirtual: java.lang.String x>",
                "$r3 = r0.<InvokeVirtual: java.lang.String x>",
                "$z0 = virtualinvoke $r2.<java.lang.String: boolean equals(java.lang.Object)>($r3)",
                "return $z0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void testInvokeVirtual2() {
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "interfaceMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

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
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "doStuf", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getActiveBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(IStmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InvokeVirtual",
                "virtualinvoke r0.<InvokeVirtual: void interfaceMethod()>()",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
