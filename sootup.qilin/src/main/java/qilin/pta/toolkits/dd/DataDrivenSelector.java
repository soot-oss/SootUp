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

import qilin.core.pag.AllocNode;
import qilin.core.pag.FieldValNode;
import qilin.core.pag.LocalVarNode;
import qilin.parm.ctxcons.CallsiteCtxConstructor;
import qilin.parm.ctxcons.HybObjCtxConstructor;
import qilin.parm.ctxcons.ObjCtxConstructor;
import qilin.parm.ctxcons.TypeCtxConstructor;
import qilin.parm.select.CtxSelector;
import soot.Context;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Map;

public class DataDrivenSelector extends CtxSelector {
    private final Map<SootMethod, FeaturesTrueTable> m2ftt = new HashMap<>();
    private final Class mClass;

    public DataDrivenSelector(Class mClass) {
        this.mClass = mClass;
    }

    private FeaturesTrueTable findOrCreateFeaturesTrueTable(SootMethod sm) {
        return m2ftt.computeIfAbsent(sm, k -> new FeaturesTrueTable(sm));
    }

    @Override
    public Context select(SootMethod m, Context context) {
        FeaturesTrueTable ftt = findOrCreateFeaturesTrueTable(m);
        int i = 0;
        if (mClass == HybObjCtxConstructor.class) {
            if (ftt.hybrid2objFormula2()) {
                i = 2;
            } else if (ftt.hybrid2objFormula1()) {
                i = 1;
            }
        } else if (mClass == ObjCtxConstructor.class) {
            if (ftt.twoObjFormula2()) {
                i = 2;
            } else if (ftt.twoObjFormula1()) {
                i = 1;
            }
        } else if (mClass == CallsiteCtxConstructor.class) {
            if (ftt.twoCFAFormula2()) {
                i = 2;
            } else if (ftt.twoCFAFormula1()) {
                i = 1;
            }
        } else if (mClass == TypeCtxConstructor.class) {
            if (ftt.twoTypeFormula2()) {
                i = 2;
            } else if (ftt.twoTypeFormula1()) {
                i = 1;
            }
        } else {
            throw new RuntimeException("unsupport data-driven pointer analysis!");
        }
        return contextTailor(context, i);
    }

    @Override
    public Context select(LocalVarNode lvn, Context context) {
        return context;
    }

    @Override
    public Context select(FieldValNode fvn, Context context) {
        return context;
    }

    @Override
    public Context select(AllocNode heap, Context context) {
        return context;
    }
}
