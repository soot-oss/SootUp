package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lib.annotations.callgraph.IndirectCall;
class Demo {
    public static String target(String param1, String param2) { return param1 + param2; }
    @IndirectCall(
        name = "target", returnType = String.class, parameterTypes = { String.class, String.class },
        line = 18, resolvedTargets = "Ltmr/Demo;"
    )
    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(String.class, String.class, String.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(Demo.class, "target", methodType);
        String s = (String) handle.invokeWithArguments(new Object[]{ "42", "42" });
    }
}
