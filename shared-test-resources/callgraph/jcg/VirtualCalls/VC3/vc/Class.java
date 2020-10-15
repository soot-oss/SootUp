package vc;
import lib.annotations.callgraph.DirectCall;
interface Interface {
    void method();
}
class Class {
    public void method(){ }
    @DirectCall(name = "method", line = 15, resolvedTargets = {"Lvc/ClassImpl;"}, prohibitedTargets ={"Lvc/Class;"})
    public static void callOnInterface(Interface i){
        i.method();
    }
    public static void main(String[] args){
        callOnInterface(new ClassImpl());
    }
}
class ClassImpl implements Interface {
    public void method(){ }
}
