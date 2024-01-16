package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.apache.commons.lang3.tuple.Pair;
import sootup.core.graph.*;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class LocalSplitter implements BodyInterceptor {

    private Map<Stmt, List<Pair<Stmt, Local>>> stmtToUses;

    private Map<Stmt, Stmt> newToOriginalStmt;


    static class RefBox {
        Stmt stmt;

        public RefBox(Stmt stmt) {
            this.stmt = stmt;
        }

        @Override
        public String toString() {
            return stmt.toString();
        }
    }


    /**
     * Multiple defs of the same local are to split.
     *
     * @param builder
     * @return
     */
    private Set<Local> findLocalsToSplit(Body.BodyBuilder builder) {
        Set<Local> visitedLocals = new LinkedHashSet<>();
        Set<Local> localsToSplit = new LinkedHashSet<>();
        for (Stmt stmt : builder.getStmts()) {
            for (LValue def : stmt.getDefs()) {
                if (def instanceof Local) {
                    if (visitedLocals.contains(def)) {
                        localsToSplit.add((Local) def);
                    } else {
                        visitedLocals.add((Local) def);
                    }
                }
            }
        }
        return localsToSplit;
    }


    @Override
    public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
        newToOriginalStmt = new HashMap<>();

        Set<Local> localsToSplit = findLocalsToSplit(builder);
        int w = 0;

        Set<RefBox> visited = new HashSet<>();
        for (int i = 0; i < builder.getStmts().size(); i++) {
            Stmt stmt = builder.getStmts().get(i);
            RefBox ref = new RefBox(stmt);
            if (ref.stmt.getDefs().size() == 1) {
                LValue singleDef = ref.stmt.getDefs().get(0);
                if (!(singleDef instanceof Local) || visited.remove(ref.stmt)) {
                    continue;
                }

                Local oldLocal = (Local) singleDef;
                if (!localsToSplit.contains(oldLocal)) {
                    continue;
                }

                Local newLocal = oldLocal.withName(oldLocal.getName() + "#" + (++w));
                builder.addLocal(newLocal);

                Deque<RefBox> queue = new ArrayDeque<>();
                queue.addFirst(ref);
                do {
                    RefBox head = queue.removeFirst();
                    if (visited.add(head)) {
                        Set<RefBox> useStmts = usesUntilNewDef(builder, head.stmt);
                        for (RefBox useRef : useStmts) {
                            for (Value use : useRef.stmt.getUses()) {
                                if (use == newLocal) {
                                    continue;
                                }
                                if (use instanceof Local) {
                                    queue.addAll(getDefsBefore(oldLocal, builder, useRef.stmt));
                                    addNewUse(builder, useRef, newLocal, oldLocal);
                                }
                            }
                        }
                        for (LValue def : head.stmt.getDefs()) {
                            if (def instanceof Local) {
                                addNewDef(builder, head, newLocal, oldLocal);
                            }
                        }
                    }
                    System.out.println(builder.getStmtGraph());
                } while (!queue.isEmpty());

                visited.remove(ref);
            }

        }

    }

    private void addNewDef(Body.BodyBuilder builder, RefBox ref, Local newDef, Local oldDef) {
        if (ref.stmt.getDefs().contains(oldDef)) {
            Stmt withNewDef = new JAssignStmt(newDef, ref.stmt.getUses().get(0), ref.stmt.getPositionInfo());
            builder.getStmtGraph().replaceNode(ref.stmt, withNewDef);
            ref.stmt = withNewDef;
        }
    }

    private void addNewUse(Body.BodyBuilder builder, RefBox ref, Local newLocal, Local old) {
        if (ref.stmt.getUses().contains(old)) {
            Stmt withNewUse = ref.stmt.withNewUse(old, newLocal);
            builder.getStmtGraph().replaceNode(ref.stmt, withNewUse);
            ref.stmt = withNewUse;
        }
    }

    private Set<RefBox> usesUntilNewDef(Body.BodyBuilder builder, Stmt stmt) {
        Set<Stmt> usesUntilNewDef = new HashSet<>();
        List<LValue> defs = stmt.getDefs();
        if (defs.size() == 1) {
            Local def = (Local) defs.get(0);
            Deque<Stmt> queue = new ArrayDeque<>();
            queue.add(builder.getStmts().get(0));
            while (!queue.isEmpty()) {
                Stmt s = queue.removeFirst();
                if (s.getUses().contains(def)) {
                    if (!usesUntilNewDef.add(s)) {
                        break; // if it is already there maybe we started looping?
                    }
                }
                if (!stmt.equals(s)) {
                    if (s.getDefs().contains(def)) {
                        continue;
                    }
                }
                queue.addAll(builder.getStmtGraph().successors(s));
            }
        }
        return usesUntilNewDef.stream().map(e -> new RefBox(e)).collect(Collectors.toSet());
    }

    private List<RefBox> getDefsBefore(Local local, Body.BodyBuilder builder, Stmt mStmt) {
        List<Stmt> defsBefore = new ArrayList<>();
        Set<BasicBlock> visited = new HashSet<>();
        BasicBlock<?> block = builder.getStmtGraph().getBlockOf(mStmt);
        Deque<BasicBlock> blockQueue = new ArrayDeque<>();
        blockQueue.addFirst(block);
        boolean firstPass = true;
        do {
            block = blockQueue.removeFirst();

            if (firstPass) {
                ;
                ; // to visit the same block once more when looping
            } else if (!visited.add(block)) {
                continue;
            }
            Deque<Stmt> stmtQueue = new ArrayDeque<>();
            if (firstPass) {
                stmtQueue.addAll(predsInSameBlock(builder, mStmt));
                firstPass = false;
            } else {
                stmtQueue.add(block.getTail());
            }
            while (!stmtQueue.isEmpty()) {
                Stmt s = stmtQueue.removeFirst();
                if (s.getDefs().contains(local)) {
                    defsBefore.add(s);
                    break;
                }
                // iterate only in the same block here.
                stmtQueue.addAll(predsInSameBlock(builder, s));
            }
            blockQueue.addAll(block.getPredecessors());
        } while (!blockQueue.isEmpty());
        return defsBefore.stream().map(e -> new RefBox(e)).collect(Collectors.toList());
    }

    private List<Stmt> predsInSameBlock(Body.BodyBuilder builder, Stmt stmt) {
        List<Stmt> predsInSameBlock = new ArrayList<>();
        MutableStmtGraph stmtGraph = builder.getStmtGraph();
        List<Stmt> preds = stmtGraph.predecessors(stmt);
        for (Stmt pred : preds) {
            if (stmtGraph.getBlockOf(pred).equals(stmtGraph.getBlockOf(stmt))) {
                predsInSameBlock.add(pred);
            }
        }
        return predsInSameBlock;
    }


}