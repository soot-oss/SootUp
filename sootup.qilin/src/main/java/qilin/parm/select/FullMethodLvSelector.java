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

public class FullMethodLvSelector extends CtxSelector {
    private final int k;
    /*
     * Methods and its corresponding context length obtained by Data-driven (OOPSLA 2017).
     * */
    private final Map<SootMethod, Integer> m2len;

    public FullMethodLvSelector(Map<SootMethod, Integer> m2len, int k) {
        this.m2len = m2len;
        this.k = k;
    }

    @Override
    public Context select(SootMethod m, Context context) {
        return contextTailor(context, m2len.getOrDefault(m, 0));
    }

    @Override
    public Context select(LocalVarNode lvn, Context context) {
        SootMethod sm = lvn.getMethod();
        return contextTailor(context, m2len.getOrDefault(sm, 0));
    }

    @Override
    public Context select(FieldValNode fvn, Context context) {
        return contextTailor(context, k);
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        SootMethod sm = heap.getMethod();
        return contextTailor(context, Math.max(0, m2len.getOrDefault(sm, 0) - 1));
    }
}
