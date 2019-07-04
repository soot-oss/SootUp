package de.upb.soot.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public final class FunctionalUtils {
  /**
   * Gets a {@link Function} that tries to cast an object to the specified class in one step. Use
   * this method in combination with {@link Optional#flatMap(Function)}.
   *
   * <p><b>Note:</b> It is not recommended to apply this method on {@link Stream streams}. For
   * streams, you may use {@link StreamUtils#filterAllCasted(Stream, Class) filterAllCasted(…)}.
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
   * It is obviously that this will produce a lot of overhead, thus it is recommended either to use
   * the {@link StreamUtils#filterAllCasted(Stream, Class) filterAllCasted(…)} helper method or to
   * use the conventional way when dealing with streams:
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
   * @see StreamUtils#filterAllCasted(Stream, Class)
   */
  @Nonnull
  public static <C> Function<Object, Optional<C>> tryCastTo(@Nonnull Class<C> clazz) {
    return c -> clazz.isInstance(c) ? Optional.of(clazz.cast(c)) : Optional.empty();
  }
}
