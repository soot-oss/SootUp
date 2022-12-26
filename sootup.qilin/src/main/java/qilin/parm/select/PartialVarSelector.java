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
import qilin.parm.ctxcons.CtxConstructor;
import soot.Context;
import soot.SootMethod;

import java.util.Set;

public class PartialVarSelector extends CtxSelector {
    private final int k;
    private final int hk;
    // precision-critical nodes selected by the partial-variable-level approaches, e.g., Eagle, Turner.
    private final Set<Object> csnodes;
    private final Set<SootMethod> pcm;

    public PartialVarSelector(int k, int hk, Set<Object> csnodes, Set<SootMethod> pcm) {
        this.k = k;
        this.hk = hk;
        this.csnodes = csnodes;
        this.pcm = pcm;
    }

    @Override
    public Context select(SootMethod m, Context context) {
        if (pcm.contains(m)) {
            return contextTailor(context, k);
        } else {
            return CtxConstructor.emptyContext;
        }
    }

    @Override
    public Context select(LocalVarNode lvn, Context context) {
        Object ir = lvn.getVariable();
        if (csnodes.contains(ir)) {
            return contextTailor(context, k);
        } else {
            return CtxConstructor.emptyContext;
        }
    }


    @Override
    public Context select(FieldValNode fvn, Context context) {
        if (csnodes.contains(fvn.getField())) {
            return contextTailor(context, k);
        } else {
            return CtxConstructor.emptyContext;
        }
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        Object ir = heap.getNewExpr();
        if (csnodes.contains(ir)) {
            return contextTailor(context, hk);
        } else {
            return CtxConstructor.emptyContext;
        }
    }
}
