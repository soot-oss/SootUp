package tr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void verifyCall(){ /* do something */ }
    @DirectCall(name="verifyCall", line=9, resolvedTargets = "Ltr/Demo;")
    public Demo() { Demo.verifyCall(); }
    public static void main(String[] args) throws Exception {
        Demo.class.newInstance();
    }
}
