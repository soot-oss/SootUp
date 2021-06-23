package de.upb.swt.soot.core.graph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2021 Zun Wang
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import java.util.HashSet;
import java.util.Set;

/*@author Zun Wang*/
public class DominanceTree {

  private Block content;
  private Set<DominanceTree> children = new HashSet<>();

  public DominanceTree(Block content) {
    this.content = content;
  }

  public Block getContent() {
    return this.content;
  }

  public Set<DominanceTree> getChildren() {
    return children;
  }

  public void addChild(DominanceTree child) {
    children.add(child);
  }

  public Block setContent(Block content) {
    return this.content;
  }

  public void replaceChild(DominanceTree newChild, DominanceTree oldChild) {
    children.remove(oldChild);
    children.add(newChild);
  }

  public void setChildren(Set<DominanceTree> children) {
    this.children = children;
  }
}
