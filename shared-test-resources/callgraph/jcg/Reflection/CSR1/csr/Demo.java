package csr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void verifyCall(){ /* do something */ }
    static void callForName(String className) throws Exception {
        Class.forName(className);
    }
    public static void main(String[] args) throws Exception {
        Demo.callForName("csr.TargetClass");
    }
}
class TargetClass {
     static {
         staticInitializerCalled();
     }
     @DirectCall(name="verifyCall", line=24, resolvedTargets = "Lcsr/Demo;")
     static private void staticInitializerCalled(){
         Demo.verifyCall();
     }
 }
