package sootup.callgraph.implicit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.callgraph.scope.CallFilter;
import sootup.callgraph.scope.CallResolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;

/**
 * Which calls fire the rules of a {@link RuleBasedImplicitCallModel}, and which methods the
 * implicit invocations call. The fixture is {@code callgraph/ImplicitResolution}; its class {@code
 * ir.MissingExecutor} is not in the view.
 */
@Tag("Java8")
public class ImplicitCallResolutionTest {

  private static final String RESOURCES = "src/test/resources/callgraph/ImplicitResolution/";

  private final JavaView view = createView(false);

  private static JavaView createView(boolean withJimple) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new DefaultRuntimeAnalysisInputLocation(SourceType.Library, Collections.emptyList()));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            RESOURCES + "binary", SourceType.Application, Collections.emptyList()));
    if (withJimple) {
      inputLocations.add(
          new JimpleAnalysisInputLocation(Paths.get(RESOURCES + "jimple"), SourceType.Application));
    }
    return new JavaView(inputLocations);
  }

  private MethodSignature sig(String signature) {
    return view.getIdentifierFactory().parseMethodSignature(signature);
  }

  private MethodSignature fixture(String method) {
    return sig("<ir.ImplicitResolution: " + method + ">");
  }

  /** The JDK model and a rule in which the static Util.post(Runnable) calls the given target */
  private ImplicitCallModel model(ImplicitCallTarget.Direct postTarget) {
    ImplicitCallModel jdk = ImplicitCallModel.jdk();
    ImplicitCallModel post =
        ImplicitCallModel.of(
            List.of(
                new ImplicitCall(
                    sig("<ir.Util: void post(java.lang.Runnable)>"), true, List.of(postTarget))));
    return (context, body, stmt) -> {
      List<AbstractInstanceInvokeExpr> invokes = new ArrayList<>(jdk.resolve(context, body, stmt));
      invokes.addAll(post.resolve(context, body, stmt));
      return invokes;
    };
  }

  /** The JDK model and a rule in which the static Util.post(Runnable) runs its argument */
  private ImplicitCallModel model() {
    return model(
        new ImplicitCallTarget.Direct(
            0, view.getIdentifierFactory().parseMethodSubSignature("void run()")));
  }

  /** A call resolver that does not follow calls into the methods of the given classes */
  private CallResolver excluding(String... classNames) {
    Set<ClassType> excluded =
        Arrays.stream(classNames)
            .map(view.getIdentifierFactory()::getClassType)
            .collect(Collectors.toSet());
    return method ->
        excluded.contains(method.getDeclaringClassType()) ? CallFilter.none() : CallFilter.all();
  }

  /** CHA and RTA call graphs from the given entry point */
  private static List<CallGraph> callGraphs(
      JavaView view, ImplicitCallModel model, CallResolver resolver, MethodSignature entryPoint) {
    List<CallGraph> callGraphs = new ArrayList<>();
    for (boolean rta : new boolean[] {false, true}) {
      CallGraphConfigBuilder builder =
          CallGraphConfig.builder()
              .view(view)
              .entryPoints(Collections.singletonList(entryPoint))
              .implicitCallModel(model);
      if (resolver != null) {
        builder.callResolver(resolver);
      }
      callGraphs.add(
          rta
              ? builder.rta().build().computeCallGraph()
              : builder.cha().build().computeCallGraph());
    }
    return callGraphs;
  }

  private List<CallGraph> callGraphs(ImplicitCallModel model, CallResolver resolver) {
    return callGraphs(view, model, resolver, fixture("void main(java.lang.String[])"));
  }

  private List<CallGraph> callGraphs(ImplicitCallModel model) {
    return callGraphs(model, null);
  }

  /** The CHA call graph, whose targets of a call are all subtypes of the class it names */
  private CallGraph cha(ImplicitCallModel model) {
    return callGraphs(model).get(0);
  }

  /** The RTA call graph, whose targets of a call are the instantiated subtypes */
  private CallGraph rta(ImplicitCallModel model) {
    return callGraphs(model).get(1);
  }

  private static Set<MethodSignature> targets(CallGraph cg, MethodSignature source) {
    return cg.callsFrom(source).stream()
        .map(CallGraph.Call::targetMethodSignature)
        .collect(Collectors.toSet());
  }

  @Test
  public void superStartCallsRunInsideTheOverride() {
    MethodSignature run = sig("<ir.SuperStartThread: void run()>");
    for (CallGraph cg : callGraphs(model())) {
      assertTrue(targets(cg, sig("<ir.SuperStartThread: void start()>")).contains(run));
      assertFalse(targets(cg, fixture("void superStart()")).contains(run));
    }
  }

  @Test
  public void overrideWithoutSuperStartDoesNotCallRun() {
    for (CallGraph cg : callGraphs(model())) {
      assertFalse(
          targets(cg, fixture("void overrideWithoutSuper()"))
              .contains(sig("<ir.NoStartThread: void run()>")));
    }
  }

  @Test
  public void startOnAThreadTypedLocalDependsOnTheTargetsOfTheCall() {
    // the call names Thread.start(); CHA dispatches it to Thread.start() too, RTA only to the
    // override of the instantiated NoStartThread, which never starts the thread
    MethodSignature run = sig("<ir.NoStartThread: void run()>");
    assertTrue(targets(cha(model()), fixture("void threadTypedOverride()")).contains(run));
    assertFalse(targets(rta(model()), fixture("void threadTypedOverride()")).contains(run));
  }

  @Test
  public void rtaStartsTheThreadOnceAPlainThreadIsInstantiated() {
    // at the call of start(), only NoStartThread is instantiated; the Thread instantiated later
    // gives the call Thread.start() as a target, which fires the rule
    MethodSignature entry = fixture("void lateInstantiation()");
    CallGraph rta = callGraphs(view, model(), null, entry).get(1);
    assertTrue(targets(rta, entry).contains(sig("<ir.NoStartThread: void run()>")));
  }

  @Test
  public void analyzedExecutorIsFollowedInsteadOfModeled() {
    MethodSignature run = sig("<ir.Job: void run()>");
    for (CallGraph cg : callGraphs(model())) {
      assertFalse(targets(cg, fixture("void droppingExecutor()")).contains(run));
    }
    for (CallGraph cg : callGraphs(model(), excluding("ir.DroppingExecutor"))) {
      assertTrue(targets(cg, fixture("void droppingExecutor()")).contains(run));
    }
  }

  @Test
  public void executorInheritingItsImplementationFromAnotherClass() {
    // InheritingExecutor implements Executor with Base.execute(Runnable), Base is no Executor
    MethodSignature run = sig("<ir.Job: void run()>");
    for (CallGraph cg : callGraphs(model())) {
      assertFalse(targets(cg, fixture("void inheritingExecutor()")).contains(run));
    }
    for (CallGraph cg : callGraphs(model(), excluding("ir.Base"))) {
      assertTrue(targets(cg, fixture("void inheritingExecutor()")).contains(run));
    }
  }

  @Test
  public void interfaceTypedExecutorDependsOnTheTargetsOfTheCall() {
    // CHA dispatches to the executors of the runtime, which are not analyzed; RTA only to the
    // instantiated DroppingExecutor, which is
    MethodSignature run = sig("<ir.Job: void run()>");
    MethodSignature execute = sig("<ir.DroppingExecutor: void execute(java.lang.Runnable)>");
    Set<MethodSignature> cha = targets(cha(model()), fixture("void interfaceTypedExecutor()"));
    assertTrue(cha.contains(run));
    assertTrue(cha.contains(execute));
    Set<MethodSignature> rta = targets(rta(model()), fixture("void interfaceTypedExecutor()"));
    assertFalse(rta.contains(run));
    assertTrue(rta.contains(execute));
  }

  @Test
  public void unrelatedClassWithTheSameSubsignatureDoesNotFire() {
    for (CallGraph cg : callGraphs(model())) {
      assertFalse(targets(cg, fixture("void unrelated()")).contains(sig("<ir.Job: void run()>")));
    }
  }

  @Test
  public void callOnAClassMissingFromTheViewFires() {
    for (CallGraph cg : callGraphs(model())) {
      assertTrue(targets(cg, fixture("void missing()")).contains(sig("<ir.Job: void run()>")));
    }
  }

  @Test
  public void staticSourceThroughASubclassFiresAndAHidingMethodDoesNot() {
    MethodSignature run = sig("<ir.Job: void run()>");
    for (CallGraph cg : callGraphs(model())) {
      assertTrue(targets(cg, fixture("void staticThroughSubclass()")).contains(run));
      assertFalse(targets(cg, fixture("void hidingStatic()")).contains(run));
      // the hiding method calls the source itself
      assertTrue(targets(cg, sig("<ir.HidingUtil: void post(java.lang.Runnable)>")).contains(run));
    }
  }

  @Test
  public void localTypedNarrowerThanTheParameterNarrowsTheDispatch() {
    // CHA: the calls of execute() on an Executor parameter reach the runtime's executors
    CallGraph cg = cha(model());
    Set<MethodSignature> narrow =
        targets(cg, fixture("void narrowLocal(java.util.concurrent.Executor)"));
    assertTrue(narrow.contains(sig("<ir.Job: void run()>")));
    assertFalse(narrow.contains(sig("<ir.OtherJob: void run()>")));
    Set<MethodSignature> wide =
        targets(cg, fixture("void wideLocal(java.util.concurrent.Executor,java.lang.Runnable)"));
    assertTrue(wide.contains(sig("<ir.Job: void run()>")));
    assertTrue(wide.contains(sig("<ir.OtherJob: void run()>")));
  }

  @Test
  public void declaringClassOfTheTargetIsNarrowedByTheLocal() {
    ImplicitCallModel model =
        model(
            new ImplicitCallTarget.Direct(
                0,
                view.getIdentifierFactory().parseMethodSubSignature("void run()"),
                view.getIdentifierFactory().getClassType("java.lang.Runnable"),
                Map.of()));
    for (CallGraph cg : callGraphs(model)) {
      Set<MethodSignature> targets = targets(cg, fixture("void work()"));
      assertTrue(targets.contains(sig("<ir.OtherJob: void run()>")));
      assertFalse(targets.contains(sig("<ir.Job: void run()>")));
    }
  }

  @Test
  public void methodMissingInTheDeclaringClassIsLookedUpInTheLocalsType() {
    ImplicitCallModel model =
        model(
            new ImplicitCallTarget.Direct(
                0,
                view.getIdentifierFactory().parseMethodSubSignature("void work()"),
                view.getIdentifierFactory().getClassType("java.lang.Runnable"),
                Map.of()));
    for (CallGraph cg : callGraphs(model)) {
      assertTrue(targets(cg, fixture("void work()")).contains(sig("<ir.OtherJob: void work()>")));
    }
  }

  @Test
  public void objectTypedArgumentIsCalledAsTheParameterType() {
    // in ObjectTyped.jimple, the PrivilegedAction is passed in a local of type Object
    JavaView jimpleView = createView(true);
    MethodSignature main = sig("<ir.ObjectTyped: void main(java.lang.String[])>");
    for (CallGraph cg : callGraphs(jimpleView, model(), null, main)) {
      // without the parameter type, the method would be looked up in Object, which has no run()
      assertTrue(targets(cg, main).contains(sig("<ir.Action: java.lang.Object run()>")));
    }
  }
}
