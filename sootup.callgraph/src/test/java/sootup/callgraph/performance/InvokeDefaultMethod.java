package sootup.callgraph.performance;

public interface InvokeDefaultMethod {
  default void defaultMeth() {
    System.out.println("Default Method.");
  }
}
