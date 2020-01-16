package instanceofclassapi;
import lib.annotations.callgraph.DirectCall;
class Demo{ 
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          callIfInstanceOfTarget(new Target());
        else 
          callIfInstanceOfTarget(new Demo());
    }
    @DirectCall(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Linstanceofclassapi/Target;"
    )
    static void callIfInstanceOfTarget(Object o) {
      if (Target.class.isInstance(o))
        o.toString();
    }
    public String toString() { return "Demo"; }
}
class Target {
  public String toString() { return "Target"; }
}
