package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lib.annotations.callgraph.IndirectCall;
class Demo extends Superclass {
    protected String target() { return "42!"; }
    @IndirectCall(
        name = "target", returnType = String.class, line = 18,
        resolvedTargets = "Ltmr/Superclass;"
    )
    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findSpecial(Superclass.class, "target", methodType, Demo.class);
        String s = (String) handle.invokeExact(new Demo());
    }
}
class Superclass {
    protected String target() { return "42"; }
}
