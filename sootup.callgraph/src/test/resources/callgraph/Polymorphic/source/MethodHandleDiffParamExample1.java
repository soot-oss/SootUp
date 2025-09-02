package e1;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleDiffParamExample1 {
    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup()
                .findVirtual(
                        String.class,
                        "indexOf",
                        MethodType.methodType(int.class, String.class, int.class)
                );

        handle.invoke("HelloWorld", "lo", 3);
    }
}