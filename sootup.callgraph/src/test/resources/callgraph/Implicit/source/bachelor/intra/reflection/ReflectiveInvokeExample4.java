package bachelor.intra.reflection;

import java.lang.reflect.Method;

public class ReflectiveInvokeExample4 {
    static class T { public void helloWorld(){} }

    public static void main(String[] args) throws Exception {
        T t = new T();
        // expression builds the name inline — not stored in a local
        Method m = T.class.getMethod("hello" + "World");
        m.invoke(t);
    }
}
