package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.test.SparkTestUtil;

/**
 * Regression test for the crash traced back from sosa-analyzer's {@code
 * IFDSCallSemantics.buildSplitCall}: {@code IndexOutOfBoundsException} when a resolved reflective
 * callee's declared arity exceeds what its call site ({@code Constructor#newInstance(Object[])} or
 * {@code Method#invoke(Object, Object[])}) can syntactically supply. The FAIR converter builds a
 * callee's actual-argument list directly from the call site's own syntactic arguments (never from
 * the callee's real declared arity), so {@link sootup.spark.Solver#resolveConstructor} / {@code
 * findMethodByName} must never wire an edge to a callee whose parameter count exceeds that bound --
 * doing so doesn't just lose precision downstream, it crashes.
 */
public class ReflectiveArityBoundTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("ReflectiveArityBound");
  ClassType tooManyCtorArgsType =
      SparkTestUtil.idFactory.getClassType("ReflectiveArityBound$TooManyCtorArgs");
  ClassType oneCtorArgType =
      SparkTestUtil.idFactory.getClassType("ReflectiveArityBound$OneCtorArg");
  ClassType tooManyMethodParamsType =
      SparkTestUtil.idFactory.getClassType("ReflectiveArityBound$TooManyMethodParams");

  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());

  MethodSignature tooManyCtorArgsSig =
      SparkTestUtil.idFactory.getMethodSignature(
          tooManyCtorArgsType,
          "<init>",
          VoidType.getInstance(),
          java.util.List.of(PrimitiveType.getInt(), PrimitiveType.getInt()));
  MethodSignature ctorTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          tooManyCtorArgsType, "ctorTouch", VoidType.getInstance(), Collections.emptyList());

  MethodSignature oneCtorArgSig =
      SparkTestUtil.idFactory.getMethodSignature(
          oneCtorArgType,
          "<init>",
          VoidType.getInstance(),
          Collections.singletonList(PrimitiveType.getInt()));
  MethodSignature oneArgCtorTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          oneCtorArgType, "oneArgCtorTouch", VoidType.getInstance(), Collections.emptyList());

  MethodSignature tooManyMethodParamsCtorSig =
      SparkTestUtil.idFactory.getMethodSignature(
          tooManyMethodParamsType, "<init>", VoidType.getInstance(), Collections.emptyList());
  MethodSignature bigMethodSig =
      SparkTestUtil.idFactory.getMethodSignature(
          tooManyMethodParamsType,
          "bigMethod",
          VoidType.getInstance(),
          java.util.List.of(
              PrimitiveType.getInt(), PrimitiveType.getInt(), PrimitiveType.getInt()));
  MethodSignature bigMethodTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          tooManyMethodParamsType,
          "bigMethodTouch",
          VoidType.getInstance(),
          Collections.emptyList());

  SparkOptions otfOptions = SparkOptions.builder().onFlyCallGraph(true).build();

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void constructorExceedingCallSiteArityIsNotWired() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();

    assertFalse(
        cg.containsMethod(tooManyCtorArgsSig),
        "getConstructor().newInstance() supplies at most one syntactic argument; a two-arg "
            + "constructor must not be wired, or IFDSCallSemantics.buildSplitCall crashes "
            + "indexing past it");
    assertFalse(
        cg.containsMethod(ctorTouchSig),
        "ctorTouch() is only reachable through the never-should-be-wired constructor");
  }

  @Test
  public void constructorWithinCallSiteArityIsStillWired() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();

    assertTrue(
        cg.containsMethod(oneCtorArgSig),
        "a one-arg constructor is within Constructor#newInstance(Object[])'s one-argument call "
            + "site and must still resolve (this is the Xalan/Luindex shape)");
    assertTrue(
        cg.containsMethod(oneArgCtorTouchSig)
            && cg.callTargetsFrom(oneCtorArgSig).contains(oneArgCtorTouchSig),
        "the resolved constructor's body must actually be analyzed");
  }

  @Test
  public void methodExceedingCallSiteArityIsNotWired() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();

    assertTrue(
        cg.containsMethod(tooManyMethodParamsCtorSig),
        "sanity check: the no-arg constructor for TooManyMethodParams must resolve normally");
    assertFalse(
        cg.containsMethod(bigMethodSig),
        "Method#invoke(Object, Object[]) supplies exactly two syntactic arguments; a three-param "
            + "method must not be wired, or IFDSCallSemantics.buildSplitCall crashes indexing "
            + "past it");
    assertFalse(
        cg.containsMethod(bigMethodTouchSig),
        "bigMethodTouch() is only reachable through the never-should-be-wired method");
  }
}
