package jvmc;
import lib.annotations.callgraph.IndirectCall;
public class Demo {
    @IndirectCall(name="exit", line = 12, resolvedTargets = "Ljava/lang/Thread;")
	public static void main(String[] args) throws InterruptedException {
        Runnable r = new TargetRunnable();
        Thread t = new Thread(r);
        t.start();
        t.join();
	}
}
class TargetRunnable implements Runnable {
    public void run(){
        /* Do the hard work */
    }   
}
