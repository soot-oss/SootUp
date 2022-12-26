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

package qilin.pta.toolkits.conch;

import qilin.core.pag.Node;

/*
 * This is the path edge defined as the standard in IFDS algorithm.
 * */
public class PathEdge {
    final Node srcNode;
    final DFA.State srcState;
    final Node tgtNode;
    final DFA.State tgtState;
    final int hashCode;

    public PathEdge(Node srcNode, DFA.State srcState, Node tgtNode, DFA.State tgtState) {
        this.srcNode = srcNode;
        this.srcState = srcState;
        this.tgtNode = tgtNode;
        this.tgtState = tgtState;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((srcNode == null) ? 0 : srcNode.hashCode());
        result = prime * result + ((srcState == null) ? 0 : srcState.hashCode());
        result = prime * result + ((tgtNode == null) ? 0 : tgtNode.hashCode());
        result = prime * result + ((tgtState == null) ? 0 : tgtState.hashCode());
        this.hashCode = result;
    }

    public Node getSrcNode() {
        return srcNode;
    }

    public DFA.State getSrcState() {
        return srcState;
    }

    public Node getTgtNode() {
        return tgtNode;
    }

    public DFA.State getTgtState() {
        return tgtState;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathEdge other = (PathEdge) obj;
        if (srcNode == null) {
            if (other.srcNode != null)
                return false;
        } else if (!srcNode.equals(other.srcNode))
            return false;
        if (tgtNode == null) {
            if (other.tgtNode != null)
                return false;
        } else if (!tgtNode.equals(other.tgtNode))
            return false;
        if (srcState == null) {
            if (other.srcState != null)
                return false;
        } else if (!srcState.equals(other.srcState))
            return false;
        if (tgtState == null) {
            return other.tgtState == null;
        } else return tgtState.equals(other.tgtState);
    }

    @Override
    public String toString() {
        return "(" + srcNode + "," + srcState + ")\n\t" + "-->" + "(" + tgtNode + "," + tgtState + ")";
    }
}
