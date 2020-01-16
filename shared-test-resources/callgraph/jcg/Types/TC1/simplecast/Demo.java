package simplecast;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          castToTarget(new Target());
        else 
          castToTarget(new Demo());
    }
    @DirectCall(
        name = "target", returnType = String.class, line = 18,
        resolvedTargets = "Lsimplecast/Target;"
    )
    static void castToTarget(Object o) {
        Target b = (Target) o;
        b.target();
    }
    public String target() { return "Demo"; }
}
class Target {
  public String target() { return "Target"; }
}
