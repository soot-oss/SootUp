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

package qilin.parm.ctxcons;

import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.context.TypeContextElement;
import qilin.core.pag.CallSite;
import qilin.core.pag.ContextAllocNode;
import soot.Context;
import soot.MethodOrMethodContext;
import soot.SootMethod;

// implementation of selective hybrid context...(Yannis pldi'13)
public class HybTypeCtxConstructor implements CtxConstructor {

    @Override
    public Context constructCtx(MethodOrMethodContext caller, ContextAllocNode receiverNode, CallSite callSite, SootMethod target) {
        Context callerContext = caller.context();
        if (receiverNode == null) { // static invoke
            assert callerContext instanceof ContextElements;
            ContextElements callerCtxs = (ContextElements) callerContext;
            ContextElement[] cxtAllocs = callerCtxs.getElements();
            int s = callerCtxs.size();
            ContextElement[] array = new ContextElement[s + 1];
            array[1] = callSite;
            array[0] = cxtAllocs[0];
            if (s > 1) {
                System.arraycopy(cxtAllocs, 1, array, 2, s - 1);
            }
            return new ContextElements(array, s + 1);
        } else {
            Context context = receiverNode.context();
            assert context instanceof ContextElements;
            ContextElements ctx = (ContextElements) context;
            int s = ctx.size();
            ContextElement[] cxtAllocs = ctx.getElements();
            ContextElement[] array = new ContextElement[s + 1];
            array[0] = TypeContextElement.getTypeContextElement(receiverNode.base());
            System.arraycopy(cxtAllocs, 0, array, 1, s);
            return new ContextElements(array, s + 1);
        }
    }
}
