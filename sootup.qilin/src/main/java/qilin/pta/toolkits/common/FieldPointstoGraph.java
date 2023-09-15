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

package qilin.pta.toolkits.common;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.PAG;
import qilin.core.sets.PointsToSet;
import soot.jimple.spark.pag.SparkField;

import java.util.*;

public class FieldPointstoGraph {
    private final Map<AllocNode, Map<SparkField, Set<AllocNode>>> pointsTo = new HashMap<>();
    private final Map<AllocNode, Map<SparkField, Set<AllocNode>>> pointedBy = new HashMap<>();

    public FieldPointstoGraph(PTA pta) {
        buildFPG(pta);
    }

    private void buildFPG(PTA pta) {
        PAG pag = pta.getPag();
        pag.getAllocNodes().forEach(this::insertObj);
        pag.getContextFields().forEach(contextField -> {
            AllocNode base = contextField.getBase();
            if (base.getMethod() == null) {
                return;
            }
            SparkField field = contextField.getField();
            PointsToSet pts = pta.reachingObjects(contextField).toCIPointsToSet();
            for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
                AllocNode n = it.next();
                insertFPT(base, field, n);
            }
        });
    }

    public Set<AllocNode> getAllObjs() {
        return pointsTo.keySet();
    }

    public Set<SparkField> outFieldsOf(AllocNode baseObj) {
        return pointsTo.getOrDefault(baseObj, Collections.emptyMap()).keySet();
    }

    public Set<SparkField> inFieldsOf(AllocNode obj) {
        return pointedBy.get(obj).keySet();
    }

    public Set<AllocNode> pointsTo(AllocNode baseObj, SparkField field) {
        return pointsTo.get(baseObj).get(field);
    }

    public Set<AllocNode> pointedBy(AllocNode obj, SparkField field) {
        return pointedBy.get(obj).get(field);
    }

    public boolean hasFieldPointer(AllocNode obj, SparkField field) {
        return pointsTo.get(obj).containsKey(field);
    }


    private void insertObj(AllocNode obj) {
        pointsTo.computeIfAbsent(obj, k -> new HashMap<>());
        pointedBy.computeIfAbsent(obj, k -> new HashMap<>());
    }

    /**
     * Insert field points-to relation.
     *
     * @param baseObj the base object
     * @param field   a field of `baseObj'
     * @param obj     the object pointed by `field'
     */
    private void insertFPT(AllocNode baseObj, SparkField field, AllocNode obj) {
        insertPointsTo(baseObj, field, obj);
        insertPointedBy(baseObj, field, obj);
    }

    private void insertPointsTo(AllocNode baseObj, SparkField field, AllocNode obj) {
        Map<SparkField, Set<AllocNode>> fpt = pointsTo.computeIfAbsent(baseObj, k -> new HashMap<>());
        fpt.computeIfAbsent(field, k -> new HashSet<>()).add(obj);
    }

    private void insertPointedBy(AllocNode baseObj, SparkField field, AllocNode obj) {
        Map<SparkField, Set<AllocNode>> fpb = pointedBy.computeIfAbsent(obj, k -> new HashMap<>());
        fpb.computeIfAbsent(field, k -> new HashSet<>()).add(baseObj);
    }

}
