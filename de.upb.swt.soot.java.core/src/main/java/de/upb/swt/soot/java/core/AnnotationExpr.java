package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.java.core.types.AnnotationType;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

public class AnnotationExpr {

  @Nonnull private final AnnotationType annotation;
  @Nonnull private final Map<String, Value> values;

  public AnnotationExpr(@Nonnull AnnotationType annotation, @Nonnull Map<String, Value> values) {
    this.annotation = annotation;
    this.values = values;
  }

  @Nonnull
  public AnnotationType getAnnotation() {
    return annotation;
  }

  @Nonnull
  public Map<String, Value> getValues() {
    return Collections.unmodifiableMap(values);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("@").append(annotation);
    if (!values.isEmpty()) {
      sb.append("(");
      values.forEach((k, v) -> sb.append(k).append("=").append(v).append(","));
      sb.setCharAt(sb.length() - 1, ')');
    }
    return sb.toString();
  }
}
