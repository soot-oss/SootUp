package si;
import lib.annotations.callgraph.DirectCall;
public class Main {
	public static void main(String[] args) {
		Demo.assignMe = 42;
	}
}
class Demo {
	static String name = init();
    static int assignMe;
    @DirectCall(name = "callback", line = 18, resolvedTargets = "Lsi/Demo;")
	static String init() {
		callback();
		return "Demo";
	}
	static void callback() {}
}
