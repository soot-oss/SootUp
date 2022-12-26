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

package qilin.parm.select;

import qilin.core.pag.AllocNode;
import qilin.core.pag.FieldValNode;
import qilin.core.pag.LocalVarNode;
import soot.Context;
import soot.SootMethod;

import java.util.Map;

/*
 * This class is for a future technique and thus currently has no usage.
 * */
public class VarLvSelector extends CtxSelector {
    private final int k;
    private final int hk;
    /* mapping from nodes to the context length they require.
     * This is designed for a future approaches.
     */
    private final Map<Object, Integer> node2Len;
    private final Map<SootMethod, Integer> mthd2Len;

    public VarLvSelector(int k, int hk, Map<Object, Integer> node2Len, Map<SootMethod, Integer> m2len) {
        this.k = k;
        this.hk = hk;
        this.node2Len = node2Len;
        this.mthd2Len = m2len;
    }

    @Override
    public Context select(SootMethod m, Context context) {
        return contextTailor(context, Math.min(k, mthd2Len.getOrDefault(m, 0)));
    }

    @Override
    public Context select(LocalVarNode lvn, Context context) {
        Object ir = lvn.getVariable();
        return contextTailor(context, Math.min(k, node2Len.getOrDefault(ir, 0)));
    }

    @Override
    public Context select(FieldValNode fvn, Context context) {
        return contextTailor(context, Math.min(k, node2Len.getOrDefault(fvn.getField(), 0)));
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        Object ir = heap.getNewExpr();
        return contextTailor(context, Math.min(hk, node2Len.getOrDefault(ir, 0)));
    }
}
