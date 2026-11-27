package sootup.callgraph.implicit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class ImplicitCallModelTest {

  private static final String CLASS_PATH = "src/test/resources/callgraph/ImplicitModel/binary";

  private final JavaView view = createView();
  private final MethodSignature main =
      view.getIdentifierFactory()
          .getMethodSignature(
              view.getIdentifierFactory().getClassType("im.ImplicitModel"),
              view.getIdentifierFactory().getMainSubSignature());

  private static JavaView createView() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library, Collections.emptyList()));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            CLASS_PATH, SourceType.Application, Collections.emptyList()));
    return new JavaView(inputLocations);
  }

  private MethodSignature sig(String signature) {
    return view.getIdentifierFactory().parseMethodSignature(signature);
  }

  private CallGraphConfigBuilder builder(Optional<ImplicitCallModel> model) {
    CallGraphConfigBuilder builder =
        CallGraphConfig.builder().view(view).entryPoints(Collections.singletonList(main));
    model.ifPresent(builder::implicitCallModel);
    return builder;
  }

  private List<CallGraph> callGraphs(Optional<ImplicitCallModel> model) {
    return List.of(
        builder(model).cha().build().computeCallGraph(),
        builder(model).rta().build().computeCallGraph());
  }

  /** The call statement in main that invokes the given method */
  private InvokableStmt callSite(String invoked) {
    MethodSignature invokedSig = sig(invoked);
    for (Stmt stmt : view.getMethod(main).get().getBody().getStmts()) {
      if (stmt.isInvokableStmt()) {
        Optional<AbstractInvokeExpr> expr = stmt.asInvokableStmt().getInvokeExpr();
        if (expr.isPresent() && expr.get().getMethodSignature().equals(invokedSig)) {
          return stmt.asInvokableStmt();
        }
      }
    }
    throw new AssertionError("no call of " + invoked);
  }

  /** The statements of the calls from main to the given target */
  private Set<InvokableStmt> callSitesOf(CallGraph cg, String target) {
    MethodSignature targetSig = sig(target);
    return cg.callsFrom(main).stream()
        .filter(call -> call.targetMethodSignature().equals(targetSig))
        .map(CallGraph.Call::invokableStmt)
        .collect(Collectors.toSet());
  }

  @Test
  public void threadStartCallsRunAtTheStartStatements() {
    // default model: Thread.start() calls run() on the thread
    for (CallGraph cg : callGraphs(Optional.empty())) {
      // MyThread.run() can be called by both starts, as "thread" is a Thread
      assertEquals(
          Set.of(
              callSite("<im.MyThread: void start()>"),
              callSite("<java.lang.Thread: void start()>")),
          callSitesOf(cg, "<im.MyThread: void run()>"));
    }
  }

  @Test
  public void implicitCallsAreAttributedToStatementsOfTheBody() {
    Set<Stmt> body = Collections.newSetFromMap(new IdentityHashMap<>());
    body.addAll(view.getMethod(main).get().getBody().getStmts());
    for (CallGraph cg : callGraphs(Optional.of(ImplicitCallModel.jdk()))) {
      for (CallGraph.Call call : cg.callsFrom(main)) {
        assertTrue(body.contains(call.invokableStmt()), call.toString());
        assertTrue(call.getLineNumber() > 0, call.toString());
      }
    }
  }

  @Test
  public void jdkModelCallsRunnablesOfThreadsExecutorsAndPrivilegedActions() {
    List<CallGraph> callGraphs = callGraphs(Optional.of(ImplicitCallModel.jdk()));
    // CHA dispatches execute() to the runtime's executors, which are not analyzed; RTA only to the
    // instantiated DirectExecutor, whose execute() is analyzed and does not run the task
    assertEquals(
        Set.of(callSite("<java.util.concurrent.Executor: void execute(java.lang.Runnable)>")),
        callSitesOf(callGraphs.get(0), "<im.Task: void run()>"));
    assertTrue(callSitesOf(callGraphs.get(1), "<im.Task: void run()>").isEmpty());
    for (CallGraph cg : callGraphs) {
      assertEquals(
          Set.of(callSite("<java.lang.Thread: void start()>")),
          callSitesOf(cg, "<im.ThreadRunnable: void run()>"));
      assertEquals(
          Set.of(
              callSite(
                  "<java.security.AccessController: java.lang.Object"
                      + " doPrivileged(java.security.PrivilegedAction)>")),
          callSitesOf(cg, "<im.Action: java.lang.Object run()>"));
      // the start of a Thread subclass is still modeled
      assertTrue(
          callSitesOf(cg, "<im.MyThread: void run()>")
              .contains(callSite("<im.MyThread: void start()>")));
    }
  }

  @Test
  public void defaultModelOnlyCallsTheThreadItself() {
    for (CallGraph cg : callGraphs(Optional.empty())) {
      assertTrue(callSitesOf(cg, "<im.ThreadRunnable: void run()>").isEmpty());
      assertTrue(callSitesOf(cg, "<im.Task: void run()>").isEmpty());
    }
  }

  @Test
  public void noneModelHasNoImplicitCalls() {
    for (CallGraph cg : callGraphs(Optional.of(ImplicitCallModel.none()))) {
      assertTrue(callSitesOf(cg, "<im.MyThread: void run()>").isEmpty());
    }
  }
}
