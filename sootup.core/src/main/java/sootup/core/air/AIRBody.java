package sootup.core.air;

import sootup.core.air.common.AIRStmt;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record AIRBody(String methodSignature,
                      List<AIRStmt> airStmts,
                      Set<String> locals,
                      boolean isFixedPointReady){

    public static class AIRBodyBuilder {
        private String signature;
        private final List<AIRStmt> statements = new ArrayList<>();
        private final Set<String> locals = new HashSet<>();
        private boolean isFAIR = false;

        public AIRBodyBuilder setMethodSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public void addStatement(AIRStmt stmt) {
            this.statements.add(stmt);
            extractLocals(stmt);
        }

        public AIRBodyBuilder markAsFAIR() {
            this.isFAIR = true;
            return this;
        }

        private void extractLocals(AIRStmt stmt) {
            if (stmt instanceof AIR.AIRCopyStmt copy) {
                locals.add(copy.destination());
                locals.add(copy.source());
            }
        }

        public AIRBody build() {
            return new AIRBody(signature, List.copyOf(statements), Set.copyOf(locals), isFAIR);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 1. Header: Method Signature
        sb.append("// ").append(isFixedPointReady ? "FAIR" : "AIR").append(" Representation\n");
        sb.append(methodSignature).append(" {\n");

        // 2. Declarations: Locals
        if (!locals.isEmpty()) {
            sb.append("    // Locals\n");
            for (String local : locals.stream().sorted().toList()) {
                sb.append("    local ").append(local).append(";\n");
            }
            sb.append("\n");
        }

        // 3. Statements
        sb.append("    // Statements\n");
        for (AIRStmt stmt : airStmts) {
            // Labels and Loop Headers usually don't get indentation or indices
            if (stmt instanceof AIR.AIRLabelStmt || stmt instanceof AIR.AIRLoopHeaderStmt) {
                sb.append("\n  ").append(stmt).append("\n");
            } else {
                // Numbered lines for easy Solver debugging
                sb.append("    ").append(stmt).append("\n");
            }
        }

        sb.append("}");
        return sb.toString();
    }

}
