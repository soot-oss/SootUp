package cfne;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public static void verifyCall(){ /* do something */ }
    @DirectCall(name="verifyCall", line = 18, resolvedTargets = "Lcfne/Demo;")
	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cfne.CatchMeIfYouCan");
	        // DEAD CODE
	        LoadedClass lCls = (LoadedClass) cls.newInstance();
	    } catch(ClassCastException cce){
	        /* DEAD CODE */
	    } catch(ClassNotFoundException cnfe){
	        verifyCall();
	    } catch(Exception rest){
	        //DEAD CODE
	    }
	}
}
class LoadedClass {
}
