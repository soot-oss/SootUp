package de.upb.soot.util;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
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
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collection of common functionality that doesn't correspond to a class defined in the {@link
 * de.upb.soot} package.
 *
 * <p>If too much methods of the same kind are gathering in this class, consider a specialized Utils
 * class.
 *
 * @author Manuel Benz created on 07.06.18
 * @author Andreas Dann
 * @author Jan Martin Persch
 */
public class Utils {

  // region Stream

  /** Converts an {@link Optional} to a {@link Stream}. */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Nonnull
  public static <T> Stream<T> optionalToStream(@Nonnull Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }

  /** Converts an {@link Iterable} to a {@link Stream}. */
  @Nonnull
  public static <T> Stream<T> iterableToStream(@Nonnull Iterable<T> it) {
    return iterableToStream(it, false);
  }

  /** Converts an {@link Iterable} to a {@link Stream}. */
  @Nonnull
  public static <T> Stream<T> iterableToStream(@Nonnull Iterable<T> it, boolean parallel) {
    return StreamSupport.stream(it.spliterator(), parallel);
  }

  /** Converts an {@link Iterator} to a {@link Stream}. */
  @Nonnull
  public static <T> Stream<T> iteratorToStream(@Nonnull Iterator<T> it) {
    return iteratorToStream(it, false);
  }

  /** Converts an {@link Iterator} to a {@link Stream}. */
  @Nonnull
  public static <T> Stream<T> iteratorToStream(@Nonnull Iterator<T> it, boolean parallel) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), parallel);
  }

  /**
   * Filters and converts all objects from a stream that are instances of the specified class.
   *
   * <p>Example:
   *
   * <pre><code>
   * List&lt;Foo&gt; foosWithName =
   *     filterAllCasted(collection.stream(), Foo.class)
   *         .filter(it -> !it.getName().isEmpty());
   * </code></pre>
   *
   * @param stream The {@link Stream} to filter.
   * @param clazz The class to cast to.
   * @param <C> The type of the casted object.
   * @return The specified <i>stream</i>.
   * @see Functional#tryCastTo(Class)
   */
  @Nonnull
  public static <C> Stream<C> filterAllCasted(@Nonnull Stream<?> stream, @Nonnull Class<C> clazz) {
    return stream.filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Returns the <i>value</i>, if it is not <tt>null</tt>; otherwise, it returns <i>other</i>.
   *
   * @param value The value to get, if it is not <tt>null</tt>.
   * @param other The other to get, if <i>value</i> is <tt>null</tt>.
   * @param <T> The type of the value.
   * @return <i>value</i>, if it is not <tt>null</tt>; otherwise, <i>other</i>.
   * @see Optional#orElse(Object)
   */
  @Nonnull
  public static <T> T valueOrElse(@Nullable T value, @Nonnull T other) {
    return value != null ? value : other;
  }

  /**
   * Performs the provided action on the specified element and returns the element.
   *
   * @param element The element to peek.
   * @param action The action to perform.
   * @param <T> The type of the element.
   * @return The specified element.
   * @see Stream#peek(Consumer)
   */
  @Nonnull
  public static <T> T peek(@Nonnull T element, @Nonnull Consumer<? super T> action) {
    action.accept(element);

    return element;
  }

  // endregion /Stream/

  /** Removes the oldValue from the set and adds the newValue afterwards. */
  public static <T> void replace(Set<T> set, T oldValue, T newValue) {
    set.remove(oldValue);
    set.add(newValue);
  }

  // region Immutable Lists

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

  // endregion /Immutable Lists/

  // region Set and EnumSet Creator Methods

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

  // endregion /Set and EnumSet Creator Methods/

  /** Provides functional utilities. */
  public static final class Functional {

    /**
     * Gets a {@link Function} that tries to cast an object to the specified class in one step. Use
     * this method in combination with {@link Optional#flatMap(Function)}.
     *
     * <p><b>Note:</b> It is not recommended to apply this method on {@link Stream streams}. For
     * streams, you may use {@link #filterAllCasted(Stream, Class) filterAllCasted(…)}.
     *
     * <h2>Example</h2>
     *
     * The following code shows an example how to apply this method:
     *
     * <pre><code>
     * Optional&lt;Foo&gt; mayBeFoo = anOptional.flatMap(tryCastTo(Foo.class));
     * </code></pre>
     *
     * This spares the two-operational <i>type check and type cast</i>, as shown in the following
     * conventional example code:
     *
     * <pre><code>
     * Optional&lt;Foo&gt; mayBeFoo =
     *     someOptional
     *         .filter(SootClass.class::isInstance)
     *         .map(SootClass.class::cast)
     * </code></pre>
     *
     * <h2>Why not to apply on streams?</h2>
     *
     * If you want to apply this try-cast method to a stream, you would have to filter out the empty
     * optionals and to unbox the values, as shown in the following example code.
     *
     * <pre><code>
     * Stream&lt;Foo&gt; mayContainFoos =
     *     collection.stream()
     *         .map(tryCastTo(Foo.class))
     *         .filter(Optional::isPresent)
     *         .map(Optional::get)
     * </code></pre>
     *
     * It is obviously that this will produce a lot of overhead, thus it is recommended either to
     * use the {@link #filterAllCasted(Stream, Class) filterAllCasted(…)} helper method or to use
     * the conventional way when dealing with streams:
     *
     * <pre><code>
     * Stream&lt;Foo&gt; mayContainFoos =
     *     collection.stream()
     *         .filter(SootClass.class::isInstance)
     *         .map(SootClass.class::cast)
     * </code></pre>
     *
     * @param clazz The class to cast to.
     * @param <C> The type of the casted object.
     * @return The cast function.
     * @see #filterAllCasted(Stream, Class)
     */
    @Nonnull
    public static <C> Function<Object, Optional<C>> tryCastTo(@Nonnull Class<C> clazz) {
      return c -> clazz.isInstance(c) ? Optional.of(clazz.cast(c)) : Optional.empty();
    }
  }
}
