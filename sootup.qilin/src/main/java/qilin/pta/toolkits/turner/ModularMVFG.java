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
import qilin.core.PointsToAnalysis;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.Node;
import qilin.util.graph.MergedNode;
import soot.RefLikeType;
import soot.SootMethod;

import java.util.Set;

public class ModularMVFG extends AbstractMVFG {
    public static AbstractMVFG findOrCreateMethodVFG(PTA prePTA, SootMethod method, OCG hg, MergedNode<SootMethod> sccNode) {
        return method2VFG.computeIfAbsent(method, k -> new ModularMVFG(prePTA, method, hg, sccNode));
    }

    private final MergedNode<SootMethod> sccNode;

    public ModularMVFG(PTA prePTA, SootMethod method, OCG hg, MergedNode<SootMethod> sccNode) {
        super(prePTA, hg, method);
        this.sccNode = sccNode;
        buildVFG();
    }

    @Override
    protected boolean statisfyAddingLoadCondition(Set<SootMethod> targets) {
        for (SootMethod tgtmtd : targets) {
            // the target method is in the same scc with current method.
            if (tgtmtd.isPhantom() || sccNode.getContent().contains(tgtmtd)) {
                return true;
            }
            if (!(tgtmtd.getReturnType() instanceof RefLikeType)) {
                continue;
            }
            MethodPAG tgtmpag = prePTA.getPag().getMethodPAG(tgtmtd);
            AbstractMVFG tgtVfg = method2VFG.get(tgtmtd);
            assert tgtVfg != null;
            MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
            Node ret = tgtnf.caseRet();
            if (tgtVfg.getCSNodes().contains(ret)) {
                return true;
            }
        }
        return false;
    }


    protected boolean satisfyAddingStoreCondition(int paramIndex, Set<SootMethod> targets) {
        for (SootMethod tgtmtd : targets) {
            // the target method is in the same scc with current method.
            if (tgtmtd.isPhantom() || sccNode.getContent().contains(tgtmtd)) {
                return true;
            }
            MethodPAG tgtmpag = prePTA.getPag().getMethodPAG(tgtmtd);
            AbstractMVFG tgtVfg = method2VFG.get(tgtmtd);
            assert tgtVfg != null;
            MethodNodeFactory tgtnf = tgtmpag.nodeFactory();
            LocalVarNode parm;
            if (paramIndex == PointsToAnalysis.THIS_NODE) {
                parm = (LocalVarNode) tgtnf.caseThis();
            } else {
                if (tgtmtd.getParameterType(paramIndex) instanceof RefLikeType) {
                    parm = (LocalVarNode) tgtnf.caseParm(paramIndex);
                } else {
                    continue;
                }
            }
            if (tgtVfg.getCSNodes().contains(parm)) {
                return true;
            }
        }
        return false;
    }

}
