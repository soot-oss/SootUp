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

package qilin.stat;

import qilin.core.PTA;
import qilin.core.pag.*;

import java.util.Map;
import java.util.Set;

public class PAGStat implements AbstractStat {
    private final PTA pta;
    private final PAG pag;

    private int newEdges = 0; // v = new O;
    private int simpleEdges = 0; // include assign, global_assign, and o.f assign.
    private int hloadEdges = 0; // v = o.f;
    private int hstoreEdges = 0; // o.f = v;
    private int storeEdges = 0; // v.f = v;
    private int loadEdges = 0; // v = v.f;

    public PAGStat(PTA pta) {
        this.pta = pta;
        this.pag = pta.getPag();
        init();
    }

    private void init() {
        for (Set<VarNode> s : pag.getAlloc().values()) {
            newEdges += s.size();
        }
        for (Map.Entry<ValNode, Set<ValNode>> e : pag.getSimple().entrySet()) {
            Set<ValNode> tagets = e.getValue();
            int nt = tagets.size();
            simpleEdges += nt;
            if (e.getKey() instanceof ContextField) {
                hloadEdges += nt;
            } else {
                for (ValNode v : tagets) {
                    if (v instanceof ContextField) {
                        hstoreEdges++;
                    }
                }
            }
        }
        for (Map.Entry<FieldRefNode, Set<VarNode>> s : pag.getStoreInv().entrySet()) {
            storeEdges += s.getValue().size();
        }
        for (Map.Entry<FieldRefNode, Set<VarNode>> s : pag.getLoad().entrySet()) {
            loadEdges += s.getValue().size();
        }
    }

    @Override
    public void export(Exporter exporter) {
        exporter.collectMetric("#Alloc-pag-edge:", String.valueOf(newEdges));
        exporter.collectMetric("#Simple-pag-edge:", String.valueOf(simpleEdges));
        exporter.collectMetric("\t#Local-to-Local:", String.valueOf((simpleEdges - hloadEdges - hstoreEdges)));
        exporter.collectMetric("\t#Field-to-Local:", String.valueOf(hloadEdges));
        exporter.collectMetric("\t#Local-to-Field:", String.valueOf(hstoreEdges));
        exporter.collectMetric("#Store-pag-edge:", String.valueOf(storeEdges));
        exporter.collectMetric("#Load-pag-edge:", String.valueOf(loadEdges));
    }
}
