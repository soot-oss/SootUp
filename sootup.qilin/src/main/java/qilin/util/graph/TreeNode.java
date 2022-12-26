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

import java.util.HashSet;
import java.util.Set;

public class TreeNode<D> {
    private final D elem;
    private TreeNode<D> parent;
    private final Set<TreeNode<D>> children;

    public TreeNode(D e) {
        this.elem = e;
        this.children = new HashSet<>();
    }

    public D getElem() {
        return elem;
    }

    public TreeNode<D> getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public void setParent(TreeNode<D> parent) {
        this.parent = parent;
    }

    public Set<TreeNode<D>> getChildren() {
        return children;
    }

    public boolean addChild(TreeNode<D> child) {
        return this.children.add(child);
    }
}
