package jvmc;
import lib.annotations.callgraph.DirectCall;
public class Demo {
	public static void main(String[] args) throws InterruptedException {
        Runnable r = new TargetRunnable();
        Thread t = new Thread(r);
        t.start();
        t.join();
	}
}
class TargetRunnable implements Runnable {
    @DirectCall(name="verifyReachability", line = 19, resolvedTargets = "Ljvmc/TargetRunnable;")
    public void run(){
        verifyReachability();
        /* Do the hard work */
    }
    static void verifyReachability(){ }
}
