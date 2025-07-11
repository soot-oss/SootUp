package e1;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInvokeExample1 {
    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup()
                .findVirtual(String.class, "length", MethodType.methodType(int.class));

        handle.invoke("Hello");
    }
}