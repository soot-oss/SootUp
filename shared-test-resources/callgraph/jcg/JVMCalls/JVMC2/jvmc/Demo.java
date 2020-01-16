package jvmc;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public static void callback(){};
	public static void main(String[] args){
          for(int i = -1; i < args.length; i++){
              new Demo();
          }
	}
	@DirectCall(name="callback", line=18, resolvedTargets = "Ljvmc/Demo;")
    public void finalize() throws java.lang.Throwable {
        callback();
        super.finalize();
    }	
}
