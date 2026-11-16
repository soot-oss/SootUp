package sootup.examples.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.MutableCallGraph;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.callgraph.config.CallGraphConfigBuilder;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.reflection.TamiflexReflectionModel;
import sootup.callgraph.scope.AppOnlyClinitCallResolver;
import sootup.callgraph.scope.CallFilter;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.ExcludedCallsCollectingCallResolver;
import sootup.callgraph.scope.ExplorationVerdict;
import sootup.callgraph.scope.SuppressClinitCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * One example per {@link CallGraphConfig} option, each run against a tiny program. Included in
 * {@code docs/callgraphs.md} and {@code docs/callgraph-cha-rta.md}.
 *
 * <p>{@code CallgraphComparison} is the Shape program of {@link CallGraphComparisonTest}; {@code
 * CallgraphFeatures/source/App.java} has a static initializer, a lambda and a reflective
 * instantiation.
 */
public class CallGraphOptionsTest {

  static final JavaView shapes = viewOf("src/test/resources/CallgraphComparison/binary");
  static final MethodSignature shapesMain = sig(shapes, "<Main: void main(java.lang.String[])>");

  static final JavaView view = viewOf("src/test/resources/CallgraphFeatures/binary");
  static final MethodSignature appMain = sig(view, "<App: void main(java.lang.String[])>");

  static JavaView viewOf(String binaryDir) {
    return new JavaView(
        List.of(
            new JavaClassPathAnalysisInputLocation(binaryDir, SourceType.Application),
            new DefaultRuntimeAnalysisInputLocation()));
  }

  static MethodSignature sig(JavaView view, String signature) {
    return view.getIdentifierFactory().parseMethodSignature(signature);
  }

  CallGraph chaOfApp(UnaryOperator<CallGraphConfigBuilder> tweak) {
    return tweak
        .apply(CallGraphConfig.builder().view(view).entryPoints(List.of(appMain)))
        .cha()
        .build()
        .computeCallGraph();
  }

  // ---------------------------------------------------------------- algorithm objects directly

