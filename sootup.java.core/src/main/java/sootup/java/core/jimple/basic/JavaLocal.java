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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.Local;
import sootup.core.types.Type;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.HasAnnotation;

public class JavaLocal extends Local implements HasAnnotation {

  // TODO: [ms] add to JavaJimple
  // TODO: [ms] make use of this class in both Java Frontends

  @NonNull private final Iterable<AnnotationUsage> annotations;

  /**
   * Constructs a JavaLocal of the given name and type with an unknown/synthetic slot index (-1).
   *
   * @param name
   * @param type
   * @param annotations
   */
  public JavaLocal(
      @NonNull String name, @NonNull Type type, @NonNull Iterable<AnnotationUsage> annotations) {
    this(name, type, -1, annotations);
  }

  /**
   * Constructs a JavaLocal of the given name, type, bytecode slot index, and annotations.
   *
   * @param name
   * @param type
   * @param slotIndex
   * @param annotations
   */
  public JavaLocal(
      @NonNull String name,
      @NonNull Type type,
      int slotIndex,
      @NonNull Iterable<AnnotationUsage> annotations) {
    super(name, type, slotIndex);
    this.annotations = annotations;
  }

  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  @NonNull
  @Override
  public Local withName(@NonNull String name) {
    return new JavaLocal(name, getType(), getSlotIndex(), getAnnotations());
  }

  @NonNull
  @Override
  public Local withType(@NonNull Type type) {
    return new JavaLocal(getName(), type, getSlotIndex(), getAnnotations());
  }

  @NonNull
  public Local withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
    return new JavaLocal(getName(), getType(), getSlotIndex(), annotations);
  }

  /** Returns a copy of this JavaLocal with the given bytecode slot index. */
  @NonNull
  @Override
  public Local withSlotIndex(int slotIndex) {
    return new JavaLocal(getName(), getType(), slotIndex, getAnnotations());
  }

  /** Alias for {@link #withSlotIndex(int)}. */
  @NonNull
  @Override
  public Local withIndex(int slotIndex) {
    return withSlotIndex(slotIndex);
  }
}
