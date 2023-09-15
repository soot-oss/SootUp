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
import qilin.core.pag.MergedNewExpr;
import qilin.core.pag.PAG;
import qilin.util.DataFactory;
import qilin.util.PTAUtils;
import soot.*;

import java.util.Set;

public class HeuristicAbstractor implements HeapAbstractor {
    private final PAG pag;
    private final Set<Type> mergedTypes = DataFactory.createSet();

    public HeuristicAbstractor(PAG pag) {
        this.pag = pag;
        mergedTypes.add(RefType.v("java.lang.StringBuffer"));
        mergedTypes.add(RefType.v("java.lang.StringBuilder"));
    }

    @Override
    public AllocNode abstractHeap(AllocNode heap) {
        Type type = heap.getType();
        SootMethod m = heap.getMethod();
        if (mergedTypes.contains(type) || (PTAUtils.isThrowable(type) && mergedTypes.add(type))) {
            return pag.makeAllocNode(MergedNewExpr.v((RefLikeType) type), type, null);
        } else {
            return heap;
        }
    }
}
