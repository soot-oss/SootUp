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

import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.AllocNode;
import qilin.core.pag.FieldValNode;
import qilin.core.pag.LocalVarNode;
import qilin.parm.ctxcons.CtxConstructor;
import soot.Context;
import soot.SootMethod;

public abstract class CtxSelector {
    public abstract Context select(SootMethod m, Context context);

    public abstract Context select(LocalVarNode lvn, Context context);

    public abstract Context select(FieldValNode fvn, Context context);

    public abstract Context select(AllocNode heap, Context context);

    protected Context contextTailor(Context context, int length) {
        if (length == 0) {
            return CtxConstructor.emptyContext;
        }
        ContextElements ctx = (ContextElements) context;
        ContextElement[] fullContexts = ctx.getElements();
        if (length >= ctx.size()) {
            return context;
        }
        ContextElement[] newContexts = new ContextElement[length];
        System.arraycopy(fullContexts, 0, newContexts, 0, length);
        return new ContextElements(newContexts, length);
    }
}
