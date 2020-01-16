package cfne;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public static void verifyCall(){ /* do something */ }
    @DirectCall(name="verifyCall", line = 15, resolvedTargets = "Lcfne/Demo;")
	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cfne.DeceptiveClass");
	        LoadedClass lCls = (LoadedClass) cls.newInstance();
	    } catch(ClassCastException cce){
	        verifyCall();
	    } catch(ClassNotFoundException cnfe){
	        // DEAD CODE
	    } catch(Exception rest){
            // DEAD CODE
        }
	}
}
class DeceptiveClass {
}
class LoadedClass {
}
