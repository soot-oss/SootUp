package tmr;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lib.annotations.callgraph.DirectCall;
class Demo {
    public static void main(String[] args) throws Throwable {
        MethodType methodType = MethodType.methodType(void.class);
        MethodHandle handle = MethodHandles.lookup().findConstructor(Demo.class, methodType);
        Demo f = (Demo) handle.invokeExact();
    }
    @DirectCall(name="verifyCall", line=18, resolvedTargets = "Ltmr/Demo;")
    public Demo() {
        Demo.verifyCall();
    }
    public static void verifyCall(){ /* do something */ }
}
