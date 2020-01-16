package csr;
import java.util.Properties;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void verifyCall(){ /* do something */ }
    static void callForName() throws Exception {
    	String className = System.getProperty("className");
        Class.forName(className);
    }
    public static void main(String[] args) throws Exception {
		Properties props = System.getProperties();
		props.put("className", "csr.TargetClass");
		System.setProperties(props);
        Demo.callForName();
    }
}
class TargetClass {
     static {
         staticInitializerCalled();
     }
     @DirectCall(name="verifyCall", line=31, resolvedTargets = "Lcsr/Demo;")
     static private void staticInitializerCalled(){
         Demo.verifyCall();
     }
 }
