package jvmc;
import java.lang.System;
import java.lang.Runtime;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public static void callback(){ /* do something */ }
	public static void main(String[] args){
        Runnable r = new TargetRunnable();
        Runtime.getRuntime().addShutdownHook(new Thread(r));
	}
}
class TargetRunnable implements Runnable {
    @DirectCall(name = "callback", line = 22, resolvedTargets = "Ljvmc/Demo;")
    public void run(){
        Demo.callback();
    }
}
