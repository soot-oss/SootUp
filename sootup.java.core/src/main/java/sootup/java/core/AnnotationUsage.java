package sootup.java.core;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;

/**
 * This class models Annotations
 *
 * @author Markus Schmidt
 */
public class AnnotationUsage {

  @NonNull private final ClassType annotation;
  @NonNull private final Map<String, Object> values;

  public AnnotationUsage(@NonNull ClassType annotation, @NonNull Map<String, Object> values) {
    this.annotation = annotation;
    this.values = values;
  }

  /*
   * Rename to getType?
   */
  @NonNull
  public ClassType getAnnotation() {
    return annotation;
  }

  @NonNull
  public Map<String, Object> getValues() {
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

    return annotation.equals(that.annotation) && this.values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(annotation, values);
  }
}
