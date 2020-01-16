package vc;
import lib.annotations.callgraph.DirectCall;
interface Interface {
    void method();
}
class Class implements Interface {
    public static Interface[] types = new Interface[]{new Class(), new ClassImpl()};
    public void method(){ }
    @DirectCall(name = "method", line = 18, resolvedTargets = "Lvc/Class;")
    public static void main(String[] args){
        Interface i = types[0];
        i.method();
    }
}
class ClassImpl implements Interface {
    public void method(){ }
}
