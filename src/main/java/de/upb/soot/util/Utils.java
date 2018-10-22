package de.upb.soot.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Collection of common functionality that doesn't correspond to a class defined in the {@link de.upb.soot} package.
 * <p>
 * If too much methods of the same kind are gathering in this class, consider a specialized Utils class.
 * </p>
 *
 * @author Manuel Benz created on 07.06.18
 * @author Andreas Dann
 */
public class Utils {
  public static <T> Stream<T> optionalToStream(Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }

  public static <T> Stream<T> iteratorToStream(Iterator<T> it) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
  }

  /**
   * Turns an Optional<T> into a Stream<T> of length zero or one depending upon whether a value is present.
   */
  static <T> Stream<T> streamOpt(Optional<T> opt) {
    if (opt.isPresent())
      return Stream.of(opt.get());
    else
      return Stream.empty();
  }
}