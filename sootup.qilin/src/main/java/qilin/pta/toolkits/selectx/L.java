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

import qilin.core.pag.LocalVarNode;
import qilin.util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * locals
 */
public class L extends I {
    public static Map<LocalVarNode, L> l2LN = new HashMap<>();
    public static Map<LocalVarNode, L> l2LP = new HashMap<>();

    public static L v(LocalVarNode origin, boolean positive) {
        if (positive) {
            return l2LP.computeIfAbsent(origin, k -> new L(origin, positive));
        } else {
            return l2LN.computeIfAbsent(origin, k -> new L(origin, positive));
        }
    }

    private final Set<G> outGs = new HashSet<>();
    private final boolean positive;
    private final Map<Integer, Set<L>> outEntryEdges = new HashMap<>();
    private final Map<Integer, Set<L>> outExitEdges = new HashMap<>();
    private final Map<Integer, Set<L>> inEntryEdges = new HashMap<>();

    L(LocalVarNode origin, boolean positive) {
        super(origin);
        this.positive = positive;
    }

    public boolean addOutEdge(G to) {
        return outGs.add(to);
    }

    public Stream<G> getOutGs() {
        return outGs.stream();
    }

    @Override
    public boolean addOutEdge(BNode toE) {
        if (toE instanceof G)
            return addOutEdge((G) toE);
        return super.addOutEdge(toE);
    }

    public boolean addOutEntryEdge(int i, L toE) {
        return Util.addToMap(outEntryEdges, i, toE);
    }

    public boolean addOutExitEdge(int i, L toE) {
        return Util.addToMap(outExitEdges, i, toE);
    }

    public Set<L> getOutEntryEdges() {
        return outEntryEdges.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<Map.Entry<Integer, Set<L>>> getOutExitEdges() {
        return outExitEdges.entrySet();
    }

    public boolean addInEntryEdge(int i, L fromE) {
        return Util.addToMap(inEntryEdges, i, fromE);
    }

    public Set<L> getInEntryEdges(int i) {
        return inEntryEdges.getOrDefault(i, Collections.emptySet());
    }

    @Override
    public Stream<? extends BNode> forwardTargets() {
        Stream<? extends BNode> exits = outExitEdges.values().stream().flatMap(Collection::stream);
        Stream<? extends BNode> outs = Stream.concat(exits, outGs.stream());
        return Stream.concat(outs, super.forwardTargets());
    }

    /*
     * inverse operation: inv(l-) = l+ and inv(l+) = l-.
     * */
    public L inv() {
        return L.v((LocalVarNode) sparkNode, !positive);
    }
}
