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

package qilin.parm.heapabst;

import qilin.core.pag.AllocNode;
import qilin.core.pag.PAG;
import qilin.util.PTAUtils;
import soot.SootMethod;
import soot.Type;

import java.util.Map;
import java.util.Set;

public class MahjongAbstractor implements HeapAbstractor {
    private final Set<Object> mergedHeap;
    private final Map<Object, Object> heapModelMap;
    private final PAG pag;

    public MahjongAbstractor(PAG pag, Set<Object> mergedHeap, Map<Object, Object> heapModelMap) {
        this.pag = pag;
        this.mergedHeap = mergedHeap;
        this.heapModelMap = heapModelMap;
    }

    @Override
    public AllocNode abstractHeap(AllocNode heap) {
        Object newExpr = heap.getNewExpr();
        Type type = heap.getType();
        SootMethod m = heap.getMethod();
        Object mergedIr = this.heapModelMap.get(newExpr);
        if (this.mergedHeap.contains(mergedIr)) {
            return pag.makeAllocNode(mergedIr, type, null);
        } else {
            if (PTAUtils.isThrowable(type)) {
                // Mahjong still needs heuristics to handle throwable types.
                return pag.makeAllocNode("Merged " + type, type, null);
            }
            return pag.makeAllocNode(newExpr, type, m);
        }
    }
}
