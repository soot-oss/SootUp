package sootup.java.core.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;

class AnnotationUsageVisibilityTest {
  private final JavaClassType type = new JavaIdentifierFactory().getClassType("example.Marker");

  @Test
  void existingConstructorDefaultsToRuntimeVisible() {
    AnnotationUsage usage = new AnnotationUsage(type, Map.of());
    assertTrue(usage.isRuntimeVisible());
    assertEquals(new AnnotationUsage(type, Map.of(), true), usage);
  }

  @Test
  void visibilityParticipatesInEqualityAndHashCollections() {
    AnnotationUsage visible = new AnnotationUsage(type, Map.of("value", "same"), true);
    AnnotationUsage invisible = new AnnotationUsage(type, Map.of("value", "same"), false);
    AnnotationUsage invisibleCopy = new AnnotationUsage(type, Map.of("value", "same"), false);
    assertFalse(invisible.isRuntimeVisible());
    assertNotEquals(visible, invisible);
    assertEquals(invisible, invisibleCopy);
    assertEquals(invisible.hashCode(), invisibleCopy.hashCode());
    assertEquals(2, new HashSet<>(List.of(visible, invisible, invisibleCopy)).size());
  }

  @Test
  void visibilityDoesNotChangeAnnotationTextOrValues() {
    Map<String, Object> values = Map.of("value", "same");
    AnnotationUsage visible = new AnnotationUsage(type, values, true);
    AnnotationUsage invisible = new AnnotationUsage(type, values, false);
    assertEquals(visible.toString(), invisible.toString());
    assertEquals(values, invisible.getValues());
    assertThrows(UnsupportedOperationException.class, () -> invisible.getValues().clear());
  }
}
