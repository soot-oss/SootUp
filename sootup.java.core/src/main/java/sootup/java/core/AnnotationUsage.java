package sootup.java.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import sootup.java.core.types.AnnotationType;

/**
 * This class models Annotations
 *
 * @author Markus Schmidt
 */
public class AnnotationUsage {

  @Nonnull private final AnnotationType annotation;
  @Nonnull private final Map<String, Object> values;
  private Map<String, Object> valuesWithDefaults;

  public AnnotationUsage(@Nonnull AnnotationType annotation, @Nonnull Map<String, Object> values) {
    this.annotation = annotation;
    this.values = values;
  }

  @Nonnull
  public AnnotationType getAnnotation() {
    return annotation;
  }

  @Nonnull
  public Map<String, Object> getValues() {
    return Collections.unmodifiableMap(values);
  }

  @Nonnull
  public Map<String, Object> getValuesWithDefaults() {
    if (valuesWithDefaults == null) {
      valuesWithDefaults = new HashMap<>(annotation.getDefaultValues(Optional.empty()));
      values.forEach((k, v) -> valuesWithDefaults.put(k, v));
    }

    return Collections.unmodifiableMap(valuesWithDefaults);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("@").append(annotation);
    if (!getValuesWithDefaults().isEmpty()) {
      sb.append("(");
      getValuesWithDefaults().forEach((k, v) -> sb.append(k).append("=").append(v).append(","));
      sb.setCharAt(sb.length() - 1, ')');
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnnotationUsage that = (AnnotationUsage) o;

    return annotation.equals(that.annotation) && this.values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(annotation, values);
  }
}
