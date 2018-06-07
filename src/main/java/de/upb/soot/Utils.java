package de.upb.soot;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Manuel Benz created on 07.06.18
 */
public class Utils {
  public static <T> Stream<T> optionalToStream(Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }
}
