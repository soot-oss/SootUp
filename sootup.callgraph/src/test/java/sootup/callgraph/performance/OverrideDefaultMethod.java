package sootup.callgraph.performance;

public class OverrideDefaultMethod implements InvokeDefaultMethod {
    @Override
    public void defaultMeth() {
        System.out.println("Override Method.");
    }
}
