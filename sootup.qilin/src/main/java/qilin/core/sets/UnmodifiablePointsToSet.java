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

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ClassConstantNode;
import qilin.core.pag.Node;
import qilin.core.pag.StringConstantNode;
import soot.RefType;
import soot.Type;
import soot.jimple.ClassConstant;

import java.util.*;

public class UnmodifiablePointsToSet implements PointsToSet {
    private final PointsToSetInternal pts;
    private final PTA pta;

    public UnmodifiablePointsToSet(PTA pta, PointsToSetInternal pts) {
        this.pta = pta;
        this.pts = pts;
    }

    @Override
    public boolean isEmpty() {
        return pts.isEmpty();
    }

    @Override
    public boolean contains(AllocNode n) {
        return pts.contains(n.getNumber());
    }

    @Override
    public boolean hasNonEmptyIntersection(PointsToSet other) {
        if (other instanceof UnmodifiablePointsToSet uother) {
            return pta == uother.pta && pts.hasNonEmptyIntersection(uother.pts);
        }
        return false;
    }

    @Override
    public Set<Type> possibleTypes() {
        final Set<Type> ret = new HashSet<>();
        pts.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                Type t = n.getType();
                if (t instanceof RefType rt) {
                    if (rt.getSootClass().isAbstract()) {
                        return;
                    }
                }
                ret.add(t);
            }
        });
        return ret;
    }

    @Override
    public Set<String> possibleStringConstants() {
        final Set<String> ret = new HashSet<>();
        return pts.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                if (n instanceof StringConstantNode) {
                    ret.add(((StringConstantNode) n).getString());
                } else {
                    returnValue = true;
                }
            }
        }) ? null : ret;
    }

    @Override
    public Set<ClassConstant> possibleClassConstants() {
        final Set<ClassConstant> ret = new HashSet<>();
        return pts.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                if (n instanceof ClassConstantNode) {
                    ret.add(((ClassConstantNode) n).getClassConstant());
                } else {
                    returnValue = true;
                }
            }
        }) ? null : ret;
    }

    @Override
    public int size() {
        return pts.size();
    }

    @Override
    public void clear() {
        pts.clear();
    }

    @Override
    public String toString() {
        final StringBuffer ret = new StringBuffer();
        pts.forall(new P2SetVisitor(pta) {
            public void visit(Node n) {
                ret.append(n).append(",");
            }
        });
        return ret.toString();
    }

    @Override
    public int pointsToSetHashCode() {
        long intValue = 1;
        final long PRIME = 31;
        for (Iterator<Integer> it = pts.iterator(); it.hasNext(); ) {
            intValue = PRIME * intValue + it.next();
        }
        return (int) intValue;
    }

    @Override
    public boolean pointsToSetEquals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof UnmodifiablePointsToSet otherPts) {
            if (otherPts.pta != pta) {
                return false;
            }
            // both sets are equal if they are supersets of each other
            return superSetOf(otherPts.pts, pts) && superSetOf(pts, otherPts.pts);
        }
        return false;
    }

    @Override
    public PointsToSet toCIPointsToSet() {
        PointsToSetInternal ptoSet = new HybridPointsToSet();
        pts.forall(new P2SetVisitor(pta) {
            @Override
            public void visit(Node n) {
                AllocNode heap = (AllocNode) n;
                ptoSet.add(heap.base().getNumber());
            }
        });
        return new UnmodifiablePointsToSet(pta, ptoSet);
    }

    @Override
    public Collection<AllocNode> toCollection() {
        Set<AllocNode> ret = new HashSet<>();
        for (Iterator<AllocNode> it = iterator(); it.hasNext(); ) {
            ret.add(it.next());
        }
        return ret;
    }

    /**
     * Returns <code>true</code> if <code>onePts</code> is a (non-strict) superset of <code>otherPts</code>.
     */
    private boolean superSetOf(PointsToSetInternal onePts, final PointsToSetInternal otherPts) {
        Iterator<Integer> it = onePts.iterator();
        while (it.hasNext()) {
            int idx = it.next();
            if (!otherPts.contains(idx)) {
                return false;
            }
        }
        return true;
    }

    public Iterator<AllocNode> iterator() {
        return new UnmodifiablePTSIterator();
    }

    private class UnmodifiablePTSIterator implements Iterator<AllocNode> {
        Iterator<Integer> it = pts.iterator();

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public AllocNode next() {
            int idx = it.next();
            return pta.getPag().getAllocNodeNumberer().get(idx);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return pointsToSetEquals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointsToSetHashCode(), pta);
    }
}
