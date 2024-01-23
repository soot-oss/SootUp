package sootup.java.bytecode.interceptors.defuse;

import sootup.core.graph.BasicBlock;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;

import java.util.*;

public class DefUseHelper {

    /**
     * Returns a map of locals to Stmts, such that the Stmt defines the local in the given block
     * if a local is defined multiple times, we keep the most recent definition.
     *
     * @param block
     * @return
     */
    public static Map<Local, Stmt> getDefsInBlock(BasicBlock<?> block) {
        Map<Local, Stmt> localToDefStmt = new HashMap<>();
        for (Stmt stmt : block.getStmts()) {
            if (stmt.getDefs().size() == 1) {
                Local def = (Local) stmt.getDefs().get(0);
                localToDefStmt.put(def, stmt); // subsequent defs will overwrite
            }
        }
        return localToDefStmt;
    }

    /**
     * Returns a map of locals to Stmts, such that the Stmt uses the local in the given block.
     * We only keep the first use the local
     * @param block
     * @return
     */
    public static Map<Local, Stmt> getFirstUsesInBlock(BasicBlock<?> block) {
        Map<Local, Stmt> localToUseStmt = new HashMap<>();
        for (Stmt stmt : block.getStmts()) {
            for (Value use : stmt.getUses()) {
                if (use instanceof Local) {
                    localToUseStmt.putIfAbsent((Local) use, stmt);
                }
            }
        }
        return localToUseStmt;
    }

}
