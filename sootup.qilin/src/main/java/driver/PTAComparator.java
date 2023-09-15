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

package driver;

import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.PAG;
import qilin.core.pag.VarNode;
import qilin.core.sets.PointsToSet;
import qilin.pta.PTAConfig;
import qilin.util.Util;
import soot.Local;

import java.io.PrintStream;
import java.util.*;

public class PTAComparator {
    static final boolean verbose = true;
    public static int count, count2;

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        for (String arg : args) {
            if (!arg.startsWith("-pta=")) {
                list.add(arg);
            }
        }
        runCompare(list.toArray(new String[0]), "2o", "E-2o");
    }

    public static void runCompare(String[] args, String ptaPtn1, String ptaPtn2) {
        Comparator comparator = new PTSComparator();
        String[] ptaArg1 = Util.concat(args, new String[]{"-pta=" + ptaPtn1});
        comparator.record(Main.run(ptaArg1));
        PTAConfig.v().getPtaConfig().ptaPattern = new PTAPattern(ptaPtn2);
        PTA pta = PTAFactory.createPTA(PTAConfig.v().getPtaConfig().ptaPattern);
        pta.pureRun();
        comparator.compare(pta);
        comparator.out.close();
        count = comparator.count;
        count2 = comparator.count2;
    }

    abstract static class Comparator {
        protected int count, count2;
        PrintStream out;

        public Comparator() {
            out = System.out;
        }

        abstract void record(PTA pta);

        abstract void compare(PTA pta);
    }

    static class PTSComparator extends Comparator {
        private final Map<Object, Set<Object>> valToOldAllocs = new HashMap<>();
        private final Map<Object, Set<Object>> valToNewAllocs = new HashMap<>();

        private Set<Object> getOldAllocs(Object val) {
            return valToOldAllocs.computeIfAbsent(val, k -> new HashSet<>());
        }

        private Set<Object> getNewAllocs(Object val) {
            return valToNewAllocs.computeIfAbsent(val, k -> new HashSet<>());
        }

        @Override
        void record(PTA pta) {
            pta.getPag().getValNodes().forEach(valNode -> {
                if (valNode instanceof VarNode varNode) {
                    final Set<Object> allocSites = getOldAllocs(varNode.getVariable());
                    PointsToSet pts = pta.reachingObjects(varNode).toCIPointsToSet();
                    for (Iterator<AllocNode> it = pts.iterator(); it.hasNext(); ) {
                        AllocNode n = it.next();
                        allocSites.add(n.getNewExpr());
                    }
                }
            });
        }

        @Override
        void compare(PTA pta) {
            count = 0;
            PAG pag = pta.getPag();
            pag.getValNodes().forEach(valNode -> {
                if (valNode instanceof VarNode varNode) {
                    final Set<Object> allocSites = getNewAllocs(varNode.getVariable());

                    PointsToSet pts1 = pta.reachingObjects(varNode).toCIPointsToSet();
                    for (Iterator<AllocNode> it = pts1.iterator(); it.hasNext(); ) {
                        AllocNode n = it.next();
                        allocSites.add(n.getNewExpr());
                    }
                }
            });
            valToNewAllocs.forEach((val, allocSites) -> {
                if (val.toString().contains("Parm THIS_NODE")) {
                    return;
                }
                Set<Object> oldAllocSites = getOldAllocs(val);
                if (oldAllocSites == null)
                    oldAllocSites = Collections.emptySet();
                if (!oldAllocSites.equals(allocSites)) {
                    if (verbose) {
                        out.println("\nPoint-to sets not equal!");
                        if (val instanceof Local) {
                            out.println("local: " + pag.findLocalVarNode(val).getMethod() + ": " + val);
                        } else {
                            out.println("not local: " + pag.findValNode(val) + ": " + val);
                        }

                        out.println("Old Points-to set:" + oldAllocSites.size());
//                        oldAllocSites.forEach(a -> System.out.println(qilin.pta.getPag().valToAllocNode.get(a)));
                        out.println("New Points-to set:" + allocSites.size());
//                        allocSites.forEach(a -> System.out.println(qilin.pta.getPag().valToAllocNode.get(a)));
                    }
                    int diff = allocSites.size() - oldAllocSites.size();
                    if (diff > 0)
                        count += diff;
                    else if (diff < 0)
                        count2 -= diff;
                    if (verbose) {
                        out.println("difference:" + diff);
                        if (diff > 0) {
                            allocSites.removeAll(oldAllocSites);
                            allocSites.forEach(a -> out.println(pag.findAllocNode(a)));
                        } else if (diff < 0) {
                            oldAllocSites.removeAll(allocSites);
                            oldAllocSites.forEach(a -> out.println(pag.findAllocNode(a)));
                        }
                    }
                }
            });
            out.println("\nTotal pts over:" + count);
            out.println("\nTotal pts lose:" + count2);
        }
    }
}
