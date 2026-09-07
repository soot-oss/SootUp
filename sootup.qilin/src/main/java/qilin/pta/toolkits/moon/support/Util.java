package qilin.pta.toolkits.moon.support;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.Set;

public class Util {
  public static <E> boolean haveOverlap(Set<E> s1, Set<E> s2) {
    Set<E> small, large;
    if (s1.size() <= s2.size()) {
      small = s1;
      large = s2;
    } else {
      small = s2;
      large = s1;
    }
    for (E o : small) {
      if (large.contains(o)) {
        return true;
      }
    }
    return false;
  }
}
