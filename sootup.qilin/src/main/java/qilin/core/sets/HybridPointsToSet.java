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

import soot.util.BitSetIterator;
import soot.util.BitVector;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Hybrid implementation of points-to set, which uses an explicit array for small sets, and a bit vector for large sets.
 *
 * @author Ondrej Lhotak
 */
public final class HybridPointsToSet extends PointsToSetInternal {
    private static HybridPointsToSet emptySet = null;

    public static HybridPointsToSet getEmptySet() {
        if (emptySet == null) {
            emptySet = new HybridPointsToSet();
        }
        return emptySet;
    }

    private final int[] nodeIdxs = new int[16];
    private BitVector bits = null;
    private int size = 0;

    private boolean empty = true;

    /**
     * Returns true if this set contains no run-time objects.
     */
    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public void clear() {
        Arrays.fill(nodeIdxs, 0);
        bits = null;
        empty = true;
        size = 0;
    }

    private boolean nativeAddAll(HybridPointsToSet other, PointsToSetInternal exclude) {
        boolean ret = false;
        for (Iterator<Integer> it = other.iterator(); it.hasNext(); ) {
            int idx = it.next();
            if (exclude == null || !exclude.contains(idx)) {
                ret |= add(idx);
            }
        }
        return ret;
    }

    /**
     * Adds contents of other into this set, returns true if this set changed.
     */
    public boolean addAll(final PointsToSetInternal other, final PointsToSetInternal exclude) {
        if (other == null) {
            return false;
        }
        if (other instanceof DoublePointsToSet dpts) {
            return nativeAddAll(dpts.getNewSet(), exclude) | nativeAddAll(dpts.getOldSet(), exclude);
        }
        return nativeAddAll((HybridPointsToSet) other, exclude);
    }

    private class HybridPTSIterator implements Iterator<Integer> {
        private BitSetIterator it;
        private int idx;

        public HybridPTSIterator() {
            if (bits == null) {
                idx = 0;
            } else {
                it = bits.iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (bits == null) {
                return idx < nodeIdxs.length && nodeIdxs[idx] != 0;
            } else {
                return it.hasNext();
            }
        }

        @Override
        public Integer next() {
            if (bits == null) {
                return nodeIdxs[idx++];
            } else {
                return it.next();
            }
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new HybridPTSIterator();
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Calls v's visit method on all nodes in this set.
     */
    public boolean forall(P2SetVisitor v) {
        if (bits == null) {
            for (int nodeIdx : nodeIdxs) {
                if (nodeIdx == 0) {
                    return v.getReturnValue();
                }
                v.visit(nodeIdx);
            }
        } else {
            for (BitSetIterator it = bits.iterator(); it.hasNext(); ) {
                v.visit(it.next());
            }
        }
        return v.getReturnValue();
    }

    /**
     * Returns true iff the set contains node idx.
     */
    public boolean contains(int idx) {
        if (bits == null) {
            for (int nodeIdx : nodeIdxs) {
                if (idx == nodeIdx) {
                    return true;
                }
                if (nodeIdx == 0) {
                    break;
                }
            }
            return false;
        } else {
            return bits.get(idx);
        }
    }

    /**
     * Adds idx to this set, returns true if idx was not already in this set.
     */
    public boolean add(int idx) {
        if (bits == null) {
            for (int i = 0; i < nodeIdxs.length; i++) {
                if (nodeIdxs[i] == 0) {
                    empty = false;
                    nodeIdxs[i] = idx;
                    ++size;
                    return true;
                } else if (nodeIdxs[i] == idx) {
                    return false;
                }
            }
            // convert to Bits
            bits = new BitVector();
            for (int nodeIdx : nodeIdxs) {
                if (nodeIdx != 0) {
                    bits.set(nodeIdx);
                }
            }
        }
        boolean ret = bits.set(idx);
        if (ret) {
            ++size;
            empty = false;
        }
        return ret;
    }

}
