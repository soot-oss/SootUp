package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public String toString() { return "42"; }
    public Demo field;
    public Demo() {
        this.field = this;
    }
    @DirectCall(
        name = "toString", returnType = String.class,
        line = 22, resolvedTargets = "Ltmr/Demo;"
    )
    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup().findGetter(Demo.class, "field", Demo.class);
        handle.invoke(new Demo()).toString();
    }
}
