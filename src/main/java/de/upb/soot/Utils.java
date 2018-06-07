package de.upb.soot;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Manuel Benz created on 07.06.18
 */
public class Utils {
  public static <T> Stream<T> optionalToStream(Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }

  public static <T> Stream<T> iteratorToStream(Iterator<T> it) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
  }
}
