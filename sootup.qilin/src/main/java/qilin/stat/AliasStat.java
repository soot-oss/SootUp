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
import qilin.core.pag.FieldRefNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.Node;
import qilin.core.pag.VarNode;
import qilin.core.sets.PointsToSet;
import qilin.util.Pair;
import qilin.util.Util;
import soot.Local;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;
import soot.util.queue.QueueReader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AliasStat implements AbstractStat {
    private final PTA pta;
    Map<LocalVarNode, Set<LocalVarNode>> assignMap = new HashMap<>();
    Map<SparkField, Map<Boolean, Set<LocalVarNode>>> globalMap = new HashMap<>();
    private int intraAlias = 0, intraAlias_incstst = 0, globalAlias = 0, globalAlias_incstst = 0;
    private int intraAlias_app = 0, intraAlias_incstst_app = 0, globalAlias_app = 0, globalAlias_incstst_app = 0;

    public AliasStat(PTA pta) {
        this.pta = pta;
    }

    private Pair<Integer, Integer> recordAndComputeIntraAliases(Set<SootMethod> reachableMethods) {
        int intraAlias = 0;
        int intraAlias_incstst = 0;
        for (SootMethod m : reachableMethods) {
            Map<SparkField, Map<Boolean, Set<LocalVarNode>>> localMap = new HashMap<>();
            MethodPAG srcmpag = pta.getPag().getMethodPAG(m);
            QueueReader<Node> reader = srcmpag.getInternalReader().clone();
            while (reader.hasNext()) {
                Node from = reader.next(), to = reader.next();
                if (from instanceof LocalVarNode) {
                    if (to instanceof LocalVarNode) {
                        if (!(((VarNode) from).getVariable() instanceof Local))
                            continue;
                        if (!(((VarNode) to).getVariable() instanceof Local))
                            continue;
                        Util.addToMap(assignMap, (LocalVarNode) from, (LocalVarNode) to);
                        Util.addToMap(assignMap, (LocalVarNode) to, (LocalVarNode) from);
                    } else if (to instanceof FieldRefNode fr) {
                        LocalVarNode base = (LocalVarNode) fr.getBase();
                        if (!(base.getVariable() instanceof Local))
                            continue;
                        addToMap(globalMap, fr.getField(), true, base);
                        addToMap(localMap, fr.getField(), true, base);
                    }//else//local-global
                } else if (from instanceof FieldRefNode fr) {
                    LocalVarNode base = (LocalVarNode) fr.getBase();
                    if (!(base.getVariable() instanceof Local))
                        continue;
                    addToMap(globalMap, fr.getField(), false, base);
                    addToMap(localMap, fr.getField(), false, base);
                }//else//global-local or new
            }

            int methodAlias = 0, methodAlias_incstst = 0;
            for (Map<Boolean, Set<LocalVarNode>> subMap : localMap.values()) {
                Set<LocalVarNode> storeSet = subMap.getOrDefault(true, Collections.emptySet());
                Set<LocalVarNode> loadSet = subMap.getOrDefault(false, Collections.emptySet());
                int stld = checkAlias(storeSet, loadSet, assignMap) + checkAlias(loadSet, storeSet, assignMap);
                int stst = checkAlias(storeSet, storeSet, assignMap);
                methodAlias += stld;
                methodAlias_incstst += stld + stst;
            }

            intraAlias += methodAlias;
            intraAlias_incstst += methodAlias_incstst;
        }
        return new Pair<>(intraAlias, intraAlias_incstst);
    }

    private Pair<Integer, Integer> computeInterAliases() {
        int globalAlias = 0;
        int globalAlias_incstst = 0;
        for (Map<Boolean, Set<LocalVarNode>> subMap : globalMap.values()) {
            Set<LocalVarNode> storeSet = subMap.getOrDefault(true, Collections.emptySet());
            Set<LocalVarNode> loadSet = subMap.getOrDefault(false, Collections.emptySet());
            int stld = checkAlias(storeSet, loadSet, assignMap) + checkAlias(loadSet, storeSet, assignMap);
            int stst = checkAlias(storeSet, storeSet, assignMap);
            globalAlias += stld;
            globalAlias_incstst += stld + stst;
        }
        return new Pair<>(globalAlias, globalAlias_incstst);
    }

    private int checkAlias(Set<LocalVarNode> set1, Set<LocalVarNode> set2, Map<LocalVarNode, Set<LocalVarNode>> exclMap) {
        int num = 0;
        for (LocalVarNode l1 : set1) {
            Set<LocalVarNode> exclSet = exclMap.getOrDefault(l1, Collections.emptySet());
            int l1Hashcode = l1.hashCode();
            for (LocalVarNode l2 : set2) {
                int l2Hashcode = l2.hashCode();
                if (l2Hashcode <= l1Hashcode || exclSet.contains(l2)) {
                    continue;
                }
                if (checkAlias(l1, l2)) {
                    num++;
                }
            }
        }
        return num;
    }

    private boolean checkAlias(LocalVarNode l1, LocalVarNode l2) {
        PointsToSet pts1 = pta.reachingObjects((Local) l1.getVariable());
        PointsToSet pts2 = pta.reachingObjects((Local) l2.getVariable());
        return pts1.hasNonEmptyIntersection(pts2);
    }

    public static <K, T, V> boolean addToMap(Map<K, Map<T, Set<V>>> m, K key1, T key2, V value) {
        Map<T, Set<V>> subMap = m.computeIfAbsent(key1, k -> new HashMap<>());
        return Util.addToMap(subMap, key2, value);
    }

    public void aliasesProcessing() {
        Collection<SootMethod> reachableMethods = pta.getNakedReachableMethods();
        Pair<Integer, Integer> r1 = recordAndComputeIntraAliases(reachableMethods.stream().filter(m -> m.getDeclaringClass().isApplicationClass()).collect(Collectors.toSet()));
        this.intraAlias_app = r1.getFirst();
        this.intraAlias_incstst_app = r1.getSecond();
        Pair<Integer, Integer> r2 = computeInterAliases();
        this.globalAlias_app = r2.getFirst();
        this.globalAlias_incstst_app = r2.getSecond();
        Pair<Integer, Integer> r3 = recordAndComputeIntraAliases(reachableMethods.stream().filter(m -> !m.getDeclaringClass().isApplicationClass()).collect(Collectors.toSet()));
        this.intraAlias = this.intraAlias_app + r3.getFirst();
        this.intraAlias_incstst = this.intraAlias_incstst_app + r3.getSecond();
        Pair<Integer, Integer> r4 = computeInterAliases();
        this.globalAlias = r4.getFirst();
        this.globalAlias_incstst = r4.getSecond();
    }

    public int getGlobalAliasesIncludingStSt() {
        return globalAlias_incstst;
    }

    @Override
    public void export(Exporter exporter) {
        aliasesProcessing();
        exporter.collectMetric("#intraAlias(App):", String.valueOf(intraAlias_app));
        exporter.collectMetric("#intraAlias_incstst(App):", String.valueOf(intraAlias_incstst_app));
        exporter.collectMetric("#globalAlias(App):", String.valueOf(globalAlias_app));
        exporter.collectMetric("#globalAlias_incstst(App):", String.valueOf(globalAlias_incstst_app));
        exporter.collectMetric("#intraAlias:", String.valueOf(intraAlias));
        exporter.collectMetric("#intraAlias_incstst:", String.valueOf(intraAlias_incstst));
        exporter.collectMetric("#globalAlias:", String.valueOf(globalAlias));
        exporter.collectMetric("#globalAlias_incstst:", String.valueOf(globalAlias_incstst));
    }
}
