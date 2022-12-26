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

/*
 * A pipeline context selector which selects context by firstly using ctxSelA and then ctxSelB.
 * */
public class PipelineSelector extends CtxSelector {
    private final CtxSelector ctxSelA;
    private final CtxSelector ctxSelB;

    public PipelineSelector(CtxSelector lenSelA, CtxSelector lenSelB) {
        this.ctxSelA = lenSelA;
        this.ctxSelB = lenSelB;
    }

    @Override
    public Context select(SootMethod m, Context context) {
        return ctxSelB.select(m, ctxSelA.select(m, context));
    }

    @Override
    public Context select(LocalVarNode lvn, Context context) {
        return ctxSelA.select(lvn, ctxSelB.select(lvn, context));
    }

    @Override
    public Context select(FieldValNode fvn, Context context) {
        return ctxSelA.select(fvn, ctxSelB.select(fvn, context));
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        return ctxSelA.select(heap, ctxSelB.select(heap, context));
    }
}
