package tr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void verifyCall(){ /* do something */ }
    public static void main(String[] args) throws Exception {
        Class.forName("tr.InitializedClass");
    }
}
class InitializedClass {
    static {
        staticInitializerCalled();
    }
    @DirectCall(name="verifyCall", line=21, resolvedTargets = "Ltr/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
