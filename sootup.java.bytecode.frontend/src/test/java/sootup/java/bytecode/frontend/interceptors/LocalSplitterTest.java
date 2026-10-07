package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.interceptors.LocalSplitter;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class LocalSplitterTest {
  JavaView view;
  LocalSplitter localSplitter = new LocalSplitter();

  @BeforeEach
  public void setup() {
    String classPath = "src/test/resources/interceptors";
    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            classPath, SourceType.Application, Collections.emptyList());
    view = new JavaView(inputLocation);
  }

  private Body getBody(String methodName) {
    ClassType type = view.getIdentifierFactory().getClassType("LocalSplitterTarget");
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
  @Disabled("Takes too long. Good for profiling though.")
  public void JRT() {
    JrtFileSystemAnalysisInputLocation inputLocation =
        new JrtFileSystemAnalysisInputLocation(
            SourceType.Library, Collections.singletonList(localSplitter));
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    // ~200_000 methods
    view.getClasses()
        .parallel()
        .flatMap(clazz -> clazz.getMethods().stream())
        .filter(method -> !method.isAbstract() && !method.isNative())
        .forEach(SootMethod::getBody);
  }

  @Test
  public void testSimpleAssignment() {
    Body.BodyBuilder builder = Body.builder(getBody("simpleAssignment"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "l2#0 = 1;\n"
            + "l1#1 = l2#0 + 1;\n"
            + "l2#1 = l1#1 + 1;\n"
            + "\n"
            + "return;";

    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testSelfAssignment() {
    Body.BodyBuilder builder = Body.builder(getBody("selfAssignment"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
            + "l1#0 = 0;\n"
            + "l2#0 = 1;\n"
            + "l1#1 = l1#0 + 1;\n"
            + "l2#1 = l2#0 + 1;\n"
            + "\n"
            + "return;";

    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testBranch() {
    Body.BodyBuilder builder = Body.builder(getBody("branch"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testBranchMoreLocals() {
    Body.BodyBuilder builder = Body.builder(getBody("branchMoreLocals"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");
    expectedLocals.add("l1#5");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testBranchMoreBranches() {
    Body.BodyBuilder builder = Body.builder(getBody("branchMoreBranches"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");
    expectedLocals.add("l1#5");
    expectedLocals.add("l1#6");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testBranchElseIf() {
    Body.BodyBuilder builder = Body.builder(getBody("branchElseIf"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l1#2");
    expectedLocals.add("l1#3");
    expectedLocals.add("l1#4");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testForLoop() {
    Body.BodyBuilder builder = Body.builder(getBody("forLoop"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  @Test
  public void testReusedLocals() {
    Body.BodyBuilder builder = Body.builder(getBody("reusedLocals"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
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
        "this := @this: LocalSplitterTarget;\n"
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
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * The first try block only covers {@code a = 2}: its handler can only be entered before that
   * assignment completes (asynchronously, as a catch of Throwable allows), so it returns the 1
   * assigned before the try, l1#0.
   */
  @Test
  public void testTraps() {
    Body.BodyBuilder builder = Body.builder(getBody("traps"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    Set<String> expectedLocals = new HashSet<>();
    expectedLocals.add("this");
    expectedLocals.add("l1#0");
    expectedLocals.add("l1#1");
    expectedLocals.add("l2#0");
    expectedLocals.add("l2#1");
    expectedLocals.add("l3");
    expectedLocals.add("$stack4");
    expectedLocals.add("$stack5");
    expectedLocals.add("$stack6");
    expectedLocals.add("$stack7");
    expectedLocals.add("$stack8");

    assertLocals(expectedLocals, builder);

    String expectedStmts =
        "this := @this: LocalSplitterTarget;\n"
            + "l1#0 = 1;\n"
            + "\n"
            + "label1:\n"
            + "l1#1 = 2;\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack7 := @caughtexception;\n"
            + "l2#0 = $stack7;\n"
            + "$stack8 = staticinvoke <java.lang.Integer: java.lang.Integer valueOf(int)>(l1#0);\n"
            + "\n"
            + "return $stack8;\n"
            + "\n"
            + "label4:\n"
            + "l2#1 = \"\";\n"
            + "\n"
            + "label5:\n"
            + "$stack4 = <java.lang.System: java.io.PrintStream out>;\n"
            + "virtualinvoke $stack4.<java.io.PrintStream: void println()>();\n"
            + "\n"
            + "label6:\n"
            + "goto label8;\n"
            + "\n"
            + "label7:\n"
            + "$stack6 := @caughtexception;\n"
            + "l3 = $stack6;\n"
            + "\n"
            + "return l2#1;\n"
            + "\n"
            + "label8:\n"
            + "$stack5 = staticinvoke <java.lang.Double: java.lang.Double valueOf(double)>(0.0);\n"
            + "\n"
            + "return $stack5;\n"
            + "\n"
            + " catch java.lang.Throwable from label1 to label2 with label3;\n"
            + " catch java.lang.Throwable from label5 to label6 with label7;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  private Body getExceptionsBody(String methodName) {
    ClassType type = view.getIdentifierFactory().getClassType("LocalSplitterExceptionsTarget");
    return view.getClass(type).get().getMethods().stream()
        .filter(method -> method.getName().equals(methodName))
        .findFirst()
        .get()
        .getBody();
  }

  /**
   * {@code n = n.next} reads and redefines n, and throws a NullPointerException before it assigns
   * anything: the handler returns the n passed in (l1#0), the normal path the field read (l1#1).
   */
  @Test
  public void testRedefinitionThatThrowsLeavesTheOldValueToItsHandler() {
    Body.BodyBuilder builder = Body.builder(getExceptionsBody("fieldRead"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1#0 := @parameter0: LocalSplitterExceptionsTarget$Node;\n"
            + "\n"
            + "label1:\n"
            + "l1#1 = l1#0.<LocalSplitterExceptionsTarget$Node: LocalSplitterExceptionsTarget$Node next>;\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1#0;\n"
            + "\n"
            + "label4:\n"
            + "return l1#1;\n"
            + "\n"
            + " catch java.lang.NullPointerException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * As above, but work() can throw after n was overwritten: the handler may see either value, so
   * both definitions must stay one local.
   */
  @Test
  public void testRedefinitionThatThrowsFollowedByACall() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("fieldReadThenCall"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1 := @parameter0: LocalSplitterExceptionsTarget$Node;\n"
            + "\n"
            + "label1:\n"
            + "l1 = l1.<LocalSplitterExceptionsTarget$Node: LocalSplitterExceptionsTarget$Node next>;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1;\n"
            + "\n"
            + "label4:\n"
            + "return l1;\n"
            + "\n"
            + " catch java.lang.RuntimeException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * {@code a = 2} cannot throw, and a RuntimeException is never thrown asynchronously: the handler
   * is only entered from work(), after a = 2, so it returns l1#1.
   */
  @Test
  public void testHandlerOnlyEnteredByStmtsThatCanThrow() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("narrowHandler"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1#0 = 1;\n"
            + "\n"
            + "label1:\n"
            + "l1#1 = 2;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1#1;\n"
            + "\n"
            + "label4:\n"
            + "return l1#1;\n"
            + "\n"
            + " catch java.lang.RuntimeException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * Floating-point division cannot throw, and the constant divisor shows that {@code a / 2.0f} is
   * one: the handler is only entered from work(), after the division, so it returns l1#1.
   */
  @Test
  public void testFloatDivisionByAConstantCannotThrow() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("floatDivisionByConstant"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1#0 := @parameter0: float;\n"
            + "\n"
            + "label1:\n"
            + "l1#1 = l1#0 / 2.0F;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1#1;\n"
            + "\n"
            + "label4:\n"
            + "return l1#1;\n"
            + "\n"
            + " catch java.lang.RuntimeException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * {@code a / b} divides two locals. Their types say float, but a local's type cannot be trusted
   * before typing, so the division counts as one that can throw: the handler may see either a, and
   * l1 is not split.
   */
  @Test
  public void testDivisionOfLocalsCanThrow() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("floatDivisionByLocal"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1 := @parameter0: float;\n"
            + "l2 := @parameter1: float;\n"
            + "\n"
            + "label1:\n"
            + "l1 = l1 / l2;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack4 := @caughtexception;\n"
            + "l3 = $stack4;\n"
            + "\n"
            + "return l1;\n"
            + "\n"
            + "label4:\n"
            + "return l1;\n"
            + "\n"
            + " catch java.lang.RuntimeException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * The same with catch (Error): a VirtualMachineError can be thrown asynchronously before a = 2
   * completes (JLS 11.1.3), so the handler may see 1 or 2, and l1 is not split.
   */
  @Test
  public void testErrorHandlerCanBeEnteredAsynchronously() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("errorHandler"), Collections.emptySet());
    localSplitter.interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1 = 1;\n"
            + "\n"
            + "label1:\n"
            + "l1 = 2;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1;\n"
            + "\n"
            + "label4:\n"
            + "return l1;\n"
            + "\n"
            + " catch java.lang.Error from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /** Without asynchronous exceptions, as on ART, the Error handler is entered from work() only. */
  @Test
  public void testErrorHandlerWithSynchronousExceptionsOnly() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("errorHandler"), Collections.emptySet());
    new LocalSplitter(LocalSplitter.ExceptionalFlow.SYNCHRONOUS).interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1#0 = 1;\n"
            + "\n"
            + "label1:\n"
            + "l1#1 = 2;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1#1;\n"
            + "\n"
            + "label4:\n"
            + "return l1#1;\n"
            + "\n"
            + " catch java.lang.Error from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }

  /**
   * When any exception can be thrown at any point, e.g. injected by a debugger, even the
   * RuntimeException handler can be entered before a = 2, and l1 is not split.
   */
  @Test
  public void testAnyStmtEntersAnyHandler() {
    Body.BodyBuilder builder =
        Body.builder(getExceptionsBody("narrowHandler"), Collections.emptySet());
    new LocalSplitter(LocalSplitter.ExceptionalFlow.ANY).interceptBody(builder, view);

    String expectedStmts =
        "this := @this: LocalSplitterExceptionsTarget;\n"
            + "l1 = 1;\n"
            + "\n"
            + "label1:\n"
            + "l1 = 2;\n"
            + "staticinvoke <LocalSplitterExceptionsTarget: void work()>();\n"
            + "\n"
            + "label2:\n"
            + "goto label4;\n"
            + "\n"
            + "label3:\n"
            + "$stack3 := @caughtexception;\n"
            + "l2 = $stack3;\n"
            + "\n"
            + "return l1;\n"
            + "\n"
            + "label4:\n"
            + "return l1;\n"
            + "\n"
            + " catch java.lang.RuntimeException from label1 to label2 with label3;";
    assertEquals(expectedStmts, builder.getControlFlowGraph().toString().trim());
  }
}
