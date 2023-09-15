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
import qilin.util.PTAUtils;
import soot.Type;
import soot.util.Numberable;

/**
 * Represents every node in the pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class Node implements Numberable {
    protected Type type;
    protected DoublePointsToSet p2set;
    private int number = 0;

    /**
     * Creates a new node of pointer assignment graph pag, with type type.
     */
    protected Node(Type type) {
        if (PTAUtils.isUnresolved(type)) {
            throw new RuntimeException("Unresolved type " + type);
        }
        this.type = type;
    }

    @Override
    public final int hashCode() {
        return number;
    }

    public final boolean equals(Object other) {
        return this == other;
    }

    /**
     * Returns the declared type of this node, null for unknown.
     */
    public Type getType() {
        return type;
    }

    public final int getNumber() {
        return number;
    }

    public final void setNumber(int number) {
        this.number = number;
    }
}
