package id;
import lib.annotations.callgraph.IndirectCall;
class Class {
     @FunctionalInterface interface Runnable {
        void run();
    }
    public static void doSomething(){
        /* do something */
    }
    public static Runnable[] lambdaArray = new Runnable[10];
    @IndirectCall(name = "doSomething", line = 22, resolvedTargets = "Lid/Class;")
    public static void main(String[] args) {
        Runnable r1 = () -> doSomething();
        lambdaArray[0] = r1;
        Runnable same = lambdaArray[0];
        same.run();
    }
}
final class Math {
    public static int PI(){
        return 3;
    }
}
