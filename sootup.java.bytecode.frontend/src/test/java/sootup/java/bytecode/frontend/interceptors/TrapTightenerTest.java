package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.interceptors.TrapTightener;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * @author Zun Wang
 */
public class TrapTightenerTest {

  private static final JavaView view =
      new JavaView(
          Arrays.asList(
              new JavaClassPathAnalysisInputLocation(
                  "src/test/resources/interceptors/",
                  SourceType.Application,
                  BytecodeBodyInterceptors.Default.getBodyInterceptors()),
              new DefaultRuntimeAnalysisInputLocation()));

  private Body load(String methodName, String returnType, String... parameterTypes) {
    return view.getMethod(
            view.getIdentifierFactory()
                .getMethodSignature(
                    "TrapTightenerTarget", methodName, returnType, Arrays.asList(parameterTypes)))
        .get()
        .getBody();
  }

  private Body tighten(Body body) {
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    new TrapTightener().interceptBody(builder, view);
    return builder.build();
  }

  private Body tighten(String methodName, String... parameterTypes) {
    return tighten(load(methodName, "int", parameterTypes));
  }

  /**
   * @return each exceptional flow as "stmt -> caught type", sorted
   */
  private static List<String> exceptionalFlows(Body body) {
    ControlFlowGraph<?> graph = body.getControlFlowGraph();
    return graph.getStmts().stream()
        .flatMap(
            stmt ->
                graph.exceptionalSuccessors(stmt).keySet().stream()
                    .map(type -> stmt + " -> " + type))
        .sorted()
        .collect(Collectors.toList());
  }

  private static long handlerCount(Body body) {
    return body.getStmts().stream()
        .filter(stmt -> stmt.toString().contains("@caughtexception"))
        .count();
  }

  @Test
  public void tightenRange() {
    Body body = tighten("tightenRange", "int", "int");
    assertEquals(
        Collections.singletonList("l3#2 = l3#1 / l1 -> java.lang.ArithmeticException"),
        exceptionalFlows(body));
  }

  @Test
  public void splitRange() {
    Body body = tighten("splitRange", "int", "int");
    assertEquals(
        Arrays.asList(
            "l3#1 = l1 / l2 -> java.lang.ArithmeticException",
            "l3#3 = l3#2 / l2 -> java.lang.ArithmeticException"),
        exceptionalFlows(body));
  }

  @Test
  public void noThrowingStmtRemovesHandler() {
    Body body = tighten("noThrowingStmt", "int", "int");
    assertEquals(Collections.emptyList(), exceptionalFlows(body));
    assertEquals(0, handlerCount(body));
  }

  @Test
  public void catchSupertypeKeepsFlow() {
    Body body = tighten("catchSupertype", "int", "int");
    assertEquals(
        Collections.singletonList("l3#2 = l3#1 / l1 -> java.lang.RuntimeException"),
        exceptionalFlows(body));
  }

  @Test
  public void multipleHandlers() {
    Body body = tighten("multipleHandlers", "int[]", "int", "int");
    assertEquals(
        Arrays.asList(
            "l1[0] = l4#1 -> java.lang.NullPointerException",
            "l4#1 = l2 / l3 -> java.lang.ArithmeticException"),
        exceptionalFlows(body));
    assertEquals(2, handlerCount(body));
  }

  @Test
  public void invokeKeepsFlow() {
    Body body = tighten("invoke", "int");
    assertEquals(
        Collections.singletonList(
            "virtualinvoke this.<TrapTightenerTarget: void mayThrow()>() -> java.io.IOException"),
        exceptionalFlows(body));
  }

  @Test
  public void synchronizedBlockKeepsCatchAll() {
    Body before = load("synchronizedBlock", "void", "int", "int");
    Body body = tighten(before);
    assertEquals(exceptionalFlows(before), exceptionalFlows(body));
    assertEquals(handlerCount(before), handlerCount(body));
  }
}
