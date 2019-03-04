package de.upb.soot.util;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 07.06.2018 Manuel Benz
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

/**
 * Collection of common functionality that doesn't correspond to a class defined in the {@link
 * de.upb.soot} package.
 *
 * <p>If too much methods of the same kind are gathering in this class, consider a specialized Utils
 * class.
 *
 * @author Manuel Benz created on 07.06.18
 * @author Andreas Dann
 */
public class Utils {

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> Stream<T> optionalToStream(Optional<T> o) {
    return o.map(Stream::of).orElseGet(Stream::empty);
  }

  public static <T> Stream<T> iteratorToStream(Iterator<T> it) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
  }

  /**
   * Turns an Optional<T> into a Stream<T> of length zero or one depending upon whether a value is
   * present.
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static <T> Stream<T> streamOpt(Optional<T> opt) {
    return opt.map(Stream::of).orElseGet(Stream::empty);
  }
}
