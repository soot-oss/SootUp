package qilin.pta.toolkits.moon.traversal;

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

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ConstantNode;
import qilin.core.pag.ContextAllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.support.PtrSetCache;

public class TraversalInitializer {

  private final MoonDataConstructor.MoonDataStructure moonData;
  private final int objContextLen;

  public TraversalInitializer(MoonDataConstructor.MoonDataStructure moonData, int objContextLen) {
    this.moonData = moonData;
    this.objContextLen = objContextLen;
  }

  public SetMultimap<AllocNode, LocalVarNode> initializeObjToVarMap() {
    Set<LocalVarNode> containerVars = moonData.vfgForField().getContainerVars();
    Set<AllocNode> containerObjs = moonData.containers();
    SetMultimap<AllocNode, LocalVarNode> containerObjToBaseVar =
        Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
    PtrSetCache ptrSetCache = moonData.ptrSetCache();

    Set<AllocNode> filteredContainerObjs =
        containerVars.parallelStream()
            .map(ptrSetCache::ptsOf)
            .flatMap(Collection::stream)
            .filter(
                o -> {
                  if (!containerObjs.contains(o)) return false;
                  if (!hasMultiContext(o)) return false;
                  if (o instanceof ContextAllocNode) {
                    throw new RuntimeException("ContextAllocNode detected!");
                  }
                  return !(o instanceof ConstantNode) && o.getMethod() != null;
                })
            .collect(Collectors.toSet());

    containerVars.parallelStream()
        .forEach(
            var -> {
              Set<AllocNode> pts = ptrSetCache.ptsOf(var);
              for (AllocNode obj : pts) {
                if (filteredContainerObjs.contains(obj)) {
                  containerObjToBaseVar.put(obj, var);
                }
              }
            });

    return containerObjToBaseVar;
  }

  public boolean hasMultiContext(AllocNode obj) {
    Set<AllocNode> crtObjs = new HashSet<>();
    crtObjs.add(obj);
    var oag = moonData.oag();
    for (int i = 0; i < this.objContextLen; i++) {
      Set<AllocNode> preds =
          crtObjs.stream().map(oag::getPredsOf).flatMap(Set::stream).collect(Collectors.toSet());
      if (preds.size() > 1) return true;
      crtObjs = preds;
    }
    return false;
  }
}
