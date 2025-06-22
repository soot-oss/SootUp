package e2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInvokeExample2 {
    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup()
                .findVirtual(String.class, "substring", MethodType.methodType(String.class, int.class, int.class));

        handle.invoke("HelloWorld", 0, 5);
    }
}