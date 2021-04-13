package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.java.core.types.AnnotationType;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * This class models Annotations
 *
 * @author Markus Schmidt
 */
public class AnnotationUsage {

  @Nonnull private final AnnotationType annotation;
  @Nonnull private final Map<String, Constant> values;

  public AnnotationUsage(
      @Nonnull AnnotationType annotation, @Nonnull Map<String, Constant> values) {
    this.annotation = annotation;
    this.values = values;
  }

  @Nonnull
  public AnnotationType getAnnotation() {
    return annotation;
  }

  @Nonnull
  public Map<String, Immediate> getValues() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnnotationUsage that = (AnnotationUsage) o;
    return annotation.equals(that.annotation) && values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(annotation, values);
  }
}
