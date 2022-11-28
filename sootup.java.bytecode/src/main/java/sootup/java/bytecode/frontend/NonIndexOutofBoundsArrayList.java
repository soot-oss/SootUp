package sootup.java.bytecode.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Markus Schmidt
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

import java.util.*;

/**
 * a modified ArrayList which grows instead of throwing IndexOutOfBounds and behaves more like a
 * (growing) array.
 *
 * @author Markus Schmidt
 */
public class NonIndexOutofBoundsArrayList<T> extends ArrayList<T> {

  public NonIndexOutofBoundsArrayList(int i) {
    super(i);
  }

  /** returns null instead of IndexOutfBoundsException */
  @Override
  public T get(int idx) {
    final int size = size();
    if (idx >= size) {
      return null;
    }
    return super.get(idx);
  }

  /**
   * modified in the way that the underlying array grows if index &gt;= size() and fills the gap
   * with null elements instead of throwing an IndexOutOfBoundsException
   */
  @Override
  public T set(int idx, T t) {
    final int size = size();
    if (idx >= size) {
      // copy backing array just once instead of multiple times depending on the loop
      ensureCapacity(idx);
      // fill gaps with empty values
      for (int i = size; i < idx; i++) {
        add(null);
      }
      // finally add what we want to set to index idx
      add(t);
      return null;
    } else {
      return super.set(idx, t);
    }
  }
}
