package vc;
import lib.annotations.callgraph.DirectCall;
class Class {
    public void target(){ }
    @DirectCall(name = "target", line = 12, resolvedTargets = "Lvc/Class;")
    public static void main(String[] args){
        Class cls = new Class();
        cls.target();
    }
}
