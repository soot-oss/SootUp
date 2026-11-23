package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.test.SparkTestUtil;

public class VtaPresetTest {

  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("VtaPreset"),
          SparkTestUtil.idFactory.getMainSubSignature());

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  /** The classes whose area() the call in main reaches */
  private Set<String> areaTargets(SparkOptions options) {
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .sparkOptions(options)
        .build()
        .getCallGraph()
        .callTargetsFrom(mainSig)
        .stream()
        .filter(m -> m.getName().equals("area"))
        .map(m -> m.getDeclClassType().getClassName())
        .collect(Collectors.toSet());
  }

  @Test
  public void presetSetsTheOptionsOfVta() {
    SparkOptions vta = SparkOptions.vta();
    assertTrue(vta.isIgnoreBaseObjects());
    assertTrue(vta.isTypesForSites());
    assertFalse(vta.isOnFlyCallGraph());
  }

  @Test
  public void fieldBasedCallGraphWithoutUninstantiatedClasses() {
    // b1.shape and b2.shape are one field node, but Triangle is never created
    assertEquals(Set.of("VtaPreset$Circle", "VtaPreset$Square"), areaTargets(SparkOptions.vta()));
    // field-sensitive: only what b1.shape holds
    assertEquals(Set.of("VtaPreset$Circle"), areaTargets(SparkOptions.defaultOptions()));
  }
}
