package sootup.core.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Hasitha Rajapakse
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ImmutableUtils {
  /** Returns an empty immutable list. */
  @Nonnull
  public static <E> ImmutableSet<E> emptyImmutableList() {
    return ImmutableSet.of();
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1) {
    return ImmutableList.of(e1);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2) {
    return ImmutableList.of(e1, e2);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3) {
    return ImmutableList.of(e1, e2, e3);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3, E e4) {
    return ImmutableList.of(e1, e2, e3, e4);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3, E e4, E e5) {
    return ImmutableList.of(e1, e2, e3, e4, e5);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3, E e4, E e5, E e6) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableList(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10, E e11) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @SafeVarargs
  @Nonnull
  public static <E> ImmutableList<E> immutableList(
      E e1,
      E e2,
      E e3,
      E e4,
      E e5,
      E e6,
      E e7,
      E e8,
      E e9,
      E e10,
      E e11,
      E e12,
      @Nonnull E... others) {
    return ImmutableList.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, others);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableListOf(@Nonnull E[] elements) {
    return ImmutableList.copyOf(elements);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @Nonnull
  public static <E> ImmutableList<E> immutableListOf(@Nonnull Iterable<? extends E> elements) {
    return ImmutableList.copyOf(elements);
  }

  /** Returns an immutable list containing the given elements, in order. */
  @SuppressWarnings("UnstableApiUsage")
  @Nonnull
  public static <E> ImmutableList<E> immutableListOf(@Nonnull Stream<? extends E> elements) {
    return elements.collect(ImmutableList.toImmutableList());
  }

  /** Returns an empty immutable set. */
  @Nonnull
  public static <E extends Enum<E>> ImmutableSet<E> emptyImmutableEnumSet() {
    return emptyImmutableSet();
  }

  /**
   * Returns an immutable set instance containing the given enum elements. Internally, the returned
   * set will be backed by an {@link EnumSet}.
   */
  @Nonnull
  public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSetOf(
      @Nonnull Iterable<E> elements) {
    return Sets.immutableEnumSet(elements);
  }

  /**
   * Returns an immutable set instance containing the given enum elements. Internally, the returned
   * set will be backed by an {@link EnumSet}.
   */
  @SafeVarargs
  @Nonnull
  public static <E extends Enum<E>> ImmutableSet<E> immutableEnumSet(
      @Nonnull E anElement, @Nonnull E... otherElements) {
    return Sets.immutableEnumSet(anElement, otherElements);
  }

  /** Returns an empty immutable set. */
  @Nonnull
  public static <E> ImmutableSet<E> emptyImmutableSet() {
    return ImmutableSet.of();
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1) {
    return ImmutableSet.of(e1);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2) {
    return ImmutableSet.of(e1, e2);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2, E e3) {
    return ImmutableSet.of(e1, e2, e3);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2, E e3, E e4) {
    return ImmutableSet.of(e1, e2, e3, e4);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2, E e3, E e4, E e5) {
    return ImmutableSet.of(e1, e2, e3, e4, e5);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2, E e3, E e4, E e5, E e6) {
    return ImmutableSet.of(e1, e2, e3, e4, e5, e6);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @SafeVarargs
  @Nonnull
  public static <E> ImmutableSet<E> immutableSet(E e1, E e2, E e3, E e4, E e5, E e6, E... others) {
    return ImmutableSet.of(e1, e2, e3, e4, e5, e6, others);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSetOf(@Nonnull E[] elements) {
    return ImmutableSet.copyOf(elements);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @Nonnull
  public static <E> ImmutableSet<E> immutableSetOf(@Nonnull Iterable<? extends E> elements) {
    return ImmutableSet.copyOf(elements);
  }

  /** Returns an immutable set instance containing the given enum elements. */
  @SuppressWarnings("UnstableApiUsage")
  @Nonnull
  public static <E> ImmutableSet<E> immutableSetOf(@Nonnull Stream<? extends E> elements) {
    return elements.collect(ImmutableSet.toImmutableSet());
  }

  public static final class ImmutableCollectors {
    @SuppressWarnings("UnstableApiUsage")
    @Nonnull
    public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
      return ImmutableSet.toImmutableSet();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Nonnull
    public static <E extends Enum<E>> Collector<E, ?, ImmutableSet<E>> toImmutableEnumSet() {
      return Sets.toImmutableEnumSet();
    }
  }
}
