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

package qilin.pta.toolkits.selectx;

import java.util.*;
import java.util.stream.Stream;

import qilin.core.pag.GlobalVarNode;

public class G extends BNode {
    public static Map<GlobalVarNode, G> g2GN = new HashMap<>();
    public static Map<GlobalVarNode, G> g2GP = new HashMap<>();

    public static G v(GlobalVarNode origin, boolean positive) {
        if (positive) {
            return g2GP.computeIfAbsent(origin, k -> new G(origin, true));
        } else {
            return g2GN.computeIfAbsent(origin, k -> new G(origin, false));
        }
    }

    private final Set<L> outLs = new HashSet<>();
    private final boolean positive;

    G(GlobalVarNode origin, boolean positive) {
        super(origin);
        this.positive = positive;
    }

    public boolean addOutEdge(L toE) {
        return outLs.add(toE);
    }

    @Override
    public boolean addOutEdge(BNode toE) {
        return addOutEdge((L) toE);
    }

    @Override
    public Stream<? extends BNode> forwardTargets() {
        return outLs.stream();
    }
}
