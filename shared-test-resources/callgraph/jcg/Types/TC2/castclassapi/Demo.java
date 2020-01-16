package castclassapi;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          castToTarget(Target.class, new Target());
        else 
          castToTarget(Demo.class, new Demo());
    }
    @DirectCall(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Lcastclassapi/Target;"
    )
    static <T> void castToTarget(Class<T> cls,  Object o) {
        T target = cls.cast(o);
        target.toString();
    }
    public String toString() { return "Demo"; }
}
