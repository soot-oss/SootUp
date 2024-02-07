package sootup.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class LocalSplitterTest {
  JavaView view;
  LocalSplitter localSplitter = new LocalSplitter();

  @Before
  public void setup() {
    String classPath = "src/test/java/resources/interceptors";
    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(classPath);
    view = new JavaView(inputLocation);
  }

  private Body getBody(String methodName) {
    ClassType type = new JavaClassType("LocalSplitterTarget", PackageName.DEFAULT_PACKAGE);
    SootMethod sootMethod =
        view.getClass(type).get().getMethods().stream()
            .filter(method -> method.getName().equals(methodName))
            .findFirst()
            .get();
    return sootMethod.getBody();
  }

  void assertLocals(Set<String> localNames, Body.BodyBuilder builder) {
    assertEquals(
        localNames, builder.getLocals().stream().map(Local::getName).collect(Collectors.toSet()));
  }

  @Test
  public void testSimpleAssignment() {
    Body.BodyBuilder builder = Body.builder(getBody("simpleAssignment"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "l2#0 = 1;\n"
            + "l1#1 = l2#0 + 1;\n"
            + "l2#1 = l1#1 + 1;\n"
            + "\n"
            + "return;";

    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testSelfAssignment() {
    Body.BodyBuilder builder = Body.builder(getBody("selfAssignment"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "l2#0 = 1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "l2#1 = l2#0 + 1;\n"
            + "\n"
            + "return;";

    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testBranch() {
    Body.BodyBuilder builder = Body.builder(getBody("branch"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "\n"
            + "if l1#0 >= 0 goto label1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "\n"
            + "goto label2;\n"
            + "\n"
            + "label1:\n"
            + "l1#2 = l1#0 - 1;\n"
            + "l1#1 = l1#2 + 2;\n"
            + "\n"
            + "label2:\n"
            + "return l1#1;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testBranchMoreLocals() {
    Body.BodyBuilder builder = Body.builder(getBody("branchMoreLocals"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");
    expectedLocals.add("l1#5");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "\n"
            + "if l1#0 >= 0 goto label1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "l1#2 = l1#1 + 2;\n"
            + "l1#3 = l1#2 + 3;\n"
            + "\n"
            + "goto label2;\n"
            + "\n"
            + "label1:\n"
            + "l1#4 = l1#0 - 1;\n"
            + "l1#5 = l1#4 - 2;\n"
            + "l1#3 = l1#5 - 3;\n"
            + "\n"
            + "label2:\n"
            + "return l1#3;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testBranchMoreBranches() {
    Body.BodyBuilder builder = Body.builder(getBody("branchMoreBranches"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");
    expectedLocals.add("l1#5");
    expectedLocals.add("l1#6");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "\n"
            + "if l1#0 >= 0 goto label1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "l1#2 = l1#1 + 2;\n"
            + "\n"
            + "goto label2;\n"
            + "\n"
            + "label1:\n"
            + "l1#3 = l1#0 - 1;\n"
            + "l1#2 = l1#3 - 2;\n"
            + "\n"
            + "label2:\n"
            + "if l1#2 <= 1 goto label3;\n"
            + "l1#4 = l1#2 + 3;\n"
            + "l1#5 = l1#4 + 5;\n"
            + "\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "l1#6 = l1#2 - 3;\n"
            + "l1#5 = l1#6 - 5;\n"
            + "\n"
            + "label4:\n"
            + "return l1#5;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testBranchElseIf() {
    Body.BodyBuilder builder = Body.builder(getBody("branchElseIf"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "\n"
            + "if l1#0 >= 0 goto label1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "l1#2 = l1#1 + 2;\n"
            + "\n"
            + "goto label3;\n"
            + "\n"
            + "label1:\n"
            + "if l1#0 >= 5 goto label2;\n"
            + "l1#3 = l1#0 - 1;\n"
            + "l1#2 = l1#3 - 2;\n"
            + "\n"
            + "goto label3;\n"
            + "\n"
            + "label2:\n"
            + "l1#4 = l1#0 * 1;\n"
            + "l1#2 = l1#4 * 2;\n"
            + "\n"
            + "label3:\n"
            + "return l1#2;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testForLoop() {
    Body.BodyBuilder builder = Body.builder(getBody("forLoop"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "l1 = 0;\n"
            + "l2#0 = 0;\n"
            + "\n"
            + "label1:\n"
            + "if l2#0 >= 10 goto label2;\n"
            + "l2#1 = l2#0 + 1;\n"
            + "l1 = l1 + 1;\n"
            + "l2#0 = l2#1 + 1;\n"
            + "\n"
            + "goto label1;\n"
            + "\n"
            + "label2:\n"
            + "return l1;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }

  @Test
  public void testReusedLocals() {
    Body.BodyBuilder builder = Body.builder(getBody("reusedLocals"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("l0");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");
    expectedLocals.add("$stack3");
    expectedLocals.add("$stack4");
    expectedLocals.add("$stack5");
    expectedLocals.add("$stack6");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "l0 := @this: LocalSplitterTarget;\n"
            + "$stack3 = staticinvoke <java.lang.Math: double random()>();\n"
            + "$stack4 = $stack3 cmpl 0.0;\n"
            + "\n"
            + "if $stack4 != 0 goto label1;\n"
            + "l2#0 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(1);\n"
            + "l1#0 = l2#0;\n"
            + "\n"
            + "goto label2;\n"
            + "\n"
            + "label1:\n"
            + "l2#1 = \"\";\n"
            + "l1#0 = l2#1;\n"
            + "\n"
            + "label2:\n"
            + "$stack5 = <java.lang.System: java.io.PrintStream out>;\n"
            + "virtualinvoke $stack5.<java.io.PrintStream: void println(java.lang.Object)>(l1#0);\n"
            + "l1#1 = null;\n"
            + "$stack6 = <java.lang.System: java.io.PrintStream out>;\n"
            + "virtualinvoke $stack6.<java.io.PrintStream: void println(java.lang.Object)>(l1#1);\n"
            + "\n"
            + "return;";
    assertEquals(expectedStmts, builder.getStmtGraph().toString().trim());
  }
}
