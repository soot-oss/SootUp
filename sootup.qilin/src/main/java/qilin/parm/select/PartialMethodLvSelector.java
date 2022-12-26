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

public class PartialMethodLvSelector extends CtxSelector {
    private final int k;
    private final int hk;
    // precision-critical methods selected by the method-level approaches, e.g., Zipper.
    private final Set<SootMethod> pcm;

    public PartialMethodLvSelector(int k, int hk, Set<SootMethod> pcm) {
        this.k = k;
        this.hk = hk;
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
        SootMethod sm = lvn.getMethod();
        if (pcm.contains(sm)) {
            return contextTailor(context, k);
        } else {
            return CtxConstructor.emptyContext;
        }
    }

    @Override
    public Context select(FieldValNode fvn, Context context) {
        return contextTailor(context, k);
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        SootMethod sm = heap.getMethod();
        if (pcm.contains(sm)) {
            return contextTailor(context, hk);
        } else {
            return context;
        }
    }
}
