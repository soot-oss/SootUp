import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

class VarHandleGetExample {
    static int value = 42;
}

class VarHandleInstanceExample {
    int instanceValue = 100;
}

class Base {
    protected void method1(String dummy) {
    }
}

class Derived extends Base {}

class PolymorphicSignatureExamples {
    public static void main(String[] args) throws Throwable {
        MethodHandle handle1 = MethodHandles.lookup().findVirtual(
                String.class, "indexOf", MethodType.methodType(int.class, String.class, int.class)
        );
        int result = (int) handle1.invokeExact("HelloWorld", "lo", 3);


        MethodHandle handle3 = MethodHandles.lookup()
                .findVirtual(Base.class, "method1", MethodType.methodType(void.class, String.class));
        handle3.invoke(new Derived(), "SubClass!");

        VarHandle handle4 = MethodHandles.lookup()
                .findStaticVarHandle(VarHandleGetExample.class, "value", int.class);
        handle4.get();

        VarHandle handle5 = MethodHandles.lookup()
                .findVarHandle(VarHandleInstanceExample.class, "instanceValue", int.class);
        handle5.varType();
    }
}