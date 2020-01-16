package lib4.collude;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    @DirectCall(name = "method", line = 10, resolvedTargets = "Llib4/collude/PotentialSuperclass;", 
    prohibitedTargets = "Llib4/internal/InternalClass;")
    public static void interfaceCallSite(PotentialInterface pi){
        pi.method();
    }
}
