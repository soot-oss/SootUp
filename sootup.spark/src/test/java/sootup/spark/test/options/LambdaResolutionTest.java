package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.Node;
import sootup.spark.test.SparkTestUtil;

/**
 * Covers {@link sootup.spark.MethodPAGStmtVisitor#handleInvokeExprOtf}'s handling of {@code
 * LambdaMetafactory}-backed {@code invokedynamic} sites: the lambda/method-reference target must
 * become reachable and analyzed, its captured arguments (and receiver, for bound/unbound instance
 * method references) must be wired into the target's parameters, and no return edge may be
 * fabricated from the target back into the indy site's {@code lhs}.
 */
public class LambdaResolutionTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("LambdaVariants");
  ClassType valueType = SparkTestUtil.idFactory.getClassType("LambdaVariants$Value");
  ClassType boxType = SparkTestUtil.idFactory.getClassType("LambdaVariants$Box");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  MethodSignature lambdaSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, "lambda$main$0", valueType, List.of(valueType));
  MethodSignature identitySig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, "identity", valueType, List.of(valueType));
  MethodSignature getValueSig =
      SparkTestUtil.idFactory.getMethodSignature(boxType, "getValue", valueType, List.of());
  MethodSignature ctorSig =
      SparkTestUtil.idFactory.getMethodSignature(
          valueType, "<init>", VoidType.getInstance(), Collections.emptyList());

  SparkOptions otfOptions = SparkOptions.builder().onFlyCallGraph(true).build();

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void capturingStaticLambdaBodyIsReachableAndCaptureFlows() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();
    assertTrue(cg.containsMethod(lambdaSig), "lambda body must be reachable, not dead code");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(lambdaSig), "main -> lambda body edge must exist");
    assertTrue(
        cg.containsMethod(identitySig),
        "identity() is only reachable if the lambda body itself was analyzed, not just registered");
    assertTrue(
        cg.callTargetsFrom(lambdaSig).contains(identitySig),
        "lambda body -> identity() edge must exist");

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    // "seed" is captured directly off the allocation stack temp ($stack8) rather than a named
    // local, since javac/the frontend only materializes a named local for multi-use values.
    Set<Node> aliases = pta.aliases(new Local("$stack8", valueType), mainSig);
    assertTrue(
        aliases.stream().anyMatch(n -> n.getContainingMethodSig().equals(lambdaSig)),
        () ->
            "captured local's alias set must include a PAG vertex inside the lambda body "
                + "(the wired parameter), proving the capture edge was installed: "
                + aliases);
  }

  @Test
  public void boundInstanceMethodReferenceIsReachable() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();
    assertTrue(
        cg.containsMethod(getValueSig), "Box.getValue() must be reachable via box::getValue");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(getValueSig),
        "main -> Box.getValue() edge must exist");

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    Set<Node> aliases = pta.aliases(new Local("l4", boxType), mainSig);
    assertTrue(
        aliases.stream().anyMatch(n -> n.getContainingMethodSig().equals(getValueSig)),
        () ->
            "captured receiver's alias set must include a PAG vertex inside Box.getValue() "
                + "(its `this`), proving the receiver-offset wiring landed correctly: "
                + aliases);
  }

  @Test
  public void constructorReferenceIsReachableAndDoesNotCrash() {
    Spark spark =
        assertDoesNotThrow(
            () ->
                Spark.builder()
                    .view(SparkTestUtil.view)
                    .entryPoints(Collections.singletonList(mainSig))
                    .sparkOptions(otfOptions)
                    .build());

    CallGraph cg = assertDoesNotThrow(spark::getCallGraph);
    assertDoesNotThrow(spark::getPointsToAnalysis);

    assertTrue(cg.containsMethod(ctorSig), "Value.<init>() must be reachable via Value::new");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(ctorSig), "main -> Value.<init>() edge must exist");
  }

  /**
   * At an indy site, {@code lhs} is bound to the functional-interface instance the JVM manufactures
   * at link time, not to the target method's return value. Pins that no return edge is
   * (re-)introduced from the lambda body back into the indy site's {@code lhs}.
   */
  @Test
  public void noFabricatedReturnEdgeAtIndySite() {
    ClassType lambdaCallSiteClass = SparkTestUtil.idFactory.getClassType("LambdaCallSite");
    ClassType supplierType = SparkTestUtil.idFactory.getClassType("java.util.function.Supplier");
    MethodSignature lambdaCallSiteMain =
        SparkTestUtil.idFactory.getMethodSignature(
            lambdaCallSiteClass, SparkTestUtil.idFactory.getMainSubSignature());

    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(lambdaCallSiteMain))
            .sparkOptions(otfOptions)
            .build();

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    assertTrue(
        pta.reachingObjects(new Local("l4", supplierType), lambdaCallSiteMain).isEmpty(),
        "the indy site's lhs (the Supplier local) must not pick up the lambda body's "
            + "allocation via a fabricated return edge");
  }
}
