package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public String toString() { return "42"; }
    public static Demo field = new Demo();
    @DirectCall(
        name = "toString", returnType = String.class,
        line = 18, resolvedTargets = "Ltmr/Demo;"
    )
    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup().findStaticGetter(Demo.class, "field", Demo.class);
        handle.invoke().toString();
    }
}
