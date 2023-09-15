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

package qilin.core.context;

import soot.Context;

import java.util.Arrays;

public class ContextElements implements Context {
    private final ContextElement[] array;
    private final int size;
    private int hashCode = 0;

    public ContextElements(ContextElement[] array, int s) {
        this.array = array;
        this.size = s;
    }

    public ContextElement[] getElements() {
        return array;
    }

    public ContextElement get(int i) {
        return array[i];
    }

    public boolean contains(ContextElement heap) {
        for (int i = 0; i < size; ++i) {
            if (array[i] == heap) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.hashCode(array);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        ContextElements other = (ContextElements) obj;

        // if (!Arrays.equals(array, other.array)) return false;

        // custom array equals for context
        // allows for checking of different sized arrays, but represent same
        // context-sensitive heap object
        if (this.array == null || other.array == null)
            return false;

        if (this.size() != other.size())
            return false;

        for (int i = 0; i < size(); i++) {
            Object o1 = this.array[i];
            Object o2 = other.array[i];
            if (!(o1 == null ? o2 == null : o1.equals(o2)))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append('[');
        for (int i = 0; i < array.length; i++) {
            localStringBuilder.append(array[i]);
            if (i < array.length - 1) {
                localStringBuilder.append(", ");
            }
        }
        localStringBuilder.append(']');
        return localStringBuilder.toString();
    }

    /**
     * Compose a new context by a given context and a heap.
     * The depth of new context is also required.
     */
    public static ContextElements newContext(ContextElements c, ContextElement ce, int depth) {
        ContextElement[] array;
        if (c.size() < depth) {
            array = new ContextElement[c.size() + 1];
            System.arraycopy(c.getElements(), 0, array, 1, c.size());
        } else {
            array = new ContextElement[depth];
            System.arraycopy(c.getElements(), 0, array, 1, depth - 1);
        }
        array[0] = ce;
        return new ContextElements(array, array.length);
    }
}
