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

package qilin.pta.toolkits.turner;

import qilin.core.PTA;
import soot.SootMethod;

import java.util.Set;

public class MethodVFG extends AbstractMVFG {
    public static AbstractMVFG findOrCreateMethodVFG(PTA prePTA, SootMethod method, OCG hg) {
        return method2VFG.computeIfAbsent(method, k -> new MethodVFG(prePTA, method, hg));
    }

    public MethodVFG(PTA prePTA, SootMethod method, OCG hg) {
        super(prePTA, hg, method);
        buildVFG();
    }

    @Override
    protected boolean statisfyAddingLoadCondition(Set<SootMethod> targets) {
        return true;
    }

    @Override
    protected boolean satisfyAddingStoreCondition(int paramIndex, Set<SootMethod> targets) {
        return true;
    }
}