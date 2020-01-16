package csr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static String className;
    public static void verifyCall(){ /* do something */ }
    static void callForName() throws Exception {
        Class.forName(Demo.className);
    }
    public static void main(String[] args) throws Exception {
        Demo.className = "csr.CallTarget";
        Demo.callForName();
    }
}
class CallTarget {
     static {
         staticInitializerCalled();
     }
     @DirectCall(name="verifyCall", line=27, resolvedTargets = "Lcsr/Demo;")
     static private void staticInitializerCalled(){
         Demo.verifyCall();
     }
 }
