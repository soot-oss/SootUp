package sootup.core.air;

import sootup.core.air.common.AIRStmt;

public abstract class AIR {
    public static final String APPLY = "apply ";
    public static final String SET_CONSTANT = "set_const ";
    public static final String COPY = "copy ";
    public static final String RETURN = "return ";
    public static final String ASSUME = "assume ";
    public static final String GOTO = "goto ";
    public static final String LOOP_HEADER = "LOOP_HEADER: ";

    public static class Ops{
        public static final String ADD = "Add ";
        public static final String SUB = "Sub ";
        public static final String MUL = "Mul ";
        public static final String DIV = "Div ";
    }

    public record AIRSetConstantStmt(String variableName, String value) implements AIRStmt {
        @Override public String toString() { return SET_CONSTANT + variableName + ", " + value; }
    }

    public record AIRCopyStmt(String destination, String source) implements AIRStmt {
        @Override public String toString() { return COPY + destination + ", " + source; }
    }

    public record AIRApplyStmt(String destination, String operator, java.util.List<String> args) implements AIRStmt {
        @Override public String toString() { return APPLY + destination + ", " + operator + ", " + args; }
    }

    public record AIRAssumeStmt(String left, String symbol, String right) implements AIRStmt {
        @Override public String toString() { return ASSUME + "(" + left + " " + symbol + " " + right + ")"; }
    }

    public record AIRGotoStmt(String targetLabel) implements AIRStmt {
        @Override public String toString() { return  GOTO + targetLabel; }
    }

    public record AIRRetStmt(String sourceVariable) implements AIRStmt {
        @Override public String toString() { return RETURN + "(" + sourceVariable + ")"; }
    }

    public record AIRLabelStmt(String labelName) implements AIRStmt {
        @Override public String toString() { return labelName + ":"; }
    }

    public record AIRLoopHeaderStmt(String labelName) implements AIRStmt{
        @Override public String toString() { return LOOP_HEADER; }
    }


}
