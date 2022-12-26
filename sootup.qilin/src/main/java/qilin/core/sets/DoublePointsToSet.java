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
 * Implementation of points-to set that holds two sets: one for new elements that have not yet been propagated, and the other
 * for elements that have already been propagated.
 *
 * @author Ondrej Lhotak
 */
public class DoublePointsToSet extends PointsToSetInternal {
    protected HybridPointsToSet newSet;
    protected HybridPointsToSet oldSet;

    public DoublePointsToSet() {
        newSet = new HybridPointsToSet();
        oldSet = new HybridPointsToSet();
    }

    /**
     * Returns true if this set contains no run-time objects.
     */
    @Override
    public boolean isEmpty() {
        return oldSet.isEmpty() && newSet.isEmpty();
    }

    /**
     * Returns true if this set shares some objects with other.
     */
    public boolean hasNonEmptyIntersection(PointsToSetInternal other) {
        return oldSet.hasNonEmptyIntersection(other) || newSet.hasNonEmptyIntersection(other);
    }

    @Override
    public int size() {
        return oldSet.size() + newSet.size();
    }

    private class DoublePTSIterator implements Iterator<Integer> {
        private final Iterator<Integer> oldIt = oldSet.iterator();
        private final Iterator<Integer> newIt = newSet.iterator();

        @Override
        public boolean hasNext() {
            return oldIt.hasNext() || newIt.hasNext();
        }

        @Override
        public Integer next() {
            if (oldIt.hasNext()) {
                return oldIt.next();
            } else {
                return newIt.next();
            }
        }
    }

    public Iterator<Integer> iterator() {
        return new DoublePTSIterator();
    }

    /*
     * Empty this set.
     * */
    @Override
    public void clear() {
        oldSet.clear();
        newSet.clear();
    }

    /**
     * Adds contents of other into this set, returns true if this set changed.
     */
    public boolean addAll(PointsToSetInternal other, PointsToSetInternal exclude) {
        if (exclude != null) {
            throw new RuntimeException("exclude set must be null.");
        }
        return newSet.addAll(other, oldSet);
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    @Override
    public boolean forall(P2SetVisitor v) {
        oldSet.forall(v);
        newSet.forall(v);
        return v.getReturnValue();
    }

    /**
     * Adds n to this set, returns true if idx was not already in this set.
     */
    public boolean add(int idx) {
        if (oldSet.contains(idx)) {
            return false;
        }
        return newSet.add(idx);
    }

    /**
     * Returns set of nodes already present before last call to flushNew.
     */
    public HybridPointsToSet getOldSet() {
        return oldSet;
    }

    /**
     * Returns set of newly-added nodes since last call to flushNew.
     */
    public HybridPointsToSet getNewSet() {
        return newSet;
    }

    public HybridPointsToSet getNewSetCopy() {
        HybridPointsToSet newCopy = new HybridPointsToSet();
        newCopy.addAll(newSet, null);
        return newCopy;
    }

    /**
     * Sets all newly-added nodes to old nodes.
     */
    public void flushNew() {
        oldSet.addAll(newSet, null);
        newSet = new HybridPointsToSet();
    }

    /**
     * Returns true iff the set contains idx.
     */
    @Override
    public boolean contains(int idx) {
        return oldSet.contains(idx) || newSet.contains(idx);
    }
}
