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

package qilin.core.sets;

import java.util.Iterator;

/**
 * Abstract base class for implementations of points-to sets.
 *
 * @author Ondrej Lhotak
 */
public abstract class PointsToSetInternal {
    /**
     * Calls v's visit method on all nodes in this set.
     */
    public abstract boolean forall(P2SetVisitor v);

    public abstract boolean addAll(final PointsToSetInternal other, final PointsToSetInternal exclude);

    /**
     * Adds node index idx to this set, returns true if idx was not already in this set.
     */
    public abstract boolean add(int idx);

    /**
     * Returns true iff the set contains the node number index.
     */
    public abstract boolean contains(int idx);

    public abstract Iterator<Integer> iterator();

    public abstract void clear();

    public abstract boolean isEmpty();

    public boolean hasNonEmptyIntersection(final PointsToSetInternal other) {
        Iterator<Integer> it = iterator();
        while (it.hasNext()) {
            int idx = it.next();
            if (other.contains(idx)) {
                return true;
            }
        }
        return false;
    }

    public abstract int size();
}
