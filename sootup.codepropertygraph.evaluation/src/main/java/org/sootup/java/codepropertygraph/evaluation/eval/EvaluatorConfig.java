package org.sootup.java.codepropertygraph.evaluation.eval;

public class EvaluatorConfig {
    private final String joernDir;
    private final String sootUpDir;
    private final String resultDir;
    private final String logFile;

    // Constructor
    public EvaluatorConfig(String joernDir, String sootUpDir, String resultDir, String logFile) {
        this.joernDir = joernDir;
        this.sootUpDir = sootUpDir;
        this.resultDir = resultDir;
        this.logFile = logFile;
    }

    // Getters
    public String getJoernDir() {
        return joernDir;
    }

    public String getSootUpDir() {
        return sootUpDir;
    }

    public String getResultDir() {
        return resultDir;
    }

    public String getLogFile() {
        return logFile;
    }
}
