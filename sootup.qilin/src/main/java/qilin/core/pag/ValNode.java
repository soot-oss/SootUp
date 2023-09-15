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

package qilin.core.pag;

import qilin.core.sets.DoublePointsToSet;
import soot.Type;
import soot.util.Numberable;

/**
 * Represents a simple of pointer node in the pointer assignment graph.
 */
public class ValNode extends Node implements Comparable, Numberable {

    protected ValNode(Type t) {
        super(t);
    }

    public int compareTo(Object o) {
        ValNode other = (ValNode) o;
        return other.getNumber() - this.getNumber();
    }

    /**
     * Returns the points-to set for this node.
     */
    public DoublePointsToSet getP2Set() {
        if (p2set != null) {
            return p2set;
        } else {
            p2set = new DoublePointsToSet();
            return p2set;
        }
    }

    /**
     * Delete current points-to set and make a new one
     */
    public void discardP2Set() {
        p2set = null;
    }

}
