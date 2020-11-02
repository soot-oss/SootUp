package id;
import lib.annotations.callgraph.IndirectCall;
class Class {
    public interface MyMarkerInterface1 {}
    public interface MyMarkerInterface2 {}
    public @FunctionalInterface interface Runnable {
        void run();
    }
    public static void doSomething(){
        /* do something */
    }
    @IndirectCall(name = "doSomething", line = 21, resolvedTargets = "Lid/Class;")
    public static void main(String[] args) {
        Runnable run = (Runnable & MyMarkerInterface1 & MyMarkerInterface2) () -> doSomething();
        run.run();
    }
}
