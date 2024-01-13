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
import sootup.core.graph.BasicBlock;
import sootup.core.graph.DominanceFinder;
import sootup.core.graph.StmtGraph;
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

public class LocalSplitter implements BodyInterceptor {

    private Map<Stmt, List<Pair<Stmt, Local>>> stmtToUses;

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
        Set<Local> localsToSplit = findLocalsToSplit(builder);
        int w = 0;

        Set<Stmt> visited = new HashSet<>();
        for (int i = 0; i < builder.getStmts().size(); i++) {
            Stmt stmt = builder.getStmts().get(i);
            if (stmt.getDefs().size() == 1) {
                LValue singleDef = stmt.getDefs().get(0);
                if (!(singleDef instanceof Local) || visited.remove(stmt)) {
                    continue;
                }

                Local oldLocal = (Local) singleDef;
                if (!localsToSplit.contains(oldLocal)) {
                    continue;
                }

                Local newLocal = oldLocal.withName(oldLocal.getName() + "#" + (++w));
                builder.addLocal(newLocal);

                Deque<Stmt> queue = new ArrayDeque<>();
                queue.addFirst(stmt);
                do {
                    Stmt head = queue.removeFirst();
                    if (visited.add(head)) {
                        Set<Stmt> useStmts = usesUntilNewDef(builder, head);
                        for (Stmt useStmt : useStmts) {
                            for (Value use : useStmt.getUses()) {
                                if (use == newLocal) {
                                    continue;
                                }
                                if (use instanceof Local) {
                                    queue.addAll(getDefsBefore(oldLocal, builder, useStmt));
                                    addNewUse(builder, useStmt, newLocal, oldLocal);
                                }
                            }
                        }
                        for (LValue def : head.getDefs()) {
                            if (def instanceof Local) {
                                int idx = builder.getStmts().indexOf(head);
                                addNewDef(builder, head, newLocal, oldLocal);
                                if (queue.contains(head)) {

                                }
                                head = builder.getStmts().get(idx);
                            }
                        }
                    }
                    System.out.println(builder.getStmtGraph());
                } while (!queue.isEmpty());

                visited.remove(stmt);
            }

        }

    }

    private void addNewDef(Body.BodyBuilder builder, Stmt stmt, Local newDef, Local oldDef) {
        if (stmt.getDefs().contains(oldDef)) {
            Stmt witNewDef = new JAssignStmt(newDef, stmt.getUses().get(0), stmt.getPositionInfo());
            builder.getStmtGraph().replaceNode(stmt, witNewDef);
        }
    }

    private void addNewUse(Body.BodyBuilder builder, Stmt stmt, Local newLocal, Local old) {
        if (stmt.getUses().contains(old)) {
            Stmt withNewUse = stmt.withNewUse(old, newLocal);
            builder.getStmtGraph().replaceNode(stmt, withNewUse);
        }
    }

    private Set<Stmt> usesUntilNewDef(Body.BodyBuilder builder, Stmt stmt) {
        Set<Stmt> usesUntilNewDef = new HashSet<>();
        List<LValue> defs = stmt.getDefs();
        if (defs.size() == 1) {
            Local def = (Local) defs.get(0);
            Deque<Stmt> queue = new ArrayDeque<>();
            queue.add(builder.getStmts().get(0));
            while (!queue.isEmpty()) {
                Stmt s = queue.removeFirst();
                if (s.getUses().contains(def)) {
                    usesUntilNewDef.add(s);
                }
                if (!stmt.equals(s)) {
                    if (s.getDefs().contains(def)) {
                        continue;
                    }
                }
                queue.addAll(builder.getStmtGraph().successors(s));
            }
        }
        return usesUntilNewDef;
    }

    private List<Stmt> getDefsBefore(Local local, Body.BodyBuilder builder, Stmt until) {
        List<Stmt> defsBefore = new ArrayList<>();
        Deque<Stmt> queue = new ArrayDeque<>();
        queue.addAll(builder.getStmtGraph().predecessors(until));
        while (!queue.isEmpty()) {
            Stmt s = queue.removeFirst();

            if (s.getDefs().contains(local)) {
                defsBefore.add(s);
                if (queue.size() - defsBefore.size() >= 0) {
                    continue;
                }
            }

            queue.addAll(builder.getStmtGraph().predecessors(s));
        }
        return defsBefore;
    }


}