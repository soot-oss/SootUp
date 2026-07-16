/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record Pair<T1, T2>(T1 first, T2 second) {

  @Override
  public boolean equals(Object o) {
    if (o instanceof Pair<?, ?> anoPair) {
      return Objects.equals(first, anoPair.first) && Objects.equals(second, anoPair.second);
    }
    return false;
  }

  @Override
  @NonNull
  public String toString() {
    return "<" + first + ", " + second + ">";
  }
}
