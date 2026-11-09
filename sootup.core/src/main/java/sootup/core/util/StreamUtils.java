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

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class StreamUtils {
  /** Converts an {@link Optional} to a {@link Stream}. */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @NonNull
  public static <T> Stream<T> optionalToStream(@NonNull Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }

  /** Converts an {@link Iterable} to a {@link Stream}. */
  @NonNull
  public static <T> Stream<T> iterableToStream(@NonNull Iterable<T> it) {
    return iterableToStream(it, false);
  }

  /** Converts an {@link Iterable} to a {@link Stream}. */
  @NonNull
  public static <T> Stream<T> iterableToStream(@NonNull Iterable<T> it, boolean parallel) {
    return StreamSupport.stream(it.spliterator(), parallel);
  }

  /** Converts an {@link Iterator} to a {@link Stream}. */
  @NonNull
  public static <T> Stream<T> iteratorToStream(@NonNull Iterator<T> it) {
    return iteratorToStream(it, false);
  }

  /** Converts an {@link Iterator} to a {@link Stream}. */
  @NonNull
  public static <T> Stream<T> iteratorToStream(@NonNull Iterator<T> it, boolean parallel) {
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
   *         .filter(it -&gt; !it.getName().isEmpty());
   * </code></pre>
   *
   * @param stream The {@link Stream} to filter.
   * @param clazz The class to cast to.
   * @param <C> The type of the casted object.
   * @return The specified <i>stream</i>.
   */
  @NonNull
  public static <C> Stream<C> filterAllCasted(@NonNull Stream<?> stream, @NonNull Class<C> clazz) {
    return stream.filter(clazz::isInstance).map(clazz::cast);
  }

  /**
   * Returns the <i>value</i>, if it is not {@code null}; otherwise, it returns <i>other</i>.
   *
   * @param value The value to get, if it is not {@code null}.
   * @param other The other to get, if <i>value</i> is {@code null}.
   * @param <T> The type of the value.
   * @return <i>value</i>, if it is not {@code null}; otherwise, <i>other</i>.
   * @see Optional#orElse(Object)
   */
  @NonNull
  public static <T> T valueOrElse(@Nullable T value, @NonNull T other) {
    return value != null ? value : other;
  }
}
