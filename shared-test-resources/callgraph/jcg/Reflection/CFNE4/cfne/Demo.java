package cfne;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public static void verifyCall(){ /* do something */ }
	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cfne.LoadedClass");
	        Object lCls = cls.newInstance();
	    } catch(ClassCastException cce){
	        // DEAD CODE
	    } catch(ClassNotFoundException cnfe){
	        // DEAD CODE
	    } catch(Exception rest){
            //DEAD CODE
        }
	}
}
class LoadedClass extends RootClass {
}
class RootClass {
    static {
        staticInitializerCalled();
    }
    @DirectCall(name="verifyCall", line=35, resolvedTargets = "Lcfne/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
