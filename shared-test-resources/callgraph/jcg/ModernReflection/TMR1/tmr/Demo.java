package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lib.annotations.callgraph.IndirectCall;
class Demo {
    static String target() { return "Demo"; }
    @IndirectCall(
        name = "target", returnType = String.class, line = 18,
        resolvedTargets = "Ltmr/Demo;"
    )
    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(Demo.class, "target", methodType);
        String s = (String) handle.invokeExact();
    }
}
