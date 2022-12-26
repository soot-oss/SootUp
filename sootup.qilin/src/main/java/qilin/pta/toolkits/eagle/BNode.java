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

package qilin.pta.toolkits.eagle;

import qilin.core.pag.AllocNode;
import qilin.util.PTAUtils;

/**
 * Original Graph Node(sparkNode) expanded bidirectinally
 */
public class BNode {
    public Object sparkNode;
    public Boolean forward;
    public int level;
    public Boolean cs;

    public boolean entryCS() {
        if (this.cs)
            return false;
        return this.cs = true;
    }

    public BNode(Object origin, Boolean forward) {
        this.sparkNode = origin;
        this.forward = forward;
        this.cs = false;
        this.level = 0;
    }

    public Object getIR() {
        return PTAUtils.getIR(sparkNode);
    }

    boolean isHeapPlus() {
        return sparkNode instanceof AllocNode && this.forward;
    }

    boolean isHeapMinus() {
        return sparkNode instanceof AllocNode && !this.forward;
    }

    @Override
    public String toString() {
        return sparkNode + "," + forward;
    }
}