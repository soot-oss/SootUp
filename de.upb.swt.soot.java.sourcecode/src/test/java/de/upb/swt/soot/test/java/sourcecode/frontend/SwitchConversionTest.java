package de.upb.swt.soot.test.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.util.Utils;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.WalaClassLoaderTestUtils;
import de.upb.swt.soot.java.sourcecode.frontend.WalaJavaClassProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SwitchConversionTest {
  private WalaJavaClassProvider loader;

  private JavaIdentifierFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaJavaClassProvider(srcDir);
    typeFactory = JavaIdentifierFactory.getInstance();
    declareClassSig =
        typeFactory.getClassType("de.upb.soot.concrete.controlStatements.ControlStatements");
  }

  /**
   *
   *
   * <pre>
   *     public void tableSwitch(int a) {
   *     switch (a) {
   *       case 1:
   *         System.out.println(a);
   *       case 2:
   *         System.out.println(a);
   *       default:
   *         System.out.println(a);
   *     }
   *   }
   * </pre>
   */
  @Test
  public void test1() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "tableSwitch", declareClassSig, "void", Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$i0 := @parameter0: int",
                "switch($i0)",
                "case 1: goto label2",
                "case 2: goto label3",
                "default: goto label1",
                "label1:",
                "goto label4",
                "label2:",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "label3:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i0)",
                "label4:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  /**
   *
   *
   * <pre>
   *     public void tableSwitchDefault() {
   *     int a = 3;
   *     int b = a - 1;
   *     switch (b) {
   *       case 1:
   *         System.out.println(a);
   *       case 2:
   *         System.out.println(a);
   *       default:
   *         System.out.println(a);
   *     }
   *   }
   * </pre>
   */
  @Test
  public void test2() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "tableSwitchDefault", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$i0 = 3",
                "$i1 = $i0 - 1",
                "switch($i1)",
                "case 1: goto label2",
                "case 2: goto label3",
                "default: goto label1",
                "label1:",
                "goto label4",
                "label2:",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "label3:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i0)",
                "label4:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  /**
   *
   *
   * <pre>
   *   public void tableSwitchNoDefault(int a) {
   *     switch (a) {
   *       case 1:
   *         System.out.println(a);
   *       case 2:
   *         System.out.println(a);
   *     }
   *
   *     System.out.println(a);
   *   }
   * </pre>
   */
  @Test
  public void test3() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "tableSwitchNoDefault", declareClassSig, "void", Collections.singletonList("int")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$i0 := @parameter0: int",
                "switch($i0)",
                "case 1: goto label2",
                "case 2: goto label3",
                "default: goto label1",
                "label1:",
                "goto label4",
                "label2:",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
                "label3:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(int)>($i0)",
                "label4:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i0)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  /**
   *
   *
   * <pre>
   *   public void lookupSwitch(String a) {
   *     switch (a) {
   *       case "foo":
   *         System.out.println(a);
   *       case "bar":
   *         System.out.println(a);
   *       default:
   *         System.out.println(a);
   *     }
   *   }
   * </pre>
   */
  @Test
  @Ignore("FIXME")
  public void test4() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "lookupSwitch",
                declareClassSig,
                typeFactory.getType("void"),
                Collections.singletonList(typeFactory.getClassType("java.lang.String"))));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    // FIXME: [bh] values of $i0 and $i1 are missing (foo and bar)
    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$r1 := @parameter0: java.lang.String",
                "if $r1 == $i0 goto label1",
                "if $r1 == $i1 goto label2",
                "goto label3",
                "label1:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label2:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label3:",
                "$r4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  /**
   * TODO: [bh] this is a duplicate to lookupSwitch?
   *
   * <pre>
   *   public void lookupSwitchDefault(String a) {
   *     switch (a) {
   *       case "foo":
   *         System.out.println(a);
   *       case "bar":
   *         System.out.println(a);
   *       default:
   *         System.out.println(a);
   *     }
   *   }
   * </pre>
   */
  @Test
  @Ignore("FIXME")
  public void test5() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "lookupSwitchDefault",
                declareClassSig,
                typeFactory.getType("void"),
                Collections.singletonList(typeFactory.getClassType("java.lang.String"))));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    // FIXME: [bh] values of $i0 and $i1 are missing (foo and bar)
    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$r1 := @parameter0: java.lang.String",
                "if $r1 == $i0 goto label1",
                "if $r1 == $i1 goto label2",
                "goto label3",
                "label1:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label2:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label3:",
                "$r4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  /**
   *
   *
   * <pre>
   *   public void lookupSwitchNoDefault(String a) {
   *     switch (a) {
   *       case "foo":
   *         System.out.println(a);
   *       case "bar":
   *         System.out.println(a);
   *     }
   *     System.out.println(a);
   *   }
   * </pre>
   */
  @Test
  public void test6() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            typeFactory.getMethodSignature(
                "lookupSwitchNoDefault",
                declareClassSig,
                typeFactory.getType("void"),
                Collections.singletonList(typeFactory.getClassType("java.lang.String"))));
    assertTrue(m.isPresent());
    SootMethod method = m.get();

    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts = Utils.bodyStmtsAsStrings(body);

    // FIXME: [bh] values of $i0 and $i1 are missing (foo and bar)
    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: de.upb.soot.concrete.controlStatements.ControlStatements",
                "$r1 := @parameter0: java.lang.String",
                "if $r1 == $i0 goto label1",
                "if $r1 == $i1 goto label2",
                "goto label3",
                "label1:",
                "$r2 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label2:",
                "$r3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "label3:",
                "$r4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>($r1)",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
