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

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record Triple<T1, T2, T3>(T1 first, T2 second, T3 third) {

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Triple<?, ?, ?> anoTriple) {
      return Objects.equals(this.first, anoTriple.first)
          && Objects.equals(this.second, anoTriple.second)
          && Objects.equals(this.third, anoTriple.third);
    }
    return false;
  }

  @Override
  @NonNull
  public String toString() {
    return "<" + this.first + ", " + this.second + ", " + this.third + ">";
  }
}
