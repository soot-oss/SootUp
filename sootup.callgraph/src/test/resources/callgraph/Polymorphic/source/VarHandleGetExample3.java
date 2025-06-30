package e3;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleGetExample3 {
    static int value = 42;

    public static void main(String[] args) throws Throwable {
        VarHandle handle = MethodHandles.lookup()
                .findStaticVarHandle(VarHandleGetExample3.class, "value", int.class);

        handle.get();
    }
}