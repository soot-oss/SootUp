package si;
import lib.annotations.callgraph.DirectCall;
public interface Interface {
	static String name = init();
    @DirectCall(name = "callback", line = 10, resolvedTargets = "Lsi/Interface;")
	static String init() {
		callback();
		return "Demo";
	}
	default String defaultMethod() { return "Demo"; }
	static void callback() {}
}
class Demo implements Interface {
	public static void main(String[] args) {
		new Demo();
	}
}
