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

package qilin.pta.toolkits.dd;

import qilin.core.pag.CallSite;
import qilin.core.pag.ContextAllocNode;
import qilin.parm.ctxcons.*;
import soot.Context;
import soot.MethodOrMethodContext;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

public class TunnelingConstructor implements CtxConstructor {
    private final CtxConstructor ctxCons;
    private final Map<SootMethod, CtxTunnelingFeaturesTrueTable> m2ftt = new HashMap<>();

    private CtxTunnelingFeaturesTrueTable findOrCreateTunnelingFeaturesTrueTable(SootMethod sm) {
        return m2ftt.computeIfAbsent(sm, k -> new CtxTunnelingFeaturesTrueTable(sm));
    }

    public TunnelingConstructor(CtxConstructor ctxCons) {
        this.ctxCons = ctxCons;
    }

    @Override
    public Context constructCtx(MethodOrMethodContext caller, ContextAllocNode receiverNode, CallSite callSite, SootMethod target) {
        CtxTunnelingFeaturesTrueTable ctftt1 = findOrCreateTunnelingFeaturesTrueTable(caller.method());
        CtxTunnelingFeaturesTrueTable ctftt2 = findOrCreateTunnelingFeaturesTrueTable(target);
        if (ctxCons instanceof CallsiteCtxConstructor) {
            if (ctftt1.cfaFormula1() || ctftt2.cfaFormula2()) {
                return caller.context();
            }
        } else if (ctxCons instanceof TypeCtxConstructor) {
            if (ctftt1.typeFormula1() || ctftt2.typeFormula2()) {
                return caller.context();
            }
        } else if (ctxCons instanceof ObjCtxConstructor) {
            if (ctftt1.objFormula1() || ctftt2.objFormula2()) {
                return caller.context();
            }
        } else if (ctxCons instanceof HybObjCtxConstructor) {
            if (ctftt1.hybridFormula1() || ctftt2.hybridFormula2()) {
                return caller.context();
            }
        } else {
            throw new RuntimeException("unsupported context constructor for tunneling: " + ctxCons.getClass());
        }
        return ctxCons.constructCtx(caller, receiverNode, callSite, target);
    }
}
