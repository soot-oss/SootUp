package nvc;
import lib.annotations.callgraph.DirectCall;
public class Class {
    public Class(){
    }
    @DirectCall(name = "<init>", line = 13, resolvedTargets = "Lnvc/Class;")
    public static void main(String[] args){
        Class cls = new Class();
    }
}
