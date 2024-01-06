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

import java.util.*;
import javax.annotation.Nonnull;

import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable (definition)
 * that are independent of each other.
 *
 * <p>For example the code:
 *
 * <pre>
 *    l0 := @this Test
 *    l1 = 0
 *    l2 = 1
 *    l1 = l1 + 1
 *    l2 = l2 + 1
 *    return
 * </pre>
 * <p>
 * to:
 *
 * <pre>
 *    l0 := @this Test
 *    l1#1 = 0
 *    l2#2 = 1
 *    l1#3 = l1#1 + 1
 *    l2#4 = l2#2 + 1
 *    return
 * </pre>
 *
 */
public class LocalSplitter implements BodyInterceptor {

    @Override
    public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View view) {
        Set<Local> localsToSplit = findLocalsToSplit(builder);
        Map<Local, Integer> mostRecentDef = new HashMap<>();
        Map<Stmt, Stmt> originalToNewStmt = new HashMap<>();
        for (Local local : localsToSplit) {
            mostRecentDef.put(local, 0);
        }
        int i = 0;
        for (Stmt stmt : builder.getStmts()) {
            List<LValue> defs = stmt.getDefs();
            if(defs.size()==0){
                continue;
            }
            if (defs.size()==1) {
                LValue def = defs.get(0);
                if (def instanceof Local) {
                    Local oldLocal = (Local) def;
                    if (localsToSplit.contains(oldLocal)) {
                        handleSelfUse(builder, stmt, oldLocal, mostRecentDef, originalToNewStmt);
                        Local newLocal = oldLocal.withName(oldLocal.getName() + '#' + (++i)); // renaming should not be done here
                        mostRecentDef.put(oldLocal, i);
                        builder.addLocal(newLocal);
                        Stmt toReplace;
                        if (originalToNewStmt.containsKey(stmt)) {
                            toReplace = originalToNewStmt.get(stmt);
                        } else {
                            toReplace = stmt;
                        }
                        Stmt withNewDef = new JAssignStmt(newLocal, toReplace.getUses().get(0), stmt.getPositionInfo());
                        builder.getStmtGraph().replaceNode(toReplace, withNewDef);
                        originalToNewStmt.put(stmt, withNewDef);
                    }
                }
            } else{
                throw new RuntimeException("stmt with more than 1 def!");
            }
/*
            for (Value use : stmt.getUses()) {
                if (!(use instanceof Local)) {
                    continue;
                }
                Local oldLocalUse = (Local) use;
                if (localsToSplit.contains(use)) {
                    Local newLocal = oldLocalUse.withName(oldLocalUse.getName() + '#' + mostRecentDef.get(oldLocalUse)); // use the most recent split name
                    Stmt withNewUse = stmt.withNewUse(oldLocalUse, newLocal);
                    Stmt toReplace;
                    if (originalToNewStmt.containsKey(stmt)) {
                        toReplace = originalToNewStmt.get(stmt);
                    } else {
                        toReplace = stmt;
                    }
                    builder.getStmtGraph().replaceNode(toReplace, withNewUse);
                    originalToNewStmt.put(stmt, withNewUse);
                }
            }
*/
        }
    }

    private void handleSelfUse(Body.BodyBuilder builder, Stmt stmt, Local def, Map<Local, Integer> mostRecentDef, Map<Stmt, Stmt> originalToNewStmt) {
        for (Value use : stmt.getUses()) {
            if(use.equals(def)){
                Local oldLocalUse = (Local) use;
                Local newLocal = oldLocalUse.withName(oldLocalUse.getName() + '#' + mostRecentDef.get(oldLocalUse)); // use the most recent split name
                Stmt withNewUse = stmt.withNewUse(oldLocalUse, newLocal);
                Stmt toReplace;
                if (originalToNewStmt.containsKey(stmt)) {
                    toReplace = originalToNewStmt.get(stmt);
                } else {
                    toReplace = stmt;
                }
                builder.getStmtGraph().replaceNode(toReplace, withNewUse);
                originalToNewStmt.put(stmt, withNewUse);
            }
        }
    }

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


}
