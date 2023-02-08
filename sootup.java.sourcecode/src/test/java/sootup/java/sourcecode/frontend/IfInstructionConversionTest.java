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
public class IfInstructionConversionTest {

  private WalaJavaClassProvider loader;

  private JavaIdentifierFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaJavaClassProvider(srcDir);
    typeFactory = JavaIdentifierFactory.getInstance();
    declareClassSig =
        typeFactory.getClassType("de.upb.sootup.concrete.controlStatements.ControlStatements");
  }

  @Test
  public void test1() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig,
                "simpleIfElseIfTakeThen",
                "void",
                Arrays.asList("int", "int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.sootup.concrete.controlStatements.ControlStatements",
                "$i0 := @parameter0: int",
                "$i1 := @parameter1: int",
                "$i2 := @parameter2: int",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto label1",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "goto label3",
                "label1:",
                "$z1 = $i0 < $i2",
                "if $z1 == 0 goto label2",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i1)",
                "goto label3",
                "label2:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i2)",
                "label3:",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test2() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "simpleIfElse", "boolean", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.sootup.concrete.controlStatements.ControlStatements",
                "$i0 := @parameter0: int",
                "$i1 := @parameter1: int",
                "$z0 = $i0 == $i1",
                "if $z0 == 0 goto label1",
                "return 1",
                "label1:",
                "return 0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test3() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "simpleIfElse", "boolean", Arrays.asList("boolean", "boolean")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.sootup.concrete.controlStatements.ControlStatements",
                "$z0 := @parameter0: boolean",
                "$z1 := @parameter1: boolean",
                "$i0 = (int) $z0",
                "$i1 = (int) $z1",
                "$z2 = $i0 != $i1",
                "if $z2 == 0 goto label1",
                "return 1",
                "label1:",
                "return 0")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test4() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig,
                "simpleIf",
                "boolean",
                Collections.singletonList("java.lang.String")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.sootup.concrete.controlStatements.ControlStatements",
                "$r1 := @parameter0: java.lang.String",
                "$z0 = $r1 == null",
                "if $z0 == 0 goto label1",
                "return 0",
                "label1:",
                "return 1")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test5() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig,
                "simpleIfElseIfTakeElse",
                "void",
                Arrays.asList("double", "double", "double")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.sootup.concrete.controlStatements.ControlStatements",
                "$d0 := @parameter0: double",
                "$d1 := @parameter1: double",
                "$d2 := @parameter2: double",
                "$z0 = $d0 < $d1",
                "if $z0 == 0 goto label1",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(double)>($d0)",
                "goto label3",
                "label1:",
                "$z1 = $d0 < $d2",
                "if $z1 == 0 goto label2",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(double)>($d1)",
                "goto label3",
                "label2:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(double)>($d2)",
                "label3:",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void test6() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "simpleIf", "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void test7() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "simpleIfTrue", "int", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }

  @Test
  public void test8() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                declareClassSig, "simpleIfIntExpr", "int", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    // TODO. replace the next line with assertions.
    Utils.print(method, false);
  }
}
