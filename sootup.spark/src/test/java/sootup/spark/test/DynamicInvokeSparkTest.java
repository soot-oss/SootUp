package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.invokedynamic.DynamicInvokeResolver;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.spark.Spark;
import sootup.spark.SparkCallGraphConfig;
import sootup.spark.SparkOptions;

/**
 * Spark (CHA-based and on-the-fly) with the shared {@link DynamicInvokeResolver}: lambda bodies are
 * reachable and captured values flow into them. Sources in resources/invokedynamic/source. No JDK
 * on the class path: OTF has no call resolver and would otherwise crawl the runtime.
 */
public class DynamicInvokeSparkTest {

  private static final String DIR = "src/test/resources/invokedynamic/";

  private final JavaView view =
      new JavaView(new JavaClassPathAnalysisInputLocation(DIR + "binary"));

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory()
        .getMethodSignature("indy." + cls, name, ret, Arrays.asList(params));
  }

  private MethodSignature main(String cls) {
    return sig(cls, "main", "void", "java.lang.String[]");
  }

  private Spark spark(MethodSignature entry, boolean otf, DynamicInvokeResolver resolver) {
    return Spark.builder()
        .view(view)
        .entryPoints(List.of(entry))
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(otf).build())
        .dynamicInvokeResolver(resolver)
        .build();
  }

  /** Types the {@code index}-th parameter of {@code method} may point to. */
  private Set<String> paramTypes(Spark spark, MethodSignature method, int index) {
    Local param =
        view.getMethod(method).get().getBody().getStmts().stream()
            .filter(s -> s instanceof JIdentityStmt id && id.getRightOp() instanceof JParameterRef)
            .map(s -> (JIdentityStmt) s)
            .filter(s -> ((JParameterRef) s.getRightOp()).getIndex() == index)
            .findFirst()
            .get()
            .getLeftOp();
    return spark.getPointsToAnalysis().reachingTypes(param, method).stream()
        .map(Type::toString)
        .collect(Collectors.toSet());
  }

  @Test
  public void capturedValueFlowsIntoStaticLambdaBody() {
    MethodSignature entry = main("CapturingLambda");
    MethodSignature body = sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload");
    MethodSignature sink = sig("CapturingLambda", "sink", "void", "java.lang.Object");
    for (boolean otf : new boolean[] {false, true}) {
      Spark spark = spark(entry, otf, DynamicInvokeResolver.bootstrapMethodHandles());
      assertTrue(spark.getCallGraph().callTargetsFrom(entry).contains(body), "otf=" + otf);
      assertEquals(Set.of("indy.Payload"), paramTypes(spark, body, 0), "otf=" + otf);
      assertEquals(Set.of("indy.Payload"), paramTypes(spark, sink, 0), "otf=" + otf);
    }
  }

  @Test
  public void capturedReceiverFlowsIntoInstanceLambdaBody() {
    // () -> sink(this.f): needs the captured 'this' bound to the lambda body's receiver
    MethodSignature entry = main("InstanceLambda");
    MethodSignature sink = sig("InstanceLambda", "sink", "void", "java.lang.Object");
    for (boolean otf : new boolean[] {false, true}) {
      Spark spark = spark(entry, otf, DynamicInvokeResolver.bootstrapMethodHandles());
      assertEquals(Set.of("indy.Payload"), paramTypes(spark, sink, 0), "otf=" + otf);
    }
  }

  @Test
  public void otfReachesBoundMethodRefAndConstructorRef() {
    assertTrue(
        spark(main("BoundMethodRef"), true, DynamicInvokeResolver.bootstrapMethodHandles())
            .getCallGraph()
            .callTargetsFrom(main("BoundMethodRef"))
            .contains(sig("Worker", "work", "void")));
    assertTrue(
        spark(main("ConstructorRef"), true, DynamicInvokeResolver.bootstrapMethodHandles())
            .getCallGraph()
            .callTargetsFrom(main("ConstructorRef"))
            .contains(sig("Worker", "<init>", "void")));
  }

  @Test
  public void noneLeavesLambdaBodyUnreachable() {
    MethodSignature entry = main("CapturingLambda");
    MethodSignature body = sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload");
    for (boolean otf : new boolean[] {false, true}) {
      assertFalse(
          spark(entry, otf, DynamicInvokeResolver.none()).getCallGraph().containsMethod(body),
          "otf=" + otf);
    }
  }

  @Test
  public void unifiedConfigPassesResolverToChaAndPag() {
    MethodSignature entry = main("CapturingLambda");
    MethodSignature body = sig("CapturingLambda", "lambda$main$0", "void", "indy.Payload");
    for (DynamicInvokeResolver resolver :
        List.of(DynamicInvokeResolver.bootstrapMethodHandles(), DynamicInvokeResolver.none())) {
      CallGraph cg =
          CallGraphConfig.builder()
              .view(view)
              .entryPoints(List.of(entry))
              .dynamicInvokeResolver(resolver)
              .into(SparkCallGraphConfig::from)
              .build()
              .computeCallGraph();
      assertEquals(!resolver.isNone(), cg.containsMethod(body));
    }
  }
}