  @Test
  public void directAlgorithmObjects() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:direct]
    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph fromMain = cha.initialize(List.of(mainMethod)); // explicit entry points
    CallGraph fromAllMains = cha.initialize(); // every main(String[]) of the application

    CallGraphAlgorithm rta = new RapidTypeAnalysisAlgorithm(view);
    CallGraph rtaGraph = rta.initialize(List.of(mainMethod));
    // --8<-- [end:direct]
    assertEquals(fromMain.getMethodSignatures(), fromAllMains.getMethodSignatures());
    assertTrue(rtaGraph.callCount() < fromMain.callCount());
  }

  @Test
  public void addClass() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:add-class]
    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = cha.initialize(List.of(mainMethod));
    // treat every method of Hexagon as an additional entry point
    ClassType hexagon = view.getIdentifierFactory().getClassType("Hexagon");
    CallGraph extended = cha.addClass(cg, hexagon);
    // --8<-- [end:add-class]
    assertTrue(extended.containsMethod(sig(view, "<Hexagon: void <init>()>")));
    assertFalse(cg.containsMethod(sig(view, "<Hexagon: void <init>()>")));
  }

  // ---------------------------------------------------------------------------- RTA option

  @Test
  public void rtaPreInstantiatedClasses() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:rta-preinstantiated]
    ClassType hexagon = view.getIdentifierFactory().getClassType("Hexagon");
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .rta()
            // e.g. objects a framework creates that the analyzed code never news itself
            .preInstantiatedClasses(Set.of(hexagon))
            .build()
            .computeCallGraph();
    // --8<-- [end:rta-preinstantiated]
    assertTrue(cg.containsMethod(sig(view, "<Hexagon: double area()>")));
  }

  @Test
  public void rtaConstructors() {
    JavaView view = shapes;
    ClassType hexagon = view.getIdentifierFactory().getClassType("Hexagon");
    // --8<-- [start:rta-constructor]
    CallGraphAlgorithm rta =
        new RapidTypeAnalysisAlgorithm(
            view,
            Set.of(hexagon), // preInstantiatedClasses
            new DefaultCallResolver(view), // callResolver
            VirtualCallResolver.all(), // virtualCallResolver
            true, // seedEntryPointClinits
            ReflectionModel.none(), // reflectionModel
            DynamicInvokeResolver.bootstrapMethodHandles()); // dynamicInvokeResolver
    // --8<-- [end:rta-constructor]
    assertTrue(
        rta.initialize(List.of(shapesMain)).containsMethod(sig(view, "<Hexagon: double area()>")));
  }

  @Test
  public void chaConstructors() {
    JavaView view = shapes;
    // --8<-- [start:cha-constructor]
    CallGraphAlgorithm cha =
        new ClassHierarchyAnalysisAlgorithm(
            view,
            new DefaultCallResolver(view), // callResolver
            VirtualCallResolver.all(), // virtualCallResolver
            true, // seedEntryPointClinits
            ReflectionModel.none(), // reflectionModel
            DynamicInvokeResolver.bootstrapMethodHandles()); // dynamicInvokeResolver
    // --8<-- [end:cha-constructor]
    assertTrue(
        cha.initialize(List.of(shapesMain)).containsMethod(sig(view, "<Hexagon: double area()>")));
  }

  // ------------------------------------------------------------------------- callResolver

  @Test
  public void callResolverDefault() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:call-resolver-default]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .callResolver(new DefaultCallResolver(view)) // the default: don't look into the JDK
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:call-resolver-default]
    MethodSignature objectInit = sig(view, "<java.lang.Object: void <init>()>");
    assertTrue(cg.containsMethod(objectInit)); // called ...
    assertTrue(cg.callsFrom(objectInit).isEmpty()); // ... but not expanded
  }

  @Test
  public void callResolverAll() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:call-resolver-all]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .callResolver(CallResolver.all()) // also expand library (JDK) methods
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:call-resolver-all]
    assertTrue(cg.containsMethod(sig(view, "<java.lang.Object: void <init>()>")));
  }

  @Test
  public void callResolverCollecting() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:call-resolver-collecting]
    ExcludedCallsCollectingCallResolver resolver = new ExcludedCallsCollectingCallResolver(view);
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .callResolver(resolver)
            .cha()
            .build()
            .computeCallGraph();

    // which methods were reached but not expanded? -> <java.lang.Object: void <init>()>
    Set<MethodSignature> notExpanded = resolver.getVisitedExcludedMethods();
    // --8<-- [end:call-resolver-collecting]
    assertEquals(Set.of(sig(view, "<java.lang.Object: void <init>()>")), notExpanded);
    assertNotNull(cg);
  }

  @Test
  public void callResolverCustomMethod() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:call-resolver-custom]
    CallResolver libraryDefault = new DefaultCallResolver(view);
    CallResolver skipCircle =
        caller ->
            caller.getDeclaringClassType().getClassName().equals("Circle")
                ? CallFilter.none() // Circle's methods: body is not even loaded
                : libraryDefault.tryAdvanceMethod(caller); // everything else: default behaviour

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .callResolver(skipCircle)
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:call-resolver-custom]
    MethodSignature circleInit = sig(view, "<Circle: void <init>()>");
    assertTrue(cg.containsMethod(circleInit));
    assertTrue(cg.callsFrom(circleInit).isEmpty());
    assertFalse(cg.callsFrom(sig(view, "<Square: void <init>()>")).isEmpty());
  }

  @Test
  public void callResolverPerStatement() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:call-resolver-per-stmt]
    // resolve only those calls whose target is not declared in java.*
    CallResolver noJdkCalls =
        caller ->
            CallFilter.of(
                stmt ->
                    stmt.getInvokeExpr()
                        .map(
                            invoke ->
                                !invoke
                                    .getMethodSignature()
                                    .getDeclClassType()
                                    .getPackageName()
                                    .getName()
                                    .startsWith("java."))
                        .orElse(true));

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .callResolver(noJdkCalls)
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:call-resolver-per-stmt]
    assertFalse(cg.containsMethod(sig(view, "<java.lang.Object: void <init>()>")));
    assertTrue(cg.containsMethod(sig(view, "<Circle: double area()>")));
  }

  // ------------------------------------------------------------------ virtualCallResolver

  @Test
  public void virtualCallResolverCustom() {
    JavaView view = shapes;
    MethodSignature mainMethod = shapesMain;
    // --8<-- [start:virtual-call-resolver-custom]
    VirtualCallResolver resolver =
        (caller, callee, stmt) -> {
          String targetClass = callee.getDeclClassType().getClassName();
          if (targetClass.equals("Hexagon")) {
            return ExplorationVerdict.STOP; // no edge at all
          }
          if (targetClass.equals("Triangle")) {
            return ExplorationVerdict.STOP_AFTER_CALL; // edge, but callee is not expanded
          }
          return ExplorationVerdict.EXPLORE_METHOD; // edge, callee is expanded (default)
        };

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(mainMethod))
            .virtualCallResolver(resolver)
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:virtual-call-resolver-custom]
    assertFalse(cg.containsMethod(sig(view, "<Hexagon: double area()>")));
    assertTrue(cg.containsMethod(sig(view, "<Triangle: double area()>")));
    assertTrue(cg.containsMethod(sig(view, "<Square: double area()>")));
  }

  // ---------------------------------------------------------------- <clinit> handling

  MethodSignature appClinit() {
    return sig(view, "<App: void <clinit>()>");
  }

  MethodSignature registryClinit() {
    return sig(view, "<Registry: void <clinit>()>");
  }

  Set<MethodSignature> libraryClinits(CallGraph cg) {
    return cg.getMethodSignatures().stream()
        .filter(m -> m.getName().equals("<clinit>"))
        .filter(m -> m.getDeclClassType().getPackageName().getName().startsWith("java."))
        .collect(Collectors.toSet());
  }

  @Test
  public void clinitDefault() {
    // --8<-- [start:clinit-default]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .cha() // = seedEntryPointClinits(true) + VirtualCallResolver.all()
            .build()
            .computeCallGraph();
    // --8<-- [end:clinit-default]
    assertTrue(cg.containsMethod(appClinit()));
    assertTrue(cg.containsMethod(registryClinit()));
    System.out.println("library <clinit>s (default): " + libraryClinits(cg));
  }

  @Test
  public void clinitNoSeed() {
    // --8<-- [start:clinit-no-seed]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .seedEntryPointClinits(false) // App.<clinit> is no longer a root
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:clinit-no-seed]
    assertFalse(cg.containsMethod(appClinit()));
    assertTrue(cg.containsMethod(registryClinit()));
  }

  @Test
  public void clinitSuppress() {
    // --8<-- [start:clinit-suppress]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .seedEntryPointClinits(true)
            .virtualCallResolver(new SuppressClinitCallResolver(view)) // drop discovered <clinit>s
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:clinit-suppress]
    assertTrue(cg.containsMethod(appClinit()));
    assertFalse(cg.containsMethod(registryClinit()));
  }

  @Test
  public void clinitAppOnly() {
    // --8<-- [start:clinit-app-only]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .seedEntryPointClinits(false)
            .virtualCallResolver(new AppOnlyClinitCallResolver(view)) // drop JDK <clinit>s only
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:clinit-app-only]
    assertTrue(cg.containsMethod(registryClinit()));
    assertTrue(libraryClinits(cg).isEmpty());
  }

  @Test
  public void clinitResolverChaining() {
    // --8<-- [start:clinit-chaining]
    VirtualCallResolver noHelper =
        (caller, callee, stmt) ->
            callee.getDeclClassType().getClassName().equals("Helper")
                ? ExplorationVerdict.STOP
                : ExplorationVerdict.EXPLORE_METHOD;
    // first drop JDK <clinit>s, then ask noHelper about everything else
    VirtualCallResolver resolver = new AppOnlyClinitCallResolver(view, noHelper);
    // --8<-- [end:clinit-chaining]
    CallGraph cg = chaOfApp(b -> b.virtualCallResolver(resolver));
    assertFalse(cg.containsMethod(sig(view, "<Helper: void work()>")));
    assertTrue(libraryClinits(cg).isEmpty());
  }

  // ------------------------------------------------------------------------ reflection

  MethodSignature pluginInit() {
    return sig(view, "<MyPlugin: void <init>()>");
  }

  @Test
  public void reflectionNone() {
    // --8<-- [start:reflection-none]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .reflectionModel(ReflectionModel.none()) // the default
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:reflection-none]
    assertFalse(cg.containsMethod(pluginInit()));
  }

  @Test
  public void reflectionTamiflex() {
    // --8<-- [start:reflection-tamiflex]
    // refl.log, recorded by running App with the TamiFlex Play-Out agent:
    //   Class.forName;MyPlugin;App.main;11;;
    //   Class.newInstance;MyPlugin;App.main;11;;
    ReflectionModel tamiflex =
        new TamiflexReflectionModel(view, Path.of("src/test/resources/CallgraphFeatures/refl.log"));

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .reflectionModel(tamiflex)
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:reflection-tamiflex]
    assertTrue(cg.containsMethod(pluginInit()));
  }

  @Test
  public void reflectionCustom() {
    ReflectionModel tamiflex =
        new TamiflexReflectionModel(view, Path.of("src/test/resources/CallgraphFeatures/refl.log"));
    // --8<-- [start:reflection-custom]
    // a ReflectionModel maps (method, body) to a body in which reflective calls are preceded by
    // plain Jimple; here: apply the TamiFlex model to application methods only
    ReflectionModel appOnly =
        (method, body) ->
            view.getClass(method.getDeclaringClassType())
                    .map(SootClass::isApplicationClass)
                    .orElse(false)
                ? tamiflex.resolve(method, body)
                : body;
    // --8<-- [end:reflection-custom]
    CallGraph cg = chaOfApp(b -> b.reflectionModel(appOnly));
    assertTrue(cg.containsMethod(pluginInit()));
  }

  // -------------------------------------------------------------------- invokedynamic

  MethodSignature helperWork() {
    return sig(view, "<Helper: void work()>");
  }

  MethodSignature lambdaBody() {
    return sig(view, "<App: void lambda$main$0()>");
  }

  @Test
  public void dynamicInvokeDefault() {
    // --8<-- [start:indy-default]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .dynamicInvokeResolver(DynamicInvokeResolver.bootstrapMethodHandles()) // the default
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:indy-default]
    assertTrue(cg.containsMethod(lambdaBody()));
    assertTrue(cg.containsMethod(helperWork()));
    System.out.println("callers of lambda (default): " + cg.callSourcesTo(lambdaBody()));
  }

  @Test
  public void dynamicInvokeNone() {
    // --8<-- [start:indy-none]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            .dynamicInvokeResolver(DynamicInvokeResolver.none()) // lambdas are never called
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:indy-none]
    assertFalse(cg.containsMethod(lambdaBody()));
    assertFalse(cg.containsMethod(helperWork()));
  }

  @Test
  public void dynamicInvokeCreationSite() {
    // --8<-- [start:indy-creation-site]
    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(List.of(appMain))
            // additionally: invokedynamic stmt -> lambda body, for code that never calls the lambda
            .dynamicInvokeResolver(
                DynamicInvokeResolver.bootstrapMethodHandles().withCreationSiteEdges())
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [end:indy-creation-site]
    assertTrue(cg.callSourcesTo(lambdaBody()).contains(appMain));
    System.out.println("callers of lambda (creation site): " + cg.callsTo(lambdaBody()));
  }

  // -------------------------------------------------------------------------- querying

  @Test
  public void query() {
    CallGraph cg =
        CallGraphConfig.builder()
            .view(shapes)
            .entryPoints(List.of(shapesMain))
            .cha()
            .build()
            .computeCallGraph();
    MethodSignature mainMethod = shapesMain;
    MethodSignature squareArea = sig(shapes, "<Square: double area()>");
    // --8<-- [start:query]
    List<MethodSignature> roots = cg.getEntryMethods(); // [<Main: void main(java.lang.String[])>]
    Set<MethodSignature> nodes = cg.getMethodSignatures(); // every reachable method
    int edges = cg.callCount();

    Set<MethodSignature> callees = cg.callTargetsFrom(mainMethod); // methods main may call
    Set<MethodSignature> callers = cg.callSourcesTo(squareArea); // methods that may call it

    Set<CallGraph.Call> out = cg.callsFrom(mainMethod); // edges incl. the calling stmt
    Set<CallGraph.Call> outInOrder = cg.sortedCallsFrom(mainMethod); // in stmt order
    Set<CallGraph.Call> in = cg.callsTo(squareArea);
    for (CallGraph.Call call : in) {
      System.out.println(
          call.sourceMethodSignature()
              + " line "
              + call.getLineNumber()
              + ": "
              + call.invokableStmt()
              + " -> "
              + call.targetMethodSignature());
    }

    boolean reachable = cg.containsMethod(squareArea);
    boolean edge = cg.containsCall(mainMethod, squareArea, in.iterator().next().invokableStmt());

    MutableCallGraph copy = cg.copy(); // independent copy you may modify
    // --8<-- [end:query]
    assertEquals(List.of(mainMethod), roots);
    assertTrue(nodes.contains(squareArea));
    assertTrue(edges > 0);
    assertTrue(callees.contains(squareArea));
    assertEquals(Set.of(mainMethod), callers);
    assertEquals(out.size(), outInOrder.size());
    assertTrue(reachable && edge);
    assertEquals(cg.callCount(), copy.callCount());
  }

  @Test
  public void dot() {
    CallGraph cg =
        CallGraphConfig.builder()
            .view(shapes)
            .entryPoints(List.of(shapesMain))
            .cha()
            .build()
            .computeCallGraph();
    // --8<-- [start:dot]
    String dot = cg.exportAsDot().collect(Collectors.joining("\n"));
    // --8<-- [end:dot]
    // --8<-- [start:dot-sorted]
    String sortedDot =
        cg.exportAsDot(
                Comparator.comparing(
                        (CallGraph.Call call) -> call.sourceMethodSignature().toString())
                    .thenComparing(call -> call.targetMethodSignature().toString())
                    .thenComparing(CallGraph.Call::getLineNumber))
            .collect(Collectors.joining("\n"));
    // --8<-- [end:dot-sorted]
    System.out.println(sortedDot);
    assertTrue(dot.startsWith("strict digraph"));
    assertEquals(dot.length(), sortedDot.length());
  }
}
