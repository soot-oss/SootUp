package qilin.pta.toolkits.moon.support;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.PagNode;

public class PtrSetCache {
  private final PTA pta;
  private final Map<PagNode, Set<AllocNode>> cache = new ConcurrentHashMap<>();

  public PtrSetCache(PTA pta) {
    this.pta = pta;
  }

  public Set<AllocNode> ptsOf(PagNode node) {
    return cache.computeIfAbsent(
        node,
        n ->
            Collections.unmodifiableSet(
                new HashSet<>(pta.reachingObjects(n).toCIPointsToSet().toCollection())));
  }
}
