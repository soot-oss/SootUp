package lrr;
import lib.annotations.callgraph.DirectCall;
class Demo {
    private String className;
    public static void verifyCall(){ /* do something */ }
    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        demo.className = "lrr.TargetClass";
        Class.forName(demo.className);
    }
}
class TargetClass {
     static {
         staticInitializerCalled();
     }
     @DirectCall(name="verifyCall", line=25, resolvedTargets = "Llrr/Demo;")
     static private void staticInitializerCalled(){
         Demo.verifyCall();
     }
 }
