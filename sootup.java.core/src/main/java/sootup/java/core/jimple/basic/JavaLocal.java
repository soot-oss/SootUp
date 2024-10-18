package sootup.java.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.types.Type;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.HasAnnotation;

public class JavaLocal extends Local implements HasAnnotation {

  // TODO: [ms] add to JavaJimple
  // TODO: [ms] make use of this class in both Java Frontends

  @Nonnull private final Iterable<AnnotationUsage> annotations;

  /**
   * Constructs a JimpleLocal of the given name and type.
   *
   * @param name
   * @param type
   */
  public JavaLocal(
      @Nonnull String name, @Nonnull Type type, @Nonnull Iterable<AnnotationUsage> annotations) {
    super(name, type);
    this.annotations = annotations;
  }

  @Nonnull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  @Nonnull
  public Local withName(@Nonnull String name) {
    return new JavaLocal(name, getType(), getAnnotations());
  }

  @Nonnull
  public Local withType(@Nonnull Type type) {
    return new JavaLocal(getName(), type, getAnnotations());
  }

  @Nonnull
  public Local withAnnotations(@Nonnull Iterable<AnnotationUsage> annotations) {
    return new JavaLocal(getName(), getType(), annotations);
  }
}
