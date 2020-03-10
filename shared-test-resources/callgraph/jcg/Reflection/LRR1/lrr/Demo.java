package lrr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void verifyCall(){ /* do something */ }
    public static void main(String[] args) throws Exception {
        String className = (args.length % 2 == 0) ? "lrr.Left" : "lrr.Right"; 
        Class.forName(className);
    }
}
class Left {
    static {
        staticInitializerCalled();
    }
    @DirectCall(name="verifyCall", line=22, resolvedTargets = "Llrr/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
class Right {
    static {
        staticInitializerCalled();
    }
    @DirectCall(name="verifyCall", line=35, resolvedTargets = "Llrr/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
