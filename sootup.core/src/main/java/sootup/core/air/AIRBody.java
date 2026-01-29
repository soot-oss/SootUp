package sootup.core.air;

import sootup.core.air.common.AIRStmt;

import java.util.List;

public class AIRBody {
    private final List<AIRStmt> statements;

    public AIRBody(List<AIRStmt> statements) {
        this.statements = statements;
    }

    public List<AIRStmt> getStatements() {
        return statements;
    }
}
