package vc;
import lib.annotations.callgraph.DirectCall;
class Class {
    public void method(){ }
    @DirectCall(name = "method", line = 11, resolvedTargets = "Lvc/SubClass;")
    public static void callMethod(Class cls) {
        cls.method();
    }
    public static void main(String[] args){
        callMethod(new SubClass());
    }
}
class SubClass extends Class {
    public void method() { }
}
