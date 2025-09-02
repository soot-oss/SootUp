package e4;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleSuperClassExample4 {
    static class Base {
        protected void method1(String dummy) {
            System.out.println("Method1: " + dummy);
        }
    }

    static class Derived extends Base {}

    public static void main(String[] args) throws Throwable {
        MethodHandle handle = MethodHandles.lookup()
                .findVirtual(Base.class, "method1", MethodType.methodType(void.class, String.class));

        handle.invoke(new Derived(), "SubClass!");
    }
}