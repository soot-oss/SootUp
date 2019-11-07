package de.upb.swt.soot.core.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StreamUtils {
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
}
