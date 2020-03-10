package si;
import lib.annotations.callgraph.DirectCall;
public class Demo {
	public static void main(String[] args) {
		Interface.referenceMe.toString();
	}
}
interface Interface {
    static String testHook = init();
    static final Demo referenceMe = new Demo();
    @DirectCall(name = "callback", line = 17, resolvedTargets = "Lsi/Interface;")
    static String init() {
        callback();
        return "Interface";
    }
    static void callback(){}
}
