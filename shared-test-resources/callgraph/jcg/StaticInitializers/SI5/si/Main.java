package si;
import lib.annotations.callgraph.DirectCall;
public class Main{
	public static void main(String[] args) {
		new Demo();
	}
}
class Demo {
	static {
		init();
	}
    @DirectCall(name = "callback", line = 19, resolvedTargets = "Lsi/Demo;")
	static void init() {
		callback();
	}
	static void callback() {}
}
