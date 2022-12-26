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

package qilin.util.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeImpl<N> implements Tree<N> {
    Map<N, TreeNode<N>> data2Node;
    TreeNode<N> root;

    public TreeImpl() {
        this.data2Node = new HashMap<>();
    }

    @Override
    public N getRoot() {
        if (root == null) {
            for (TreeNode<N> n : data2Node.values()) {
                if (n.isRoot()) {
                    root = n;
                    break;
                }
            }
        }
        return root != null ? root.getElem() : null;
    }

    @Override
    public Collection<N> getLeaves() {
        return getAllNodes().stream().filter(this::isALeaf).collect(Collectors.toSet());
    }

    @Override
    public boolean isALeaf(N o) {
        return data2Node.containsKey(o) && data2Node.get(o).isLeaf();
    }

    @Override
    public Collection<N> getAllNodes() {
        return data2Node.keySet();
    }

    public Collection<TreeNode<N>> getAllTreeNodes() {
        return data2Node.values();
    }

    @Override
    public int size() {
        return data2Node.size();
    }

    @Override
    public N parentOf(N o) {
        return data2Node.containsKey(o) ? data2Node.get(o).getParent().getElem() : null;
    }

    @Override
    public Collection<N> childrenOf(N o) {
        if (data2Node.containsKey(o)) {
            TreeNode<N> node = data2Node.get(o);
            return node.getChildren().stream().map(TreeNode::getElem).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public boolean addTreeEdge(N parent, N child) {
        TreeNode<N> p = data2Node.computeIfAbsent(parent, k -> new TreeNode<>(parent));
        TreeNode<N> c = data2Node.computeIfAbsent(child, k -> new TreeNode<>(child));
        boolean f = p.addChild(c);
        c.setParent(p);
        return f;
    }
}
