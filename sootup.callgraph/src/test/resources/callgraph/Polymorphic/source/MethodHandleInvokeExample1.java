package e1;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInvokeExample1 {
    public static void main(String[] args) throws Throwable {
        MethodHandleWrapper handleWrapper = new MethodHandleWrapper();
        MethodHandle handle = handleWrapper.getHandle();

        handle.invoke("Hello");
    }

    // wrapper to instantiate the abstract class MethodHandle using the keyword "new"
    static class MethodHandleWrapper {
        private final MethodHandle handle;

        public MethodHandleWrapper() throws Throwable {
            this.handle = MethodHandles.lookup()
                    .findVirtual(String.class, "length", MethodType.methodType(int.class));;
        }

        public MethodHandle getHandle() {
            return handle;
        }
    }
}