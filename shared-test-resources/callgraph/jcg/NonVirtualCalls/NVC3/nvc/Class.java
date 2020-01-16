package nvc;
import lib.annotations.callgraph.DirectCall;
class Class {
    private void method(){ /* do something*/}
    private void method(int num){ /* do something*/}
    @DirectCall(name = "method", line = 13, resolvedTargets = "Lnvc/Class;")
    public static void main(String[] args){
        Class cls = new Class();
        cls.method();
    }
}
