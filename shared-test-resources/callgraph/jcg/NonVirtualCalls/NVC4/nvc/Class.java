package nvc;
import lib.annotations.callgraph.DirectCall;
class Class extends Superclass {
    @DirectCall(name = "method", line = 9, resolvedTargets = "Lnvc/Rootclass;")
    protected void method(){
        super.method();
    }
    public static void main(String[] args){
        Class cls = new Class();
        cls.method();
    }
}
class Superclass extends Rootclass {
}
class Rootclass {
    protected void method(){ /* do something relevant */ }
}
