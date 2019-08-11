package de.upb.soot.minimaltestsuite.java6;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.minimaltestsuite.LoadClassesWithWala;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class MethodAcceptingVarTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/";
  private String className = "MethodAcceptingVar";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void shortVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "shortVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("short")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$s0 := @parameter0: short",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($s0)",
                "$s1 = $s0",
                "$s2 = $s0 + 1",
                "$s0 = $s2",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($s0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void byteVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "byteVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("byte")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$b0 := @parameter0: byte",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($b0)",
                "$b1 = $b0",
                "$b2 = $b0 + 1",
                "$b0 = $b2",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($b0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void charVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "charVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("char")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$c0 := @parameter0: char",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(char)>($c0)",
                "$c0 = 98",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(char)>($c0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void intVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "intVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$i0 := @parameter0: int",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "$i1 = $i0",
                "$i2 = $i0 + 1",
                "$i0 = $i2",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void longVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "longVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("long")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$l0 := @parameter0: long",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(long)>($l0)",
                "$l0 = 123456777",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(long)>($l0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void floatVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "floatVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("float")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$f0 := @parameter0: float",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(float)>($f0)",
                "$f0 = 7.77F",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(float)>($f0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void doubleVariableTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "doubleVariable",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.singletonList("double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$d0 := @parameter0: double",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(double)>($d0)",
                "$d0 = 1.787777777",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(double)>($d0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
