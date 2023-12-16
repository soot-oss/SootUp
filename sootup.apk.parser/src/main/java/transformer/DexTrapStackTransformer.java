package transformer;

import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.language.JavaJimple;

import java.util.List;
import java.util.Set;

public class DexTrapStackTransformer {

    public void transform(List<Stmt> stmtArrayList, List<Trap> traps, Set<Local> locals){
        stmtArrayList.add(Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));
        for (Trap t : traps) {
            // If the first statement already catches the exception, we're fine
            if (isCaughtExceptionRef(t.getHandlerStmt())) {
                continue;
            }
            // Add the exception reference
            Local local = new LocalGenerator(locals).generateLocal(t.getExceptionType());
            Stmt caughtStmt = Jimple.newIdentityStmt(local, JavaJimple.getInstance().newCaughtExceptionRef(), StmtPositionInfo.createNoStmtPositionInfo());
            stmtArrayList.add(caughtStmt);
            stmtArrayList.add(Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo()));
            replaceTrap(traps,t, t.withHandlerStmt(caughtStmt));
        }
    }

    public void replaceTrap(List<Trap> traps, Trap toBeReplaced, Trap newTrap) {
        int indexOf = traps.indexOf(toBeReplaced);
        if (indexOf != -1) {
            traps.set(indexOf, newTrap);
        }
    }

    /**
     * Checks whether the given statement stores an exception reference
     *
     * @param handlerUnit
     *          The statement to check
     * @return True if the given statement stores an exception reference, otherwise false
     */
    private boolean isCaughtExceptionRef(Stmt handlerUnit) {
        if (!(handlerUnit instanceof JIdentityStmt)) {
            return false;
        }
        JIdentityStmt stmt = (JIdentityStmt) handlerUnit;
        return stmt.getRightOp() instanceof JCaughtExceptionRef;
    }
}
